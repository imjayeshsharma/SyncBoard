-- Stage 3 — PostgreSQL 18 persistence
-- Baseline schema for SyncBoard, per 01-CONTRACT.md §3.
-- All timestamps are timestamptz stored in UTC. All PKs are uuid.
-- Status/priority/platform are stored as varchar holding the *wire value*
-- (e.g. 'InProgress'), enforced here with check constraints, and mapped in
-- JPA with explicit AttributeConverters (see persistence/converter/*).

create extension if not exists "pgcrypto";

-- ---------------------------------------------------------------------------
-- users
-- ---------------------------------------------------------------------------
create table users (
    id             uuid primary key default gen_random_uuid(),
    google_subject varchar(255) not null unique,
    email          varchar(320) not null,
    full_name      varchar(255) not null,
    department     varchar(120),
    is_active      boolean not null default true,
    created_at     timestamptz not null default now()
);

-- ---------------------------------------------------------------------------
-- tickets
-- ---------------------------------------------------------------------------
create table tickets (
    id                   uuid primary key default gen_random_uuid(),
    title                varchar(200) not null,
    description          text,
    status               varchar(20) not null
        check (status in ('Open','Triaging','InProgress','Blocked','UnderReview','Completed','Cancelled')),
    priority             varchar(10) not null
        check (priority in ('Critical','High','Medium','Low')),
    category             varchar(80),
    reporter_id          uuid not null references users(id),
    assignee_id          uuid null references users(id),
    blocked_from_status  varchar(20) null
        check (blocked_from_status is null or blocked_from_status in
            ('Open','Triaging','InProgress','Blocked','UnderReview','Completed','Cancelled')),
    due_at               timestamptz null,
    created_at           timestamptz not null,
    updated_at           timestamptz not null,
    closed_at            timestamptz null,
    version              bigint not null default 0
);

create index idx_tickets_status on tickets(status);
create index idx_tickets_assignee_id on tickets(assignee_id);
create index idx_tickets_due_at on tickets(due_at);

-- ---------------------------------------------------------------------------
-- ticket_links
-- ---------------------------------------------------------------------------
create table ticket_links (
    id          uuid primary key default gen_random_uuid(),
    ticket_id   uuid not null references tickets(id) on delete cascade,
    url         text not null,
    link_title  varchar(200),
    platform    varchar(40) not null
        check (platform in ('keka','google-drive','google-docs','github','slack','other')),
    created_at  timestamptz not null
);

create index idx_ticket_links_ticket_id on ticket_links(ticket_id);

-- ---------------------------------------------------------------------------
-- comments
-- ---------------------------------------------------------------------------
create table comments (
    id                  uuid primary key default gen_random_uuid(),
    ticket_id           uuid not null references tickets(id) on delete cascade,
    author_id           uuid not null references users(id),
    body_text           text not null,
    parent_comment_id   uuid null references comments(id),
    created_at          timestamptz not null,
    edited_at           timestamptz null
);

create index idx_comments_ticket_id_created_at on comments(ticket_id, created_at);

-- ---------------------------------------------------------------------------
-- ticket_status_history (append-only — no update/delete anywhere in the app)
-- ---------------------------------------------------------------------------
create table ticket_status_history (
    id           uuid primary key default gen_random_uuid(),
    ticket_id    uuid not null references tickets(id) on delete cascade,
    from_status  varchar(20) null
        check (from_status is null or from_status in
            ('Open','Triaging','InProgress','Blocked','UnderReview','Completed','Cancelled')),
    to_status    varchar(20) not null
        check (to_status in ('Open','Triaging','InProgress','Blocked','UnderReview','Completed','Cancelled')),
    changed_by   uuid not null references users(id),
    changed_at   timestamptz not null,
    note         text null
);

create index idx_ticket_status_history_ticket_id_changed_at on ticket_status_history(ticket_id, changed_at);
