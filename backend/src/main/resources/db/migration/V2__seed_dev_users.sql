-- Stage 3 — PostgreSQL 18 persistence
-- Deterministic dev/local seed data — 3 users with fixed UUIDs so other
-- agents' fixtures and manual testing can reference stable IDs.
-- Guarded with ON CONFLICT so re-running (or baseline-on-migrate) is a no-op.

insert into users (id, google_subject, email, full_name, department, is_active, created_at)
values
    ('11111111-1111-1111-1111-111111111111', 'dev-google-subject-alice', 'alice@example.com', 'Alice Anderson', 'Support', true, now()),
    ('22222222-2222-2222-2222-222222222222', 'dev-google-subject-bob',   'bob@example.com',   'Bob Brown',      'Engineering', true, now()),
    ('33333333-3333-3333-3333-333333333333', 'dev-google-subject-carla', 'carla@example.com', 'Carla Chen',     'Operations', true, now())
on conflict (id) do nothing;
