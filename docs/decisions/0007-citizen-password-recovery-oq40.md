# ADR-0007 — Citizen password recovery (MVP default approved; OQ-40 stays OPEN)

**Status:** **ACCEPTED (MVP default)** — team-approved at the CAMS architecture sign-off (2026-09-04) as **AD-08**: **staff-assisted citizen password reset** is the safe MVP default.
**OQ-40 remains explicitly OPEN.** The team did **not** adopt a self-service mechanism; secure self-service recovery is **not** claimed solved, because it requires a trusted verification channel that D16/D21 currently exclude.
**Date:** 2026-09-04 · **Review note:** implement the staff-assisted path for MVP; keep the `CITIZEN_RECOVERY` table + `/auth/citizen/recovery/*` endpoints shaped so an approved option can be switched on later without redesign.
**Relates to:** REQUIREMENTS.md D16, D21, NFR-SEC-007, OQ-40;
SECURITY_ARCHITECTURE.md §5; team decision **AD-08**

## Context

D21 says citizen password recovery must be **"basic and self-contained without
external OTP/email infrastructure"** in MVP; automated external recovery is Future.
D16 excludes SMS/OTP and Aadhaar. Citizens authenticate with **mobile number +
password** (D16).

**Security reality:** a *self-service* password reset is only as trustworthy as the
**recovery channel** that proves the requester owns the account. With SMS and email
excluded, there is **no trusted channel** for pure self-service. Any "reset by
knowing the mobile number + something guessable" is weak (mobile numbers are not
secret). We must not present a weak mechanism as secure.

## Decision

**Ship the safe default now; put the rest to the team.**

**Default (implemented unless the team decides otherwise): staff-assisted reset.**
- The citizen contacts the local-body office (in person / phone).
- An Officer or Admin verifies identity by local knowledge or an ID shown in
  person, then triggers a reset:
  `POST /admin/citizen-recovery/{requestId}/resolve` → system-generated temporary
  password handed to the citizen; the citizen must change it on next login.
- The event is **audited** (`PASSWORD_RESET`); the citizen's sessions are revoked.
- `POST /auth/citizen/recovery/request` (public) only records a pending request and
  returns a generic `202` (no account enumeration).
- This is "self-contained" (no external infra); its security rests on **human
  verification**.

**Optional supplement (off by default): one-time recovery code.**
- At registration the app shows a single recovery code the citizen must save
  (entity `CITIZEN_RECOVERY`, stored hashed).
- `mobile + recovery code` → set a new password; code is single-use, rate-limited.
- Clearly **lower assurance** (codes get lost, photographed, shared). Enabled only
  if the team accepts that trade-off for convenience.

## Alternatives considered (options for the team)

| Option | Security | Infra | Note |
|--------|----------|-------|------|
| **a) Staff-assisted only** *(default)* | Good (human check) | None | Slight burden on office; fine for a local body. **Recommended for MVP.** |
| **b) Staff-assisted + recovery code** | Mixed | None | Adds a self-service path at lower assurance. |
| **c) Security questions** | Weak | None | Answers are guessable/searchable; not recommended as a sole factor. |
| **d) SMS OTP** | Good | **SMS gateway (external)** | **Changes D16/D21.** Record a requirements change if chosen. |
| **e) Email reset** | Good if email is reliable | Mail service (external) | Many rural citizens may lack email; also a change to the "no external infra" intent. |

## Consequences

**Positive**
- MVP has a working, honestly-described recovery path (option a) with **no**
  external dependency and **no** security overclaim.
- The `CITIZEN_RECOVERY` table and the `/auth/citizen/recovery/*` endpoints are
  shaped so option (b) can be switched on later without redesign.

**Negative / trade-offs**
- Option (a) is not "click a link and reset yourself" — it needs office contact.
  For a locally-deployed system this is acceptable and arguably appropriate.
- If the team wants true self-service, that requires option (d) or (e) and a
  **REQUIREMENTS.md change** to D16/D21 — flagged, not decided here.

**Status of OQ-40 (2026-09-04 architecture sign-off):** **OQ-40 stays OPEN.** The
team approved **staff-assisted reset as the MVP default (AD-08)** and explicitly did
**not** close OQ-40: whether to also enable the recovery-code supplement, or to
change D16/D21 to allow an SMS/email channel, remains an open requirements decision
tracked in REQUIREMENTS.md §15.3 / PLAN.md §5. No secure self-service claim is made.
