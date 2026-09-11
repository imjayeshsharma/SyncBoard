# Build stages

SyncBoard is built incrementally. Each stage introduces one layer of real functionality on
top of a compiling skeleton; later PRs replace TODO-marked bodies with real implementations
stage by stage. This document is the developer-facing map of what each stage covers, what
must work by the end of it, and which packages it touches.

## Stage 0 — Skeleton

**Introduces:** the full project structure across both backend and frontend — every package,
class, component, migration and pipeline the product calls for — wired together and
type-/compile-consistent, with business logic left as clearly-marked TODOs.

**Must work at the end:** the backend compiles and boots; the frontend type-checks and
builds; Docker images build for both; `docker compose up` brings up the full topology.

**Touches:** everything — this is the baseline every later stage builds on.

## Stage 1 — Java 25 domain core

**Introduces:** the framework-free domain model: `TicketStatus` and `Priority` enums with
their wire values, the legal status-transition table, and the domain-level exceptions/error
codes (`ILLEGAL_TRANSITION`, `ASSIGNEE_REQUIRED`, `REASON_REQUIRED`, …).

**Must work at the end:** `TransitionRules.check(...)` correctly accepts every legal
transition and rejects every illegal one, including the `BLOCKED`/`blockedFromStatus`
round-trip, with unit tests covering the full transition table.

**Touches:** `backend/src/main/java/com/syncboard/domain/**`,
`backend/src/test/java/com/syncboard/domain/**`.

## Stage 2 — REST API layer

**Introduces:** the HTTP surface — request/response DTOs, controllers for every endpoint in
the API contract, the enum JSON binding module, and the global error-handling envelope.

**Must work at the end:** every endpoint is routable and validates its input; a
`TicketController` test asserts the `TicketDetail` JSON shape, the `201` + `Location` header
on create, and the `409 VERSION_CONFLICT` envelope.

**Touches:** `backend/src/main/java/com/syncboard/api/**`,
`backend/src/main/java/com/syncboard/service/**` (interfaces & DTO mapping),
`backend/src/test/java/com/syncboard/api/**`.

## Stage 3 — PostgreSQL 18 persistence

**Introduces:** the Flyway baseline schema, JPA entities with wire-value enum converters,
Spring Data repositories, the board-query projection, and the real implementation of
`TicketServiceImpl.transition(...)` wiring the service layer to `domain.TransitionRules`.

**Must work at the end:** `V1__baseline.sql` creates the full schema against Postgres 18;
a Testcontainers-backed repository test saves and reads back a ticket, round-tripping its
status as the wire value; ticket transitions persist a `ticket_status_history` row and
respect optimistic locking (`tickets.version`).

**Touches:** `backend/src/main/java/com/syncboard/persistence/**`,
`backend/src/main/resources/db/migration/**`,
`backend/src/test/java/com/syncboard/persistence/**`, plus the `service` layer's
persistence-backed implementations.

## Stage 4 — Angular 22 frontend

**Introduces:** the application shell (routing, layout, providers), core signal stores and
typed API clients, and the two primary screens: the global Kanban board (CDK drag-drop
across the six board columns) and the ticket detail & resource hub (threaded comments,
status history, external links, status/priority/assignee/due-date controls).

**Must work at the end:** the board renders from mock or live data with drag-and-drop wired
to `BoardStore.moveTicket(...)`; ticket detail renders comments, history and links; the
status selector only offers transitions legal from the current state.

**Touches:** `frontend/src/index.html`, `frontend/src/main.ts`, `frontend/src/styles.scss`,
`frontend/src/environments/**`, `frontend/src/app/app.*`, `frontend/src/app/core/**`,
`frontend/src/app/features/**`, `frontend/src/app/shared/**`, `frontend/public/**`.

## Stage 5 — Google Workspace SSO

**Introduces:** OAuth2/OIDC login against Google, hosted-domain (`hd` claim) enforcement,
user provisioning keyed on the Google subject (never on email), and the real
`CurrentUserProvider` implementation replacing the local-profile stub. Also introduces
orphaned-ticket handling: reassigning tickets away from a deactivated user.

**Must work at the end:** signing in with a Google account outside the configured hosted
domain is rejected (`FORBIDDEN_DOMAIN`); a first-time sign-in provisions a `users` row;
`/api/v1/board` returns `401` unauthenticated and succeeds once signed in.

**Touches:** `backend/src/main/java/com/syncboard/config/**`,
`backend/src/test/java/com/syncboard/config/**`.

## Stage 6 — Cache + realtime

**Introduces:** the Valkey-backed board response cache (with a cache-failure handler that
degrades to Postgres rather than erroring), the STOMP/WebSocket endpoint, and the publisher
broadcasting `TICKET_MOVED`/`TICKET_CREATED`/`TICKET_UPDATED` board events and per-ticket
comment events.

**Must work at the end:** repeated `GET /api/v1/board` calls hit the cache within its TTL;
a Valkey outage does not fail board requests; a client connected to `/topic/board` receives
an event when another client moves a card.

**Touches:** `backend/src/main/java/com/syncboard/config/**` (cache config),
`backend/src/main/java/com/syncboard/realtime/**`.

## Stage 7 — GraphQL read API

**Introduces:** a schema-first, read-only GraphQL API (`board`, `ticket(id)`, `myTickets`)
layered on the same service layer as REST, with batch-loaded comment/link fields and a
`DateTime` scalar.

**Must work at the end:** GraphiQL (enabled on the `local` profile) can run `{ board { ... } }`
and resolve the same data the REST board endpoint returns.

**Touches:** `backend/src/main/java/com/syncboard/graphql/**`,
`backend/src/main/resources/graphql/**`.

## Stage 8 — Infrastructure, CI/CD and documentation

**Introduces:** production-shaped Docker images for both services, the single-host Docker
Compose topology, GitHub Actions CI (build + test both halves, then build both images),
CodeQL scanning, dependency update automation, the AWS target-topology description, and this
documentation set.

**Must work at the end:** `docker compose up` brings up a fully working local stack; CI
passes on a clean checkout; both Dockerfiles build a runnable, non-root, health-checked image.

**Touches:** `infra/**`, `.github/**`, `docker-compose.yml`, `backend/Dockerfile`,
`backend/.dockerignore`, `frontend/Dockerfile`, `frontend/.dockerignore`,
`frontend/nginx.conf`, `README.md`, `docs/**`, `.env.example`.
