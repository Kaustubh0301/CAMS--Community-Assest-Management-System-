# ADR-0002 — Backend architecture style: modular monolith

**Status:** **ACCEPTED** — team-approved at the CAMS architecture sign-off (2026-09-04) as **AD-02**.
**Date:** 2026-09-04 · **Review note:** approved as written. One Spring Boot deployable; modules communicate through defined service interfaces / domain boundaries — no uncontrolled cross-module table access; no microservices.
**Relates to:** SYSTEM_ARCHITECTURE.md §4–§5; REQUIREMENTS.md §13–§14, NFR-MAINT-003; team decision **AD-02**

## Context

CAMS MVP is a single connected workflow (asset → complaint → assign → repair →
verify → close → history → rate) plus supporting concerns (auth, notifications,
audit, reports, media). Load is unknown (OQ-14 OPEN). The build team is six
students over ~one year who must also *explain* the system at a DSN3099
evaluation. Secondary modules (Inventory, Budget, SLA) and Future features
(offline, QR, AI, web dashboard) must be addable **without redesigning the MVP**
(NFR-MAINT-003).

## Decision

Build the backend as a **modular monolith**:

- **One deployable** (one Spring Boot JAR), **one PostgreSQL database**.
- Code organised into **modules** with clear ownership
  (SYSTEM_ARCHITECTURE §5): Auth/User, LocalBody/Ward, AssetCategory, Asset,
  Complaint, Maintenance, Feedback, Notification, Audit, Reporting/Analytics,
  Media; plus **isolated Secondary** modules Inventory, Budget/Expenditure, SLA.
- **Module boundary rule:** a module may call another module's **published service
  interface**; it may **not** read or write another module's tables. Reporting/
  Analytics is the single read-only exception (dedicated read queries/views).
- **In-process domain events** (synchronous, same transaction) let Complaint/
  Maintenance trigger Notification/Audit without a broker.
- Secondary modules are **compiled in but feature-flagged off** for MVP; their
  tables are not created until enabled; they reference core rows **by id only**.

## Alternatives considered

| Option | Why not |
|--------|---------|
| **Microservices** (separate Asset / Complaint / Auth / Reporting services) | Adds inter-service network calls, distributed transactions or sagas, service discovery, multiple deploy units, cross-service auth, and aggregated logging/tracing — all of which the team must build, secure, test, and explain, for **zero MVP functional benefit** and unknown/likely-modest load. High risk of not finishing. |
| **Serverless / functions** | Cold starts, local dev friction, per-function IAM, and stitching a stateful workflow across functions; poor fit for a single-host demo and a student team. |
| **"Big ball of mud" monolith** (no module boundaries) | Simplest to start, but Secondary/Future additions and parallel work by six people would create merge pain and coupling; fails NFR-MAINT-003 in practice. |
| **Modular monolith with separate schemas per module** | Reasonable, and compatible with this ADR; deferred as a physical-schema decision. MVP can use one schema with a table-naming prefix per module. |

## Consequences

**Positive**
- One thing to run, debug, test, back up, and demo.
- One database → simple ACID transactions across the workflow; no eventual
  consistency to reason about.
- Clear module ownership maps directly to the team-of-six split
  (SYSTEM_ARCHITECTURE §17).
- Secondary/Future features are additive (new package + new tables + flag),
  satisfying NFR-MAINT-003.
- Straightforward path to extract a service later *if* real scale ever demands it
  (module interfaces are already the seams).

**Negative / trade-offs**
- Discipline required: without review, modules can start touching each other's
  tables. Mitigation: the boundary rule is checked in code review / CI (e.g. an
  ArchUnit test) — a task in `TASKS.md` §2.
- One process = one failure domain (availability — OQ-17). Mitigation:
  transactional writes, health check, auto-restart; standby addable without
  redesign.
- A large single codebase needs good package hygiene and a shared build.

**Neutral**
- Physical schema layout (one schema vs schema-per-module) is left to data-model /
  migration design.
