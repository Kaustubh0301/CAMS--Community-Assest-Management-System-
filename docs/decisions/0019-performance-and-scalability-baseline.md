# ADR-0019 — Performance & scalability baseline

**Status:** **ACCEPTED (techniques)** — team-approved at the CAMS architecture sign-off (2026-09-04) as **AD-23**.
**OQ-14 remains OPEN** — no maximum-user / maximum-asset / response-time numbers are set or invented.
**Date:** 2026-09-04
**Relates to:** REQUIREMENTS.md NFR-PERF-*, NFR-SCAL-*, OQ-14; SYSTEM_ARCHITECTURE.md §15;
DATA_MODEL.md §11; ADR-0013 (bounded map queries)

## Context

Expected scale is undefined (OQ-14 OPEN) and must not be invented. The architecture
should still bake in the low-cost practices that keep a modular monolith over
PostgreSQL responsive at plausible local-body volumes, and should identify what to
measure once a real target exists.

## Decision

Adopt these as **baseline engineering practices** for MVP (no numeric targets):

- **Pagination** on every list endpoint (`page`/`size`, `size` capped by config);
  no unbounded result sets.
- **Database indexes** on foreign keys and on the columns used for filtering/
  sorting (DATA_MODEL §11) — e.g. `complaint(local_body_id, status, ward_id,
  priority)`, `worker_assignment(worker_id, active)`,
  `asset(local_body_id, ward_id, category_id, status)` and `(latitude, longitude)`,
  `notification(recipient_id, read_at)`, `audit_entry(occurred_at)`.
- **Efficient queries:** avoid N+1 (explicit fetch joins / batch), select only
  needed columns for list/marker DTOs, use set-based aggregation for analytics.
- **Connection pooling** (the framework default pool, e.g. HikariCP) with a modest
  pool size from config.
- **Bounded map queries:** the marker endpoint is always bounding-box constrained
  and `maxMarkers`-capped (ADR-0013).
- **Appropriate API payload sizes:** small DTOs for lists/markers; full detail only
  on `/{id}`; request-size caps (SECURITY_ARCHITECTURE §6); client-side image
  compression before upload (ADR-0008).
- **Synchronous** report/analytics generation for MVP, with required filters and a
  row cap + "narrow your filters" message; a job-based async path is a documented
  **non-breaking** later addition.

**Measurement plan (activate when a scale target is set):**
- p50/p95 latency for: complaint list, asset map query, asset detail, photo upload,
  each report.
- Row growth over time: `complaint`, `complaint_status_history`, `media_object`,
  `audit_entry`.
- Media volume growth (MB/week), average stored image size after compression.
- DB size and slowest queries (`pg_stat_statements`).
- Seed synthetic datasets at team-chosen trial sizes (documented as **assumptions,
  not approved requirements**) and record the above; feed results back to close
  OQ-14.

## Alternatives considered

| Option | Why not now |
|--------|-------------|
| **Pick capacity numbers and design to them** | Prohibited — OQ-14 is OPEN; numbers would be fabricated. |
| **Add caching (Redis) / read replicas / partitioning upfront** | Premature optimisation without a target or measurements; each adds infra to run and explain. Addable later without redesign. |
| **Materialised views for analytics from day one** | Only justified if measurement shows slow aggregates; keep plain queries until then. |
| **No pagination / no caps ("it's a small demo")** | A single bad query or a large ward could still degrade the demo; caps are cheap insurance. |

## Consequences

**Positive**
- Sensible performance behaviour at plausible volumes without guessing a target.
- A concrete measurement plan makes OQ-14 answerable with data, not assumptions.
- All heavier options (cache, replica, partitioning, async reports) remain
  additive.

**Negative / trade-offs**
- Some engineering effort (indexes, pagination, DTO discipline) is spent before a
  proven need — judged worthwhile and low-cost.
- Without a target, "fast enough" is a judgement call until measurements exist.
- Synthetic-load results must be clearly labelled as assumptions so they are not
  mistaken for an approved SLA.
