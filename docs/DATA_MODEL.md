# Data model

PostgreSQL 18, created by the Flyway baseline migration
(`backend/src/main/resources/db/migration/V1__baseline.sql`). All primary keys are `uuid`
(generated via `pgcrypto`'s `gen_random_uuid()`, though entities also assign their own UUID
before insert). All timestamps are `timestamptz`, stored and read in UTC. Status and priority
columns are `varchar` holding the enum's **wire value** (e.g. `'InProgress'`) — never a
Postgres enum type, never an ordinal — so the database value, the JSON value and the
TypeScript value are always identical.

## Tables

```
users
  id                 uuid            primary key
  google_subject     varchar(255)    unique, not null
  email              varchar(320)    not null
  full_name          varchar(255)    not null
  department         varchar(120)
  is_active          boolean         not null, default true
  created_at         timestamptz     not null, default now()

tickets
  id                   uuid          primary key
  title                varchar(200)  not null
  description          text
  status               varchar(20)   not null
  priority             varchar(10)   not null
  category             varchar(80)
  reporter_id          uuid          not null, references users(id)
  assignee_id          uuid          references users(id)
  blocked_from_status  varchar(20)
  due_at               timestamptz
  created_at           timestamptz   not null
  updated_at           timestamptz   not null
  closed_at            timestamptz
  version              bigint        not null, default 0        -- optimistic locking (@Version)

ticket_links
  id                 uuid            primary key
  ticket_id          uuid            not null, references tickets(id) on delete cascade
  url                text            not null
  link_title         varchar(200)
  platform           varchar(40)     not null
  created_at         timestamptz     not null

comments
  id                   uuid          primary key
  ticket_id            uuid          not null, references tickets(id) on delete cascade
  author_id            uuid          not null, references users(id)
  body_text            text          not null
  parent_comment_id    uuid          references comments(id)     -- self-FK, enables threading
  created_at           timestamptz   not null
  edited_at            timestamptz

ticket_status_history
  id             uuid          primary key
  ticket_id      uuid          not null, references tickets(id) on delete cascade
  from_status    varchar(20)                                     -- null for the initial OPEN row
  to_status      varchar(20)   not null
  changed_by     uuid          not null, references users(id)
  changed_at     timestamptz   not null
  note           text                                            -- required by domain rules for BLOCKED/CANCELLED
```

## Indexes

- `tickets(status)`
- `tickets(assignee_id)`
- `tickets(due_at)`
- `comments(ticket_id, created_at)`
- `ticket_links(ticket_id)`
- `ticket_status_history(ticket_id, changed_at)`

## Entity-relationship diagram

```
                         ┌────────────────────┐
                         │       users         │
                         │─────────────────────│
                         │ id (PK)              │
                         │ google_subject (UQ)  │
                         │ email                │
                         │ full_name            │
                         │ department           │
                         │ is_active            │
                         │ created_at           │
                         └──────────┬──────────┘
             reporter_id / assignee_id (FK, x2)   changed_by (FK)   author_id (FK)
             ┌────────────────────┬────────────────────┬──────────────────┐
             │                    │                    │                  │
             ▼                    │                    ▼                  ▼
┌───────────────────────┐         │        ┌─────────────────────────┐  ┌──────────────────┐
│        tickets         │        │        │ ticket_status_history    │  │     comments      │
│─────────────────────── │        │        │──────────────────────── │  │──────────────────│
│ id (PK)                 │◀──────┴───────▶│ id (PK)                  │  │ id (PK)           │
│ title                   │  ticket_id (FK) │ ticket_id (FK)           │  │ ticket_id (FK) ───┼──▶ tickets.id
│ description             │  on delete      │ from_status              │  │ author_id (FK)    │
│ status                  │  cascade        │ to_status                │  │ body_text         │
│ priority                │                 │ changed_by (FK)          │  │ parent_comment_id ─┼─┐ self-FK
│ category                │                 │ changed_at               │  │ (FK → comments.id) │ │ (threading)
│ reporter_id (FK)         │                 │ note                     │  │ created_at         │◀┘
│ assignee_id (FK, null)   │                 └─────────────────────────┘  │ edited_at          │
│ blocked_from_status      │                                              └──────────────────┘
│ due_at                   │
│ created_at / updated_at  │                 ┌─────────────────────────┐
│ closed_at                │◀───────────────▶│      ticket_links        │
│ version                  │  ticket_id (FK)  │──────────────────────── │
└───────────────────────┘  on delete cascade │ id (PK)                  │
                                              │ ticket_id (FK)           │
                                              │ url                      │
                                              │ link_title               │
                                              │ platform                 │
                                              │ created_at               │
                                              └─────────────────────────┘
```

## Notes

- **`overdue` is never a column.** It is derived at read time:
  `dueAt != null && dueAt.isBefore(now) && status not in (COMPLETED, CANCELLED)`.
- **`ticket_status_history` is append-only.** No repository method updates or deletes a row —
  it is the ticket's permanent audit trail.
- **`blocked_from_status`** only holds a value while a ticket is `BLOCKED`; it records where
  to return the ticket to, and is cleared on any transition out of `BLOCKED`.
- **Users are keyed on `google_subject`, not email**, so a user whose email changes upstream
  in Google Workspace is still recognized as the same account; provisioning upserts on that
  column (Stage 5).
- **`comments.parent_comment_id`** is a self-referencing FK; a `null` value means a top-level
  comment on the ticket, otherwise it is a reply, letting the API assemble a threaded
  `CommentNode` tree from one flat query.
