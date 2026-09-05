# Decisions (ADRs)

This directory is the record of **approved** decisions. One decision per file,
named `NNNN-short-title.md` (e.g. `0001-backend-framework.md`), numbered
sequentially.

Sections per ADR: **Context · Decision · Alternatives considered · Consequences ·
Status · Date/review note**.

## Index — **ADRs 0001–0019 ACCEPTED (team-approved 2026-09-04)**

Two ADRs carry an explicitly-preserved OPEN requirements question (the *architectural
approach* is approved; the *requirements OQ* stays open): ADR-0004 (OQ-39 terminology),
ADR-0005 (OQ-38 residual detail). ADR-0007 keeps OQ-40 fully OPEN. ADR-0013 and
ADR-0019 carry an open sub-item (tile provider; scale numbers / OQ-14).

| ADR | Title | Team decision | OQ |
|-----|-------|---------------|-----|
| [0001](0001-backend-framework-and-build-tool.md) | Backend framework/runtime/build (Java 21 LTS + Spring Boot + Maven + Flyway + PostgreSQL) | AD-01 | — |
| [0002](0002-modular-monolith.md) | Modular monolith; service-interface boundaries; no microservices | AD-02 | — |
| [0003](0003-rest-json-api-style.md) | REST/JSON, `/api/v1`, action sub-resources for complaint transitions | AD-03 | — |
| [0004](0004-organisation-hierarchy-oq39.md) | Two-level org hierarchy (LocalBody + Village/Municipality) + Ward → Asset → Complaint → Maintenance | AD-04 | **OQ-39** (terminology OPEN) |
| [0005](0005-complaint-state-machine-oq38.md) | D7 lifecycle; `PENDING→REJECTED`; rework = `RESOLVED→IN_PROGRESS` + event + `returned_count`; no `RETURNED` status | AD-05 | **OQ-38** (residual OPEN) |
| [0006](0006-authentication-and-session.md) | Spring Security; **Argon2id** (BCrypt fallback); short JWT access + rotating refresh; server-side | AD-06 | — |
| [0007](0007-citizen-password-recovery-oq40.md) | Staff-assisted citizen reset = MVP default; no secure self-service claim | AD-08 | **OQ-40** (OPEN) |
| [0008](0008-media-storage.md) | Server filesystem/object-like media + DB metadata; upload controls; no mandatory AV | AD-09 | — |
| [0009](0009-notification-architecture.md) | In-app notifications persisted in PostgreSQL + client polling; no SMS/WhatsApp/email | AD-11 | — |
| [0010](0010-backup-and-restore.md) | Nightly PostgreSQL + media backup, ≥7-day retention, separate location, tested restore, MANIFEST/checksums | AD-18 | — |
| [0011](0011-reporting-pdf-excel.md) | Server-side reports; Apache POI (Excel) + OpenPDF/simple PDF; Complaint + Asset register | AD-13 | — |
| [0012](0012-authorization-rbac.md) | Server-side RBAC + local-body scoping + 3-layer worker isolation; UI is not a security boundary | AD-07 | — |
| [0013](0013-map-gis-approach.md) | Flutter OSM-based map; lat/long + Ward ID; no PostGIS/polygons; bounded marker queries | AD-10 | tile provider OPEN |
| [0014](0014-audit-trail.md) | Append-only `audit_entry`; actor/action/entity/time/note; in-transaction; Admin-query; no click logging | AD-12 | — |
| [0015](0015-analytics-and-charting.md) | Backend SQL aggregation endpoints; Flutter `fl_chart`; no separate analytics platform | AD-14 | — |
| [0016](0016-deferred-feature-scope-and-isolation.md) | Inventory / Budget / SLA = Secondary; AI = Future; isolation rules; worker-cost vs official-expenditure distinct | AD-15, AD-16, AD-17, AD-25 | **OQ-41** (SLA values OPEN) |
| [0017](0017-deployment-topology-and-connectivity.md) | Single host (app + PostgreSQL + media); HTTPS/TLS; no k8s; SPOF documented; MVP online-only | AD-19, AD-21 | **OQ-17** (availability OPEN) |
| [0018](0018-flutter-state-management-riverpod.md) | Riverpod for Flutter state management | AD-20 | — |
| [0019](0019-performance-and-scalability-baseline.md) | Pagination, indexes, pooling, bounded map queries, payload discipline; measurement plan | AD-23 | **OQ-14** (scale numbers OPEN) |

Not given a standalone ADR (covered elsewhere and simply baselined): **AD-22**
security baseline = `../architecture/SECURITY_ARCHITECTURE.md` (now APPROVED);
**AD-24** six-member ownership model = `../architecture/SYSTEM_ARCHITECTURE.md` §17.

**Status:** ADRs **0001–0019 are ACCEPTED** as of the 2026-09-04 architecture
sign-off and are recorded in `../../PLAN.md` §4. Preserved OPEN items:
**OQ-14, OQ-17, OQ-25, OQ-36, OQ-37, OQ-38 (residual), OQ-39 (terminology), OQ-40,
OQ-41** — plus the map tile-provider decision. Implementation is gated behind the
environment / prerequisite readiness step (`../../TASKS.md` §1).
