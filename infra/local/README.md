# Running SyncBoard locally

Two supported ways to run the stack locally: everything under Docker Compose, or the API
and web app run directly on the host against just Postgres/Valkey in containers. Both use
the same `local` Spring profile.

## Option A — everything in Docker Compose

```bash
cp .env.example .env
# edit .env — the placeholder Postgres/Google values are fine for a first run except
# GOOGLE_CLIENT_ID/GOOGLE_CLIENT_SECRET if you need to exercise Google sign-in

docker compose up -d
```

This starts `postgres` (18), `valkey` (8), `api` and `web`. The web app is served at
`http://localhost:4200` and proxies `/api`, `/graphql` and `/ws` through to `api:8080`
(see `frontend/nginx.conf`). The API is also reachable directly at `http://localhost:8080`.

## Option B — infra in containers, app processes on the host

Useful for fast edit/rebuild loops during development.

```bash
cp .env.example .env
docker compose up -d postgres valkey
```

Then, in separate terminals:

```bash
# Backend — Flyway runs its migrations automatically on startup
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

```bash
# Frontend — proxy.conf.json forwards /api, /graphql, /ws to http://localhost:8080
cd frontend
npm install
npm start
```

The Angular dev server serves at `http://localhost:4200`.

## The `local` Spring profile

Activated with `SPRING_PROFILES_ACTIVE=local` (the default in `.env.example`). Per
`backend/src/main/resources/application.yml`, it:

- switches the cache abstraction to an in-memory `simple` cache instead of Valkey/Redis,
  so the board endpoint works even before Stage 6 wiring is exercised;
- enables GraphiQL at `/graphiql`;
- turns on `DEBUG` logging for `com.syncboard` and Hibernate SQL;
- pairs with `service.impl.StubCurrentUserProvider` (`@Profile("local")`), which returns a
  seeded dev user in place of a real Google sign-in, so the REST/GraphQL API is usable before
  Stage 5's OIDC wiring lands.

## Seeded dev users

The baseline Flyway migration ships a follow-up migration that inserts a handful of
deterministic dev users (guarded with `on conflict do nothing`, so it's safe to re-run).
`StubCurrentUserProvider` authenticates every local request as the first of these. Once
Stage 5 lands, real users are additionally upserted on Google sign-in, keyed by their Google
subject ID.

## Useful commands

```bash
docker compose ps                 # service status + health
docker compose logs -f api        # tail the backend
docker compose down                # stop everything, keep the postgres volume
docker compose down -v             # stop everything and drop the postgres volume
```
