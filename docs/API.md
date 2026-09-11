# REST API reference

Base path: **`/api/v1`**. All request/response bodies are JSON. All IDs are UUID strings.
Optional fields (`?`) are omitted from the JSON response when null rather than sent as `null`.

Authentication (Stage 5) is Google Workspace OIDC; before that lands, the `local` Spring
profile authenticates every request as a seeded dev user.

## Endpoints

| Method | Path | Body | Returns |
|---|---|---|---|
| GET | `/api/v1/board` | – | `BoardResponse` |
| GET | `/api/v1/tickets` | query: `status, assigneeId, priority, q, page, size` | `PageResponse<TicketSummary>` |
| POST | `/api/v1/tickets` | `CreateTicketRequest` | `201` + `TicketDetail` |
| GET | `/api/v1/tickets/{id}` | – | `TicketDetail` |
| PATCH | `/api/v1/tickets/{id}` | `UpdateTicketRequest` | `TicketDetail` |
| POST | `/api/v1/tickets/{id}/transitions` | `TransitionRequest` | `TicketDetail` |
| GET | `/api/v1/tickets/{id}/history` | – | `StatusHistoryEntry[]` |
| GET | `/api/v1/tickets/{id}/comments` | – | `CommentNode[]` (threaded) |
| POST | `/api/v1/tickets/{id}/comments` | `CreateCommentRequest` | `201` + `CommentNode` |
| GET | `/api/v1/tickets/{id}/links` | – | `TicketLink[]` |
| POST | `/api/v1/tickets/{id}/links` | `CreateLinkRequest` | `201` + `TicketLink` |
| DELETE | `/api/v1/tickets/{id}/links/{linkId}` | – | `204` |
| GET | `/api/v1/users` | query: `activeOnly` | `UserSummary[]` |
| GET | `/api/v1/me` | – | `UserSummary` |

`POST` endpoints that create a resource return `201 Created` with a `Location` header
pointing at the new resource.

## Payload shapes

```
UserSummary        { id, fullName, email, department?, active }
TicketLink          { id, url, linkTitle?, platform, createdAt }
CommentNode         { id, bodyText, author: UserSummary, createdAt, editedAt?, replies: CommentNode[] }
StatusHistoryEntry  { id, fromStatus?, toStatus, changedBy: UserSummary, changedAt, note? }

TicketSummary { id, title, status, priority, category?, assignee?: UserSummary,
                reporter: UserSummary, dueAt?, overdue, commentCount, linkCount,
                createdAt, updatedAt, version }

TicketDetail  { ...all TicketSummary fields..., description?, blockedFromStatus?, closedAt?,
                links: TicketLink[], comments: CommentNode[], history: StatusHistoryEntry[] }

BoardResponse { columns: BoardColumn[], generatedAt }
BoardColumn   { status, label, tickets: TicketSummary[] }

CreateTicketRequest  { title*, description?, category?, priority*, assigneeId?, dueAt?,
                        links?: { url*, linkTitle?, platform? }[] }
UpdateTicketRequest  { title?, description?, category?, priority?, assigneeId?, dueAt?, version* }
TransitionRequest    { toStatus*, note?, version* }
CreateCommentRequest { bodyText*, parentCommentId? }
CreateLinkRequest    { url*, linkTitle?, platform? }
PageResponse<T>      { content: T[], page, size, totalElements, totalPages }
```

`*` marks a required field; `?` marks an optional/nullable one.

`overdue` on `TicketSummary`/`TicketDetail` is computed at read time, never stored:
`dueAt != null && dueAt.isBefore(now) && status not in (COMPLETED, CANCELLED)`.

`platform` on a link is derived server-side from the URL host whenever the client omits it:
one of `keka`, `google-drive`, `google-docs`, `github`, `slack`, `other`.

## Ticket status and priority wire values

| Java constant | Wire value | Board column |
|---|---|---|
| `OPEN` | `Open` | Open |
| `TRIAGING` | `Triaging` | Triaging |
| `IN_PROGRESS` | `InProgress` | In Progress |
| `BLOCKED` | `Blocked` | Blocked |
| `UNDER_REVIEW` | `UnderReview` | Under Review |
| `COMPLETED` | `Completed` | Completed |
| `CANCELLED` | `Cancelled` | *(filter only — not a board column)* |

Board columns, left to right: `Open, Triaging, InProgress, UnderReview, Blocked, Completed`.

`Priority`: `CRITICAL → Critical`, `HIGH → High`, `MEDIUM → Medium`, `LOW → Low`.

## Legal status transitions

```
OPEN         -> TRIAGING, CANCELLED
TRIAGING     -> IN_PROGRESS, CANCELLED
IN_PROGRESS  -> UNDER_REVIEW, BLOCKED, CANCELLED
UNDER_REVIEW -> COMPLETED, IN_PROGRESS, BLOCKED, CANCELLED
BLOCKED      -> <the status it was blocked from>, CANCELLED
COMPLETED    -> (terminal)
CANCELLED    -> (terminal)
```

Rules that ride along with a transition:

- Transitioning **to `COMPLETED`** requires `assigneeId != null`, or the request fails with
  `ASSIGNEE_REQUIRED`.
- Transitioning **to `CANCELLED`** requires a non-blank `note`, or the request fails with
  `REASON_REQUIRED`.
- Transitioning **to `BLOCKED`** requires a non-blank `note`; the source status is recorded
  in `blockedFromStatus`. Leaving `BLOCKED` is only legal back to `blockedFromStatus` or to
  `CANCELLED`, and clears `blockedFromStatus`.
- Any other transition pair fails with `ILLEGAL_TRANSITION`.

## Errors

Every 4xx/5xx response uses the same envelope:

```json
{
  "timestamp": "2026-01-01T12:00:00Z",
  "status": 409,
  "code": "VERSION_CONFLICT",
  "message": "human readable message",
  "path": "/api/v1/tickets/...",
  "fieldErrors": { "title": "must not be blank" }
}
```

`fieldErrors` is only present for `VALIDATION_FAILED`.

| Code | HTTP status |
|---|---|
| `VALIDATION_FAILED` | 400 |
| `UNAUTHENTICATED` | 401 |
| `FORBIDDEN_DOMAIN` | 403 |
| `TICKET_NOT_FOUND` | 404 |
| `USER_NOT_FOUND` | 404 |
| `COMMENT_NOT_FOUND` | 404 |
| `LINK_NOT_FOUND` | 404 |
| `VERSION_CONFLICT` | 409 |
| `ILLEGAL_TRANSITION` | 422 |
| `ASSIGNEE_REQUIRED` | 422 |
| `REASON_REQUIRED` | 422 |
| `INTERNAL_ERROR` | 500 |

## Optimistic concurrency

`tickets.version` is a JPA `@Version` column. Clients must send back the `version` they last
read on `PATCH`/`transitions` requests; a stale write is rejected with `409 VERSION_CONFLICT`
rather than silently overwriting a concurrent change.

## Realtime (STOMP over `/ws`)

| Topic | Payload |
|---|---|
| `/topic/board` | `{ "type": "TICKET_MOVED" \| "TICKET_CREATED" \| "TICKET_UPDATED", "ticketId", "fromStatus"?, "toStatus"?, "at" }` |
| `/topic/tickets/{ticketId}/comments` | `CommentNode` |

Connect with SockJS/STOMP against `/ws`; subscribe to `/topic/board` for board-wide updates
and to a specific ticket's comment topic while its detail view is open.
