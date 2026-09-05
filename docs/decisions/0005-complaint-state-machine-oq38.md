# ADR-0005 — Complaint state machine & rework representation (approach approved; OQ-38 residual OPEN)

**Status:** **ACCEPTED (approach)** — team-approved at the CAMS architecture sign-off (2026-09-04) as **AD-05**.
**OQ-38 remains OPEN** for any residual rework detail *beyond* the approved architectural approach (e.g. a returned-reason taxonomy, whether the UI surfaces a distinct "returned, awaiting rework" label).
**Date:** 2026-09-04 · **Review note:** the team approved — primary lifecycle exactly as D7; `PENDING → REJECTED` alternative; **no `RETURNED` status**; a returned resolved complaint goes `RESOLVED → IN_PROGRESS` and the return is recorded as a meaningful history/event with an auditable `returned_count`. No additional complaint states are to be invented.
**Relates to:** REQUIREMENTS.md D7, D32, FR-COMP-006, FR-MAINT-001..010, OQ-38;
COMPLAINT_STATE_MACHINE.md; team decision **AD-05**

## Context

D7 fixes the complaint lifecycle as
`PENDING → ASSIGNED → IN_PROGRESS → RESOLVED → VERIFIED → CLOSED`, with `REJECTED`
as an alternative outcome during review. Separately, an Officer verifying a
`RESOLVED` complaint may find the work unacceptable and **send it back for rework**.
D32 lists **"tasks returned"** as a worker metric. **OQ-38:** should "returned" be
a real state (`RETURNED`), or an event while going back to `IN_PROGRESS`?

## Decision

1. **Use exactly the D7 state set. Do not add a `RETURNED` state.**
2. Model an Officer sending `RESOLVED` work back as a **transition
   `RESOLVED → IN_PROGRESS`** carrying:
   - a `complaint_status_history` row with `event_type = RETURN` and the reason;
   - `complaint.returned_count += 1`;
   - an in-app notification to the assigned Worker;
   - an audit entry (`COMPLAINT_STATUS_CHANGE`).
3. The worker metric **"tasks returned"** is derived from `RETURN` events /
   `returned_count` — no dedicated state needed.
4. No other business states are introduced. `VERIFY` and `CLOSE` remain distinct
   transitions (may be one UI action) so "verified but not yet closed" is
   representable.

## Alternatives considered

| Option | Why not |
|--------|---------|
| **Add a `RETURNED` state** between `RESOLVED` and `IN_PROGRESS` | It behaves almost identically to `IN_PROGRESS` (worker is doing the repair). It expands the D7 set (a requirements deviation), adds transitions, and complicates every status filter/label/i18n for negligible information gain over a `RETURN` event + counter. |
| **Reuse `ASSIGNED` for returned work** | Loses the fact that the worker already started; misrepresents the timeline; would need a flag anyway. |
| **Only an event, no `returned_count`** | Works, but a counter on the complaint makes the common "how many times returned" query and the D32 metric trivial and cheap. Keep both (event = detail, counter = fast aggregate). |
| **A generic `RETURNED` boolean flag on the complaint** | The counter is strictly more informative and equally simple. |

## Consequences

**Positive**
- The implemented state set matches D7 **exactly** — easy to validate against the
  requirement, and consistent across API, DB, UI labels, and i18n.
- Rework history is fully captured (who returned it, when, why) via
  `complaint_status_history`.
- D32 "tasks returned" is a one-line derivation.
- Fewer states → simpler state-transition guard table and fewer test permutations.

**Negative / trade-offs**
- A UI that wants to *show* "returned, awaiting rework" must render it from
  `status = IN_PROGRESS AND last event = RETURN` (or `returned_count > 0` in the
  current cycle) rather than a distinct status value. This is a small
  presentation-layer rule, documented in COMPLAINT_STATE_MACHINE.md.
- `returned_count` is denormalised state that must be updated in the same
  transaction as the `RETURN` event (already the pattern for all transitions).

**Follow-up (OQ-38 residual, still OPEN)**
- The architectural approach is approved. Remaining detail tracked under OQ-38:
  a return-reason taxonomy (free text vs a small enum), and whether the UI shows a
  distinct "returned, awaiting rework" indicator. These are additive and do not
  change the state set.
- If evaluators/users specifically want a first-class "Returned" *status*, that is
  a D7 change to record — not a silent addition.
