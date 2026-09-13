# Architecture

**What goes here:** system overview, component / module boundaries, data flow,
deployment shape, and cross-cutting concerns (authentication, error handling,
logging). Individual technology choices are recorded as ADRs in `../decisions/` and
summarised here.

## Contents (v1.0 — **TEAM-APPROVED**, 2026-09-04)

| Document | Covers |
|----------|--------|
| [`SYSTEM_ARCHITECTURE.md`](SYSTEM_ARCHITECTURE.md) | §1A decision register **AD-01…AD-25**; context & container views; modular-monolith style; module map; runtime flows; map/notifications/reporting/backup; performance & availability (**OQ-14/OQ-17 OPEN**); six-member ownership model (AD-24); dev prerequisites; requirements-coverage + validation. |
| [`DATA_MODEL.md`](DATA_MODEL.md) | Conceptual/logical ER model (no SQL); AD-04 two-level hierarchy (**OQ-39 terminology OPEN**); entity catalog; enumerations; Secondary-entity isolation; retention (D18). |
| [`COMPLAINT_STATE_MACHINE.md`](COMPLAINT_STATE_MACHINE.md) | The D7 lifecycle; AD-05 rework-as-event, no `RETURNED` state (**OQ-38 residual OPEN**); per-transition actor / guard / data / notification / audit. |
| [`API_ARCHITECTURE.md`](API_ARCHITECTURE.md) | AD-03 REST/JSON contract at endpoint level; AD-07 authZ model; endpoint catalog by module; error codes; non-goals. |
| [`SECURITY_ARCHITECTURE.md`](SECURITY_ARCHITECTURE.md) | AD-22 security baseline (authn/authz, Argon2id, JWT, uploads, injection, audit integrity, PII, transport, secrets, backups); residual risks; accepted no-AV limitation. |

ADRs for the significant decisions are in [`../decisions/`](../decisions/)
(**0001–0019, all ACCEPTED**; index in its README).

**Status:** architecture **TEAM-APPROVED (2026-09-04)**. Technology decisions are
recorded in `../../PLAN.md` §4. Requirements `../requirements/REQUIREMENTS.md` v1.0
remains the source of truth. **Implementation has not started** — it is gated
behind the environment / prerequisite readiness step (`../../TASKS.md` §1).
Preserved OPEN: OQ-14, OQ-17, OQ-25, OQ-36, OQ-37, OQ-38 (residual), OQ-39
(terminology), OQ-40, OQ-41, **OQ-42** (citizen recovery request persistence —
see `../decisions/README.md`), plus the map tile-provider decision.
