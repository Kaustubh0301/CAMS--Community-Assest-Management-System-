# ADR-0014 — Audit trail

**Status:** **ACCEPTED** — team-approved at the CAMS architecture sign-off (2026-09-04) as **AD-12**.
**Date:** 2026-09-04
**Relates to:** REQUIREMENTS.md D22, FR-AUDIT-001..004, NFR-SEC-005;
SECURITY_ARCHITECTURE.md §9; DATA_MODEL.md §4.6

## Context

D22 requires an audit trail for important business/security actions, recording
actor, action, affected record, timestamp, and an optional note — and explicitly
**not** logging every UI click. The backend is a modular monolith with one
PostgreSQL database and transactional writes (ADR-0002).

## Decision

- A single **append-only `audit_entry`** table in PostgreSQL.
- **Audited actions** (D22): asset create/edit/decommission; complaint
  create/edit; priority changes; worker assignment/reassignment; complaint status
  transitions; verification; closure; rejection; and important account/settings
  changes (create/deactivate/delete account, role change, password reset, asset
  category change, local-body/ward change).
- **Each entry records:** `actor_id`, `actor_role`, `action`, `entity_type`,
  `entity_id`, optional `note`, `occurred_at` (server time), optional
  `request_id` for correlation.
- **Actor and timestamp come from the server** (auth principal + server clock),
  never from the request body.
- Written **in the same DB transaction** as the change it records: a rolled-back
  change leaves no audit; a committed change always has one. Complaint transitions
  additionally write `complaint_status_history` (the workflow timeline) — audit and
  history are complementary, both in-transaction.
- **No update or delete path** is exposed (no endpoint, no ORM cascade, no admin
  edit UI). Read/query is **Admin-only** in MVP (`GET /audit` with filters);
  Officer read of a subset is Secondary.
- **Not logged:** routine navigation, list/read requests, and UI button clicks
  that are not one of the actions above (D22).

## Alternatives considered

| Option | Why not (for MVP) |
|--------|-------------------|
| **Application log files as the audit trail** | Not queryable per record, easily rotated/lost, hard to show in a demo, mixes with debug noise. |
| **External audit service / log pipeline (ELK, etc.)** | Infrastructure to run and secure for no MVP benefit; a table is sufficient and self-contained. |
| **Event-sourcing the whole domain** | Large architectural commitment; the workflow doesn't need full event sourcing, only an audit + a complaint status history. |
| **Hash-chained / tamper-evident audit (`prev_hash`)** | Reasonable hardening; deferred to Secondary — the app exposes no tamper path and DB access is host-restricted. |
| **Log every request** | Explicitly excluded by D22; noisy and storage-heavy. |

## Consequences

**Positive**
- Strong attributability (NFR-SEC-005) with a trivial, explainable mechanism.
- In-transaction writes guarantee audit/data consistency.
- Queryable for the demo and for the final evaluation's traceability check.

**Negative / trade-offs**
- `audit_entry` is a fast-growing, append-only table; index `occurred_at`,
  `(entity_type, entity_id)`, `actor_id`; partitioning is a later option if
  OQ-14 shows it is needed.
- No cryptographic tamper-evidence in MVP (residual risk noted in
  SECURITY_ARCHITECTURE §18).
- Developers must remember to emit an audit entry for each new mutating action;
  centralise via the shared audit hook (SYSTEM_ARCHITECTURE §5, Area B) and a
  domain-event listener.
