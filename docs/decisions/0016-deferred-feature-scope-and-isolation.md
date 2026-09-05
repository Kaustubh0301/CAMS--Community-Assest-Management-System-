# ADR-0016 — Deferred feature scope & isolation (Inventory, Budget, SLA, AI)

**Status:** **ACCEPTED** — team-approved at the CAMS architecture sign-off (2026-09-04) as **AD-15, AD-16, AD-17, AD-25**.
**Date:** 2026-09-04
**Relates to:** REQUIREMENTS.md §13 (Secondary), §14 (Future), D29–D34, OQ-41;
SYSTEM_ARCHITECTURE.md §5, §21; DATA_MODEL.md §9

## Context

Requirements v1.0 classifies Inventory, Budget/Expenditure, and SLA reporting as
**Secondary**, and all AI features as **Future**. The MVP is the core
asset → complaint → maintenance workflow. These deferred areas must not delay or
complicate the MVP, but must be addable later without reshaping MVP data.

## Decision

**Inventory (AD-15 — Secondary)**
- Design target: **one central store**. Multiple stores are Future.
- Own tables (`inventory_item`, `stock_movement`); references core rows (complaint,
  asset) **by id only**.
- Must not be a dependency of the MVP asset/complaint/maintenance flow — a Worker
  can complete a repair (record cost + completion photo) with the inventory module
  disabled.

**Budget (AD-16 — Secondary)**
- Conceptual model: **allocated budget**, **actual expenditure**, **remaining
  budget** (remaining is computed, not stored on MVP tables).
- **Worker-entered repair cost** (captured in MVP on the maintenance record) and
  **Officer-confirmed/recorded official expenditure** remain **conceptually
  distinct**: the worker figure is field-recorded actuals; the officer figure is
  the categorised, confirmed expenditure entry.
- Own tables (`expenditure_entry`, `budget_allocation`); id-only references. Not
  promoted into MVP.

**SLA (AD-17 — Secondary, reporting-only)**
- SLA is **reporting-only**: configurable target time per priority; target-vs-actual
  and within/over reporting derived from `complaint_status_history`.
- **No automatic escalation** in MVP (escalation is Future — FR-MAINT-012).
- **Exact SLA target values stay OPEN under OQ-41** — none are invented here.
- Own table (`sla_target`); no column added to `complaint`.

**AI (AD-25 — Future, non-foundational)**
- **The core CAMS workflow is fully independent of AI.** No MVP path calls an AI
  component.
- Possible Future/Secondary AI: complaint summarization, priority suggestion,
  duplicate-complaint detection, predictive maintenance, budget forecasting,
  worker recommendation.
- If ever added, AI features attach as **advisory** consumers of existing data
  (read-only suggestions an Officer may accept/ignore) and follow the same
  isolation rule (own module, id-only references, feature-flagged). AI must never
  block or gate a core transition (e.g. an Officer can always set priority manually
  per D9).

**Common isolation guarantees (all four):**
1. No MVP table gains a column for these features.
2. Core modules never import the deferred modules' packages.
3. Feature flags keep the tables uncreated / endpoints unrouted until enabled.
4. Communication is via published service interfaces or id references only.

## Alternatives considered

| Option | Why not |
|--------|---------|
| **Build Inventory/Budget into the MVP** | Requirements class them Secondary; adding them now widens MVP scope and risks the core workflow not finishing. |
| **Model expenditure as a single "cost" field shared by worker and officer** | Conflates a field-recorded actual with a confirmed, categorised financial record; AD-16 requires them distinct. |
| **Implement SLA escalation now** | Excluded by D34; needs timers/scheduling and a notification policy that is Future scope. |
| **Design AI hooks into core transitions now** | Makes the core depend on a Future capability; violates "AI must not block the core MVP" (AD-25). |
| **Invent SLA target numbers to be able to build the feature** | Prohibited — OQ-41 is OPEN; real targets come from the local body. |

## Consequences

**Positive**
- MVP scope stays tight and buildable by six students in the time available.
- Each deferred feature is a later additive change (new package + new tables +
  flag), satisfying NFR-MAINT-003.
- The worker-cost vs official-expenditure distinction is preserved for when Budget
  is built.

**Negative / trade-offs**
- Some duplicated-looking data later (worker cost on the maintenance record *and* an
  expenditure entry) — intentional, per AD-16; reconciliation is a Budget-module
  concern.
- Feature-flag plumbing and an architecture test to enforce "no core → deferred
  imports" are small upfront costs.
- OQ-41 remains OPEN; the SLA feature cannot be finished until target values exist.
