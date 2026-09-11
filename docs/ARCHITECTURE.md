# Architecture

## Layers

**Backend** (`backend/src/main/java/com/syncboard/`) is a layered Spring Boot application:

| Package | Responsibility |
|---|---|
| `domain` | Framework-free model: `TicketStatus`, `Priority`, `LinkPlatform` enums, the legal status-transition rules, and domain exceptions. No Spring, no JPA — pure Java 25. Everything else depends on it; it depends on nothing. |
| `persistence` | JPA entities, `AttributeConverter`s mapping enums to their wire values, Spring Data repositories, board-query projections. Owns the Flyway migrations that create the schema. |
| `service` | Orchestrates persistence + domain rules behind interfaces the API layer calls: loads entities, invokes `domain.TransitionRules`, appends status-history rows, maps optimistic-lock failures. |
| `api` | HTTP surface: REST controllers, request/response DTOs (records), Jackson enum (de)serialization, a `@RestControllerAdvice` translating domain/validation errors into the standard error envelope. |
| `config` | Cross-cutting platform concerns: Spring Security + Google OIDC login, hosted-domain enforcement, CORS, cache configuration. |
| `realtime` | STOMP/WebSocket configuration and the publisher that broadcasts board and comment events. |
| `graphql` | Schema-first, read-only GraphQL API layered on top of the same service layer. |

**Frontend** (`frontend/src/app/`) is a standalone, signal-based, zoneless Angular application:

| Directory | Responsibility |
|---|---|
| `core` | Signal-based stores (`BoardStore`, `TicketStore`, `AuthStore`), typed HTTP API clients per resource, HTTP interceptors, the realtime (STOMP) client, and the shared TypeScript models mirroring the backend DTOs. |
| `features` | Route-level screens: the Kanban board, ticket detail & resource hub, my-tasks view, ticket creation, not-found. |
| `shared` | Presentational components (priority chip, status badge, user avatar, …), pipes, and the client-side mirror of the legal-transition table used to drive the status selector. |

Dependency direction is one-way: `features` depends on `core` and `shared`; `core` and
`shared` never depend on `features`. On the backend, `api`/`service`/`config`/`realtime`/`graphql`
depend on `domain` and `persistence`; `domain` depends on nothing in this codebase.

## Request path: moving a card

This traces a drag-and-drop move on the Kanban board end to end.

```
┌─────────────┐     ┌──────────────┐     ┌───────────────┐     ┌────────────────────┐
│ Angular UI   │     │ BoardStore   │     │ TicketApi     │     │ POST .../transitions│
│ cdkDropList  │────▶│ optimistic   │────▶│ Service       │────▶│ (HTTP)              │
│ drop() event │     │ move + patch │     │ (typed client)│     │                     │
└─────────────┘     └──────┬───────┘     └───────────────┘     └──────────┬──────────┘
                            │ rollback if the                              │
                            │ request below fails                          ▼
                            │                                    ┌────────────────────┐
                            │                                    │ TicketController    │
                            │                                    │ @Valid binds        │
                            │                                    │ TransitionRequest   │
                            │                                    └──────────┬──────────┘
                            │                                               ▼
                            │                                    ┌────────────────────┐
                            │                                    │ TicketServiceImpl   │
                            │                                    │ .transition(...)    │
                            │                                    │  - load ticket      │
                            │                                    │    (version check)  │
                            │                                    │  - TransitionRules  │
                            │                                    │    .check(...)      │
                            │                                    │  - apply new status,│
                            │                                    │    blockedFrom/     │
                            │                                    │    closedAt         │
                            │                                    │  - append           │
                            │                                    │    TicketStatus     │
                            │                                    │    HistoryEntity    │
                            │                                    └──────────┬──────────┘
                            │                                               ▼
                            │                                    ┌────────────────────┐
                            │                                    │ Postgres commit     │
                            │                                    │ (@Version bump)     │
                            │                                    └──────────┬──────────┘
                            │                                               ▼
                            │                                    ┌────────────────────┐
                            │                                    │ Board cache evicted │
                            │                                    │ (Valkey, TTL-backed)│
                            │                                    └──────────┬──────────┘
                            │                                               ▼
                            │                                    ┌────────────────────┐
                            │                                    │ BoardEventPublisher │
                            │                                    │ → STOMP             │
                            │                                    │ /topic/board         │
                            │                                    │ TICKET_MOVED         │
                            │                                    └──────────┬──────────┘
                            ▼                                               ▼
                  ┌──────────────────────┐                    ┌──────────────────────┐
                  │ 200 TicketDetail      │◀───────────────────│ Every subscribed      │
                  │ reconciles the        │                    │ browser's realtime     │
                  │ optimistic patch with │                    │ client updates its     │
                  │ the authoritative     │                    │ BoardStore in place    │
                  │ server state           │                    └──────────────────────┘
                  └──────────────────────┘
```

