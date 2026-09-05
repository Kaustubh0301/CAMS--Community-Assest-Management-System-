# ADR-0017 — Deployment topology & connectivity

**Status:** **ACCEPTED** — team-approved at the CAMS architecture sign-off (2026-09-04) as **AD-19** and **AD-21**.
**Date:** 2026-09-04
**Relates to:** REQUIREMENTS.md D4, D36, NFR-OFF-001, NFR-COMPAT-*, NFR-SEC-003, OQ-17;
SYSTEM_ARCHITECTURE.md §10, §16; SECURITY_ARCHITECTURE.md §11

## Context

CAMS MVP is one Flutter app + one Spring Boot backend + one PostgreSQL database +
server-side media storage (ADR-0001, ADR-0002, ADR-0008). It will be demonstrated
for a DSN3099 evaluation using one selected local body. The team is six students.
Requirements: online-only MVP (D4); daily backup with tested restore (D36);
availability target **OPEN** (OQ-17).

## Decision

**Deployment (AD-19)**
- **Single host** running: the Spring Boot application (one executable JAR), the
  PostgreSQL server, and the media storage directory.
- **HTTPS/TLS** for any deployed (non-local-dev) environment; HTTP disabled or
  redirected. A thin reverse proxy in front of the app is optional (may terminate
  TLS); not required.
- **Environments:** `local` (each developer: local PostgreSQL + JAR) and `demo`
  (the shared host). No staging for MVP (add if time permits).
- Per-environment configuration via environment variables / an untracked
  `application-<env>.properties` (SECURITY_ARCHITECTURE §12).
- **No Kubernetes, no container orchestration, no microservices infrastructure, no
  load balancer/clustering** for MVP. Containerising the app with a simple
  `docker compose` (app + PostgreSQL) is an *optional* convenience, not a
  requirement.
- **Documented limitation:** the single host is a **single point of failure**. A
  crash needs a manual/scripted restart; data recovery is bounded by the last
  nightly backup (ADR-0010) and a manual restore.
- **Cheap robustness in MVP:** process auto-restart (systemd / Task Scheduler),
  `GET /health`, DB constraints, transactional writes, tested restore runbook.
- **Availability target stays OPEN (OQ-17)** — no uptime % / RPO / RTO number is
  set or invented. A standby DB or a supervised second instance can be added later
  **without redesign** if a target is agreed.

**Connectivity (AD-21)**
- **MVP is ONLINE-ONLY.** The app requires connectivity for all server operations;
  intermittent mobile data is handled gracefully (retry, clear offline-error
  states) but there is **no** local persistence, queueing, or background sync.
- **Offline operation and synchronization are Future scope** (D4, NFR-OFF-001) and
  are **not** designed here.

## Alternatives considered

| Option | Why not (for MVP) |
|--------|-------------------|
| **Kubernetes / container orchestration** | Operational complexity a student team can't run or explain; no scaling need proven (OQ-14). |
| **Separate hosts for app / DB / media** | More moving parts, more TLS links, more to back up; single host is sufficient for a scoped demo. |
| **Managed cloud database + object storage** | Accounts, cost, and network dependencies; `ADR-0008`'s `StorageService` seam already allows this later. |
| **Hot-standby / HA cluster now** | Premature without an availability target (OQ-17); addable later. |
| **Building offline support into MVP** | Explicitly Future (D4); a sync layer + conflict resolution is major scope. |

## Consequences

**Positive**
- One thing to deploy, secure, back up, and demo.
- Straightforward for the team to operate during development and at evaluation.
- Clear, honest statement of the SPOF limitation for the project report.

**Negative / trade-offs**
- **SPOF:** host/disk failure = downtime until manual recovery; RPO up to ~24 h
  (last nightly backup).
- No horizontal scaling path is exercised in MVP (not needed; OQ-14 OPEN).
- Online-only means the field app is unusable without connectivity — accepted per
  D4; revisit only if offline becomes an approved requirement.

**Follow-up**
- Choose the concrete demo host and record it (PLAN.md §3 "hosting/host").
- Obtain a real TLS certificate for the demo host before evaluation.
