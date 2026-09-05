# ADR-0003 — API style: REST + JSON over HTTPS

**Status:** **ACCEPTED** — team-approved at the CAMS architecture sign-off (2026-09-04) as **AD-03**.
**Date:** 2026-09-04 · **Review note:** approved as written. Base path `/api/v1`; normal REST resources for CRUD; explicit action sub-resources for important complaint state transitions; consistent validation + error-response conventions.
**Relates to:** API_ARCHITECTURE.md; REQUIREMENTS.md D5, D20; team decision **AD-03**

## Context

One Flutter app talks to one backend (ADR-0002). We need a single, well-understood
request/response style the team can design, implement, test, and explain, covering
CRUD (assets, categories, org units), a **state machine** (complaints), file
uploads, and file downloads (reports/media). There is **no** public API and **no**
external integration (D20).

## Decision

- **REST over HTTPS, JSON bodies**, one versioned base path **`/api/v1`**.
- **Resource-oriented** for CRUD.
- **Action sub-resources** for complaint state transitions —
  `POST /complaints/{id}/assign|start|resolve|return|verify|close|reject` — because
  each is a guarded verb with side effects (status history + audit + notifications),
  not a field edit. This is a deliberate, documented deviation from strict REST.
- Conventions fixed in API_ARCHITECTURE.md §2: bearer-JWT auth, pagination,
  filtering by explicit params, ISO-8601 UTC timestamps, `Accept-Language`,
  uniform JSON error body, optimistic concurrency via `expectedStatus`.
- `multipart/form-data` for uploads; binary responses for report/media downloads.

## Alternatives considered

| Option | Why not |
|--------|---------|
| **GraphQL** | Adds a schema + resolver runtime, query-cost/depth limiting, and its own auth/field-authorization model that the team must learn and secure. The client needs are fixed and few; REST endpoints are simpler to reason about and test. No over-fetching problem at this scale. |
| **gRPC / Protobuf** | Better for high-throughput service-to-service calls; worse for a mobile client + human debugging; tooling overhead. No benefit here. |
| **"REST-purist" (PATCH status only, no action endpoints)** | Would push all transition guards into interpreting arbitrary field diffs; harder to validate and to map 1:1 to the state machine. Action sub-resources are clearer and safer. |
| **JSON:API / HAL / OData** | Extra spec surface and client tooling for little gain on a small fixed client. |
| **Unversioned API** | Guarantees pain later; `/api/v1` costs nothing now. |

## Consequences

**Positive**
- Every team member can read the endpoint catalogue and know exactly what to build
  and test.
- Trivial to exercise with `curl`/Postman during development and demos.
- Action endpoints line up 1:1 with COMPLAINT_STATE_MACHINE.md, so guards,
  notifications, and audit events have one obvious home.
- Standard Spring Web tooling; MockMvc tests are straightforward.

**Negative / trade-offs**
- Action sub-resources are not "pure" REST; documented as an intentional choice.
- The client must assemble a few calls for some screens (e.g. asset detail +
  history + open complaints) — acceptable; can add a composed read later if needed.
- Versioning discipline required for any breaking change (`/api/v2`).

**Neutral**
- OpenAPI/Swagger generation is encouraged but optional for MVP.
