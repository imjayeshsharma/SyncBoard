# AWS target topology

This describes the production topology SyncBoard is designed to run on. It is a **target**,
not something wired up by this repository yet — no Terraform/CDK/CloudFormation ships here.
**Docker Compose on a single host (see `docker-compose.yml` and `infra/local/README.md`) is
the sanctioned fallback** for any environment that does not need this — local development,
demos, or a small internal deployment.

## Components

| Layer | Service | Notes |
|---|---|---|
| Frontend | **ECS Fargate** service running the `frontend/Dockerfile` image (nginx + static Angular build) | Fronted by an Application Load Balancer; serves the SPA and proxies `/api`, `/graphql`, `/ws` to the API service's internal ALB listener/target group |
| Backend | **ECS Fargate** service running the `backend/Dockerfile` image (Spring Boot) | Registered against its own ALB target group; health check on `/actuator/health` |
| Database | **RDS for PostgreSQL 18**, Multi-AZ | Flyway migrations run on API container startup; connection details injected via `SYNCBOARD_DB_URL`/`SYNCBOARD_DB_USER`/`SYNCBOARD_DB_PASSWORD` |
| Cache / pub-sub | **ElastiCache (Valkey 8, Redis-protocol compatible)**, replication group | Backs the board response cache and, longer-term, cross-instance STOMP fan-out; `SYNCBOARD_REDIS_HOST`/`SYNCBOARD_REDIS_PORT` |
| Secrets | **AWS Secrets Manager** | `GOOGLE_CLIENT_ID`/`GOOGLE_CLIENT_SECRET`/`SYNCBOARD_DB_PASSWORD` injected into the ECS task as secrets, never as plain task-definition environment values |
| Images | **ECR** | One repository per image (`syncboard-api`, `syncboard-web`); the `docker` job in `.github/workflows/ci.yml` builds both — pushing to ECR is a deploy-time concern, deliberately not wired into CI yet |
| Networking | **VPC**, private subnets for ECS tasks/RDS/ElastiCache, public subnets for the ALB only | API and web tasks talk to each other and to RDS/ElastiCache over private networking |
| Observability | **CloudWatch Logs** (container stdout/stderr), **CloudWatch Container Insights** | The Spring Boot Actuator health/info endpoints back the ECS task health check |

## Request path

```
Browser → ALB (public) → web (ECS/Fargate, nginx) → /api,/graphql,/ws
                                                        │
                                                        ▼
                                          ALB (internal) → api (ECS/Fargate, Spring Boot)
                                                        │
                                          ┌─────────────┼─────────────┐
                                          ▼             ▼
                                   RDS Postgres 18   ElastiCache (Valkey 8)
```

## Task definition sketch

`infra/aws/task-definition.example.json` sketches the API service's ECS task definition:
Fargate launch type, the container image reference (`<account>.dkr.ecr.<region>.amazonaws.com/syncboard-api:<tag>`),
port mapping on 8080, a container health check against `/actuator/health`, `secrets` entries
for credentials, plain `environment` entries for non-sensitive configuration (mirroring
`.env.example`), and a `logConfiguration` sending output to CloudWatch Logs. It is illustrative
only — task role ARNs, log group names, and the ECR account/region are placeholders.

## Explicitly out of scope here

Actual Terraform/CDK, VPC/subnet CIDR planning, IAM policy documents, autoscaling policies,
blue/green or canary deploy configuration, and CI→ECR push wiring. All land alongside a real
AWS account, not in this skeleton.
