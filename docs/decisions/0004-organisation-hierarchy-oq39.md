# ADR-0004 — Organisation hierarchy (structure resolved; OQ-39 terminology still OPEN)

**Status:** **ACCEPTED (structure)** — team-approved at the CAMS architecture sign-off (2026-09-04) as **AD-04**.
**OQ-39 remains OPEN** for the *final naming/terminology* of the two upper levels ("LocalBody" vs "Village/Municipality").
**Date:** 2026-09-04 · **Review note:** the team approved a hierarchy with **two distinct upper levels** and a multi-local-body model; it did **not** finalise the labels, so OQ-39 stays OPEN and no terminology here is a final requirement. The approved chain, including the workflow tail, is `LocalBody → Village/Municipality → Ward → Asset → Complaint → Maintenance` — where "Maintenance" denotes the maintenance workflow/records (worker assignments, completion, maintenance-history) that hang off a Complaint, not a new org level.
**Relates to:** REQUIREMENTS.md D19, OQ-39, NFR-SCAL-001; DATA_MODEL.md §2, §6; team decision **AD-04**

## Context

D19 states the data structure must support
`LocalBody → Village/Municipality → Ward → Assets → Complaints`, be
multi-local-body capable, and never be hardcoded to one village. **OQ-39** asks
whether `LocalBody` and `Village/Municipality` are genuinely two conceptual levels
or effectively one (in Indian local governance a Gram Panchayat / municipality
*is* the local body).

## Decision

**Keep both levels as distinct entities, exactly as D19 lists them.**

- **LocalBody** — the governing administrative unit operating CAMS (a Gram
  Panchayat; a Nagar Palika / Municipal Council).
- **Village/Municipality** — a populated settlement unit administered by that
  LocalBody (a Panchayat may administer several revenue villages; a small
  municipality may have one such unit).
- **Ward** — the operational/electoral subdivision that assets and complaints are
  grouped by.

Supporting rules:
- Every scoped row (asset, complaint, …) carries a **denormalised `local_body_id`**
  for cheap whole-body queries (Officers see the whole body — D6).
- For a single-village body, the Admin creates **one** Village/Municipality record
  (named after the body); the app **may hide** that level in pickers when a
  LocalBody has exactly one — a presentation choice only; the data keeps the level.
- `ADMIN` is a **deployment-global** operator in MVP (creates local bodies, staff,
  categories); per-body admin scoping is a Future refinement (not required by D1).

## Alternatives considered

| Option | Why not |
|--------|---------|
| **Collapse LocalBody and Village/Municipality into one entity** | Loses the ability to model a Panchayat covering multiple villages without inventing fake wards; contradicts the explicit D19 list. Would be a **requirements change**, not a modelling simplification. |
| **Add levels above LocalBody** (Block/District/State) | Not in D19; not needed for a local-body-scoped system; pure speculation. Add later only if a requirement appears. |
| **Make Village/Municipality optional (nullable) between LocalBody and Ward** | Creates two code paths (with/without the level) for every scoped query and picker; more complexity than always having exactly one. |

## Consequences

**Positive**
- Matches D19 verbatim; no silent requirement change.
- Multi-village Panchayats and single-unit municipalities both model cleanly.
- Denormalised `local_body_id` keeps Officer-wide queries simple and fast
  regardless of the two intermediate levels (helps with the unknown OQ-14 scale).

**Negative / trade-offs**
- One extra table and one extra FK hop; a small amount of UI logic to hide a
  redundant level for single-village bodies.
- Admin does one extra setup step (create the single Village/Municipality) for a
  small Panchayat.

**Follow-up (OQ-39 still OPEN)**
- The **two-level structure** is approved (AD-04). The **final terminology** for the
  two upper levels is **not** settled — OQ-39 stays OPEN in REQUIREMENTS.md §15.3.
  Implementation may proceed against the structure using the working names
  `LocalBody` / `VillageOrMunicipality`; a later terminology decision is a rename,
  not a redesign.
- If the team ever concludes one level is genuinely redundant for all realistic
  deployments, that requires a superseding ADR **and** a REQUIREMENTS.md change to
  D19 — not a silent table drop.
