# ADR-0012 — Authorization: server-side RBAC + scoping

**Status:** **ACCEPTED** — team-approved at the CAMS architecture sign-off (2026-09-04) as **AD-07**.
**Date:** 2026-09-04
**Relates to:** REQUIREMENTS.md D1, D6, D28, NFR-SEC-001; SECURITY_ARCHITECTURE.md §4;
API_ARCHITECTURE.md §3; ADR-0006 (authentication)

## Context

CAMS has four roles (ADMIN, OFFICER, WORKER, CITIZEN — D1). Officers manage the
whole local body (D6); Workers may see only their own assigned work (D28); Citizens
see only their own complaints. The client is a Flutter app whose UI cannot be
trusted as a security boundary. Authentication is covered by ADR-0006; this ADR
covers **authorization** (what an authenticated principal may do).

## Decision

- **Role-based access control, enforced entirely server-side.** Every endpoint
  declares the roles allowed to call it; the backend rejects anything else
  (`403 forbidden_role`).
- **Local-body scoping:** non-admin requests are filtered/validated against the
  principal's `localBodyId` (a JWT claim). A reference to another local body's row
  returns `404` (not `403`) to avoid existence probing.
- **Worker data isolation (D28) — three layers:**
  1. list endpoints auto-filter to `activeAssignee = self`;
  2. every `/{id}` fetch/action re-checks the active assignment after loading;
  3. Workers have no role grant to local-body-wide analytics, reports, audit, or
     budget endpoints; a Worker analytics call returns only their own metrics.
- **Citizen isolation:** reads limited to own complaints + read-only asset/map in
  own local body; writes limited to complaint submission and feedback on own
  complaints.
- **Object-level checks everywhere** — the path id is never the sole guard.
- **State-machine guards double as authorization** (COMPLAINT_STATE_MACHINE.md §4):
  e.g. only the active assignee may `start`/`resolve`; only an Officer may
  `assign`/`verify`/`close`/`reject`/`return`.
- **Admin** is a deployment-global operator in MVP; per-local-body Admin scoping is
  a Future refinement (not required by D1).
- The Flutter UI hides/disables actions a role cannot perform **only for UX** — it
  is not relied on for security.

## Alternatives considered

| Option | Why not |
|--------|---------|
| **Trust the Flutter UI to restrict actions** | The APK is distributable and modifiable; API calls can be made directly. Never a security boundary. |
| **Attribute-based access control (ABAC) / policy engine** | Fine-grained and flexible, but heavyweight to build/test/explain for four roles + one scope dimension. Revisit only if permissions become genuinely multi-dimensional. |
| **Per-object ACLs** | Unnecessary — access is fully determined by role + local body + assignment/ownership. |
| **API-gateway-enforced authz** | No gateway in the single-host modular monolith; enforcement lives in the app's security layer. |

## Consequences

**Positive**
- One consistent enforcement model, close to the data, easy to unit-test
  (SECURITY_ARCHITECTURE §15).
- Worker/citizen data-isolation requirements (D28, D2) are satisfied by
  construction.
- Matches Spring Security's method/endpoint security idioms (ADR-0006).

**Negative / trade-offs**
- Every new endpoint must be consciously classified by allowed roles + scope +
  object check; enforced by review and by the authz test suite.
- `404`-for-out-of-scope means some genuinely-missing vs not-authorised cases look
  alike to clients — an intentional privacy trade-off.

**Follow-up**
- Maintain the role-capability matrix (SECURITY_ARCHITECTURE §4 / API §3) as the
  single reference; add an authz test per endpoint group.
