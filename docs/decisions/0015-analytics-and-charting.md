# ADR-0015 — Analytics computation & charting

**Status:** **ACCEPTED** — team-approved at the CAMS architecture sign-off (2026-09-04) as **AD-14**.
**Date:** 2026-09-04
**Relates to:** REQUIREMENTS.md FR-ANLY-001..002 (MVP), FR-ANLY-003..007 (Secondary), D28, D32;
SYSTEM_ARCHITECTURE.md §13; API_ARCHITECTURE.md §4.10; ADR-0011 (reporting)

## Context

MVP analytics are on-screen counts: assets by status & condition; complaints by
status/ward/category/priority (FR-ANLY-001/002). Workers see only their own metrics
(D28). Advanced analytics (resolution time, cost, worker-performance metrics per
D32, trends) are Secondary. The client is Flutter; the backend is the modular
monolith over PostgreSQL.

## Decision

- **Compute analytics with backend database queries / SQL aggregation** exposed as
  small JSON endpoints (`GET /analytics/asset-summary`,
  `/analytics/complaint-summary`, `/analytics/my-metrics`).
- **Reuse the same read queries/DTOs as the reports** (ADR-0011) so a number on a
  screen and the same number in a PDF/Excel always match.
- **No separate analytics platform / warehouse / OLAP engine / BI tool.**
- **Flutter renders** the metrics and charts using **`fl_chart`** (or an equally
  lightweight, actively-maintained Flutter charting package if the team finds a
  blocker); charts consume the JSON endpoints directly.
- **Scoping:** analytics endpoints are Officer/Admin for local-body-wide figures;
  `my-metrics` returns only the calling Worker's own figures (D28) and computes the
  D32 metric set (assigned, completed, average completion time, tasks returned,
  average citizen rating) as **factual derivations** from
  `complaint_status_history` / `worker_assignment` / `feedback` — no subjective
  score.
- Advanced analytics (Secondary) add more endpoints/queries over the same tables;
  no architectural change.

## Alternatives considered

| Option | Why not (for MVP) |
|--------|-------------------|
| **Dedicated analytics DB / warehouse (e.g. a star schema, ClickHouse, BigQuery)** | Another datastore + ETL to build, secure, back up, and explain; unjustified at unknown/modest scale (OQ-14). PostgreSQL aggregation is ample. |
| **Embedded BI tool (Metabase/Superset)** | A second app to deploy and secure; the requirement is a few counts in the mobile app, not self-serve BI. |
| **Pre-computed/materialised aggregate tables** | Premature optimisation; add materialised views only if measurements show slow queries. |
| **Client-side aggregation from raw data** | Would ship large datasets to the device and duplicate logic; breaks worker isolation cleanly done server-side. |
| **A different chart library (e.g. `syncfusion_flutter_charts`)** | Fine functionally; `fl_chart` is lightweight, permissively licensed, and widely used — preferred unless a concrete limitation appears. |

## Consequences

**Positive**
- Zero new infrastructure; one datastore, one query layer shared with reports.
- Consistent numbers across screen and exports.
- Worker isolation and D32's "factual metrics, not scores" fall out naturally from
  server-side SQL.

**Negative / trade-offs**
- Heavy aggregate queries could be slow at unknown scale (OQ-14); mitigations —
  required filters, indexes (ADR-0019), and materialised views later if needed.
- `fl_chart` limits some advanced chart types; acceptable for the MVP metric set,
  revisit for Secondary analytics if richer visuals are wanted.
