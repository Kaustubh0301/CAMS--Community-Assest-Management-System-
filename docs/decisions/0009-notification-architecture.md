# ADR-0009 — Notification architecture

**Status:** **ACCEPTED** — team-approved at the CAMS architecture sign-off (2026-09-04) as **AD-11**.
**Date:** 2026-09-04 · **Review note:** approved as written. MVP = in-app notifications only; records persisted in PostgreSQL; client polling is acceptable for MVP. No SMS, WhatsApp, or email in the MVP architecture. OS push stays Future.
**Relates to:** REQUIREMENTS.md D10, D20, FR-NOTIF-001..003, FR-NOTIF-005 (Future);
SYSTEM_ARCHITECTURE.md §12; COMPLAINT_STATE_MACHINE.md §3; team decision **AD-11**

## Context

MVP notifications are **in-app only** (D10): no SMS, WhatsApp, or email. External
channels and OS push are Future (FR-NOTIF-005). Notifications are triggered by
complaint-workflow events (submit, assign/reassign, start, resolve, return, verify,
close, reject). The backend is a modular monolith with in-process domain events
(ADR-0002).

## Decision

- **Model:** a `notification` row per recipient per event —
  `(id, recipient_id, type, title_key, body_params, related_entity_type,
  related_entity_id, created_at, read_at)`. Text is **not** stored; `type` +
  `title_key` + `body_params` let the app render EN/HI (D12).
- **Creation:** synchronous, **inside the same DB transaction** as the triggering
  state change, via a `NotificationService.notify(...)` call from the Complaint/
  Maintenance module (or a synchronous domain-event listener). If the transaction
  rolls back, no notification is created; if it commits, the notifications exist.
- **Recipients per event:** defined in SYSTEM_ARCHITECTURE §12 / state-machine §3
  (e.g. submit → all Officers of the local body; assign → the Worker; start →
  the Citizen; resolve → Officer + Citizen; return → Worker; reject/verify/close →
  Citizen).
- **Delivery:** the app **polls** `GET /notifications/unread-count` on a modest
  interval and `GET /notifications` when the user opens the bell; `POST
  /notifications/{id}/read` and `/read-all`.
- **No** WebSocket/SSE/push, **no** subscription/registration endpoints, **no**
  external channel adapters in MVP.

## Alternatives considered

| Option | Why not (for MVP) |
|--------|-------------------|
| **OS push (FCM)** | FCM is a *different mechanism* from the approved "in-app" requirement (D10) and needs a Google project, device-token registration, and delivery handling. It is **Future** (FR-NOTIF-005). Would also arguably need a D10 change. |
| **WebSocket / SSE for live updates** | Real-time delivery infra (connection management, auth on the socket, reconnection) for marginal UX gain at this scale; polling is sufficient and trivial to reason about. |
| **Message broker / outbox pattern** | Needed only when notification delivery is asynchronous or cross-service. In a single monolith with a single DB, a same-transaction insert is simpler and strictly consistent. |
| **Email/SMS "for important ones only"** | Explicitly excluded by D10/D20; external dependency. |
| **Compute notifications on read (no `notification` table)** | Harder to track read/unread per user and to show a history; a table is simpler. |

## Consequences

**Positive**
- Dead simple, fully consistent with the workflow transaction, and easy to test.
- No external accounts, keys, or services (aligns with D20).
- `notification` rows are the natural substrate for a Future push channel — adding
  FCM later is an **additive delivery adapter** over the same rows (plus its own
  decision/possible D10 change).
- i18n-ready (codes + params, not stored text).

**Negative / trade-offs**
- **Latency:** notifications appear on the next poll, not instantly. Acceptable for
  this workflow (nothing is time-critical to the second).
- **Polling load** scales with active users × interval; bounded by a sane interval
  and a small payload, and revisited under OQ-14. The unread-count endpoint is
  cheap (indexed `recipient_id, read_at`).
- Table growth over time; old read notifications can be pruned by housekeeping if
  needed (not user data).

**Follow-up**
- Fix the poll interval and any client backoff during implementation.
- If the team later wants push, write a new ADR and check it against D10.
