# SyncBoard

SyncBoard is an internal ticketing and coordination system for IT service company
management. It gives teams a single Kanban board for every open piece of work — support
requests, internal tasks, cross-team hand-offs — with clear ownership, an auditable status
history, and threaded discussion on each ticket, instead of the mix of chat threads,
spreadsheets and email that this kind of tracking usually ends up scattered across.

Each ticket carries a priority, an assignee, a due date and a set of links out to the tools
work actually happens in (Keka, Google Drive/Docs, GitHub, Slack, and others), so the board
stays the entry point without becoming a dead end. Status changes follow a fixed set of
legal transitions with built-in guardrails — you can't mark a ticket complete without an
assignee, or cancel or block one without a reason — and every change is recorded, broadcast
to everyone else looking at the board in real time, and reflected the moment you look back
at the ticket's history.

## Stack

| Layer | Technology | Version |
|---|---|---|
| Backend language / runtime | Java | 25 |
| Backend framework | Spring Boot | 4.1.1 |
| Database | PostgreSQL | 18 |
| Cache / realtime backing store | Valkey | 8 |
| Frontend framework | Angular | 22.1.5 |
| Frontend runtime (build/tooling) | Node.js | 22 |
| Build tools | Maven (backend), npm (frontend) | — |
| Containers | Docker / Docker Compose | — |

## Build sequence

SyncBoard is built in stages, each adding one layer of working functionality on top of the
last. This repository currently contains **Stage 0 — the skeleton**: full package/component
structure, compiling code, and every contract-defined name in place, with business logic
left as marked TODOs.

| Stage | What it introduces | Status |
|---|---|---|
| 0 | Skeleton — project scaffolding, shared contract, compiling structure for every layer | [x] |
| 1 | Java 25 domain core — status/priority enums, legal transition rules, framework-free | [ ] |
| 2 | REST API layer — DTOs, controllers, service interfaces | [ ] |
| 3 | PostgreSQL 18 persistence — Flyway schema, JPA entities and repositories | [ ] |
| 4 | Angular 22 frontend — app shell, Kanban board, ticket detail, feature screens | [ ] |
| 5 | Google Workspace SSO — OIDC login, hosted-domain restriction, user provisioning | [ ] |
| 6 | Cache + realtime — Valkey-backed board cache, STOMP/WebSocket broadcast | [ ] |
| 7 | GraphQL read API — schema-first read-only API alongside REST | [ ] |
| 8 | Infrastructure, CI/CD, documentation — Docker images, Compose, pipelines, docs | [ ] |

## Quickstart

```bash
# 1. Configure environment
cp .env.example .env
# edit .env if you need real Google OAuth credentials; the rest of the defaults work as-is

# 2. Start the database and cache
docker compose up -d postgres valkey

# 3. Run the API (Flyway migrates the schema on startup)
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 4. Run the web app, in another terminal
cd frontend
npm install
npm start
```

The web app runs at `http://localhost:4200`, proxying `/api`, `/graphql` and `/ws` to the
API at `http://localhost:8080`. To run the entire stack in containers instead
(`postgres`, `valkey`, `api`, `web`), use `docker compose up -d` — see
[`infra/local/README.md`](infra/local/README.md) for both options in detail, and
[`infra/aws/README.md`](infra/aws/README.md) for the production target topology.

## Repository layout

```
SyncBoard/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/syncboard/
│       │   ├── domain/        # Stage 1 — framework-free domain model & transition rules
│       │   ├── persistence/   # Stage 3 — JPA entities, repositories, Flyway-backed schema
│       │   ├── api/           # Stage 2 — DTOs, REST controllers, error handling
│       │   ├── service/       # Stage 2/3 — service layer, transition orchestration
│       │   ├── config/        # Stage 5/6 — security, OIDC, cache config
│       │   ├── realtime/      # Stage 6 — STOMP/WebSocket board events
│       │   └── graphql/       # Stage 7 — GraphQL read API
│       ├── main/resources/
│       │   ├── application.yml
│       │   ├── db/migration/  # Flyway SQL migrations
│       │   └── graphql/       # GraphQL schema
│       └── test/java/com/syncboard/
├── frontend/
│   ├── package.json
│   └── src/app/
│       ├── core/               # Stage 4 — signal stores, API clients, interceptors
│       ├── features/           # Stage 4 — Kanban board, ticket detail, my tasks, create
│       └── shared/              # Stage 4 — shared components, pipes, transition table
├── infra/
│   ├── aws/                    # ECS/RDS/ElastiCache target topology (prose + example task def)
│   └── local/                  # Local Docker Compose / dev workflow docs
├── docs/                       # Architecture, build stages, API and data model docs
├── .github/                    # CI, CodeQL, PR template, Dependabot
├── docker-compose.yml
└── .env.example
```

## Documentation

- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — layers, package map, and the end-to-end
  request path for moving a ticket across the board.
- [`docs/BUILD_STAGES.md`](docs/BUILD_STAGES.md) — what each build stage introduces and what
  must work at the end of it.
- [`docs/API.md`](docs/API.md) — the REST API reference: endpoints, payload shapes, error codes.
- [`docs/DATA_MODEL.md`](docs/DATA_MODEL.md) — the PostgreSQL schema and an entity-relationship
  diagram.