1. The user drags a card between columns. `cdkDropList`'s `drop()` handler computes the
   source and target `TicketStatus` and calls `BoardStore.moveTicket(...)`.
2. `BoardStore` applies the move **optimistically** — the card visibly moves before the
   network round-trip completes — and calls `TicketApiService`, which issues
   `POST /api/v1/tickets/{id}/transitions` with `{ toStatus, note?, version }`.
3. `TicketController` binds and validates the request and delegates to
   `TicketServiceImpl.transition(...)`, the one method in the skeleton implemented for real.
4. The service loads the ticket (JPA optimistic locking compares the client's `version`
   against the row), builds a `TransitionContext`, and calls the domain layer's
   `TransitionRules.check(...)` — the single source of truth for which transitions are legal
   and which side rules apply (`ASSIGNEE_REQUIRED`, `REASON_REQUIRED`, `blockedFromStatus`
   bookkeeping).
5. On success: the entity's status (and `blockedFromStatus`/`closedAt` as applicable) is
   updated, an append-only `ticket_status_history` row is written, and the transaction
   commits — `@Version` on `tickets` means a concurrent conflicting write instead surfaces as
   `ObjectOptimisticLockingFailureException`, translated by `GlobalExceptionHandler` into
   `409 VERSION_CONFLICT`.
6. The board's cached response (Valkey, keyed with a short TTL) is evicted so the next
   `GET /api/v1/board` reflects the change.
7. `BoardEventPublisher` broadcasts a `TICKET_MOVED` event to `/topic/board` over STOMP;
   every connected browser's realtime client patches its own `BoardStore`, independent of
   whether it was the browser that made the move.
8. The original HTTP response (`200 TicketDetail`) reconciles the initiating browser's
   optimistic patch with the authoritative server state (in case a server-side rule, e.g.
   `blockedFromStatus`, changed something beyond a plain status flip).

## Concurrency & rollback story

- **Optimistic UI, pessimistic-safe backend.** The frontend never blocks on the network for
  a drag — it patches state immediately and rolls back only if the API call fails. The
  backend never trusts the client's optimism: every mutating request carries the `version`
  the client last read, and JPA's `@Version` column is the actual arbiter.
- **Stale write → `409 VERSION_CONFLICT`.** `GlobalExceptionHandler` maps
  `ObjectOptimisticLockingFailureException` to the standard error envelope. `BoardStore` rolls
  the optimistic move back and (in later stages) surfaces a "this ticket changed — reload"
  prompt rather than silently overwriting someone else's edit.
- **Illegal transitions never reach Postgres.** `domain.TransitionRules` is checked in-memory
  before anything is written, so `ILLEGAL_TRANSITION`/`ASSIGNEE_REQUIRED`/`REASON_REQUIRED`
  fail fast with no partial state change.
- **History is append-only.** `ticket_status_history` has no update or delete path anywhere
  in the codebase, so the audit trail behind every ticket is tamper-evident by construction.
- **Cache failures degrade, they don't cascade.** The board cache's error handler (Stage 6)
  logs and swallows Valkey get/put/evict failures rather than failing the request — a cache
  outage means every board load falls through to Postgres, not a broken board.
- **Realtime is a broadcast, not a source of truth.** STOMP events tell already-connected
  clients to re-sync; the REST response to the mutating request itself is what the initiating
  client trusts, so a dropped WebSocket message never leaves that client's own view stale.
