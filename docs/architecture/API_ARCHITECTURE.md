# CAMS — API Architecture

**Document:** `docs/architecture/API_ARCHITECTURE.md`
**Version:** 1.0 (**TEAM-APPROVED**)
**Date:** 2026-09-04 · **Approved:** 2026-09-04 (architecture sign-off)
**Status:** 🟢 **TEAM-APPROVED** as decision **AD-03** (REST/JSON, `/api/v1`, action
sub-resources for complaint transitions, consistent validation/error conventions),
with authorization per **AD-07** (ADR-0012). **High-level contract only — no
controller code, no request/response bodies beyond the shapes needed to review the
design.** Source of truth: `REQUIREMENTS.md` v1.0. Implementation of these endpoints
has **not** started (gated behind `TASKS.md` §1 environment readiness).

Related: [`SYSTEM_ARCHITECTURE.md`](SYSTEM_ARCHITECTURE.md),
[`DATA_MODEL.md`](DATA_MODEL.md),
[`COMPLAINT_STATE_MACHINE.md`](COMPLAINT_STATE_MACHINE.md),
[`SECURITY_ARCHITECTURE.md`](SECURITY_ARCHITECTURE.md), ADR-0003.

---

## 1. Style decision

**PROPOSED (ADR-0003): REST over HTTPS, JSON bodies, one versioned base path
`/api/v1`.**

- Resource-oriented for CRUD (`/assets`, `/complaints`, …).
- **Action sub-resources** for state-machine transitions
  (`POST /complaints/{id}/assign`), because the operations are verbs with guards and
  side effects, not field edits. This is a deliberate, documented pragmatic
  exception to "pure REST".
- No GraphQL (adds a schema/runtime the team must learn and secure for no MVP
  benefit). No gRPC (browser/Flutter tooling, debuggability). No public API, no
  external webhooks (D20).
- One monolith serves the whole API (SYSTEM_ARCHITECTURE §4); paths are grouped by
  module.

---

## 2. Conventions

| Concern | Convention (PROPOSED) |
|---------|-----------------------|
| Base path / version | `/api/v1`; breaking changes → `/api/v2`. |
| Transport | HTTPS only; HTTP refused/redirected. |
| Auth | `Authorization: Bearer <access JWT>` on all endpoints except `/auth/login`, `/auth/refresh`, `/auth/citizen/register`, `/auth/citizen/recovery/*`, `/health`, `/meta/enums`. |
| Content type | `application/json` for bodies; `multipart/form-data` for uploads; responses `application/json`, or `application/pdf` / `…spreadsheetml.sheet` for reports, or the image type for media. |
| IDs | Opaque strings (UUID proposed); never guessable sequence exposed as the only guard. |
| Timestamps | ISO-8601 UTC (`...Z`); the app localises. |
| Language | `Accept-Language: en` / `hi`; server returns **codes** (status/priority/error keys), app renders text. Free text is returned as stored. |
| Pagination | `?page=<0-based>&size=<n>`; response `{ content:[], page, size, totalElements, totalPages }`; `size` capped by config (default cap proposed 100). |
| Sorting | `?sort=field,asc|desc` (whitelisted fields per endpoint). |
| Filtering | Explicit query params per endpoint (documented), not a generic query language. |
| Errors | HTTP status + JSON `{ "error":"<code>", "message":"<dev text>", "details":[{field,issue}], "requestId":"..." }`. |
| Concurrency (transitions) | Body field `expectedStatus` (or `If-Match: <version>`); mismatch → `409 conflict_stale_state`. |
| Idempotency (uploads/transitions) | Safe to retry a failed call; server ignores a repeat that would re-apply an already-applied transition (returns current state). |
| Rate limiting | Per-IP on `/auth/*`; global sane request-size cap (SECURITY §7, §14). |
| CORS | Not required for the Flutter app; if a dev web tool is used, allow-list only. |

---

## 3. Authorization model (enforced server-side, every request)

Token claims (access JWT): `sub` (userId), `role`, `localBodyId` (null for global
Admin), `lang`, `exp`, `iat`, `jti`.

| Rule | Enforcement |
|------|-------------|
| **Role gate** | Each endpoint declares allowed roles (tables below). 403 `forbidden_role` otherwise. |
| **Local-body scope** | Non-admin requests are filtered/validated against `claims.localBodyId`. Cross-body id → 404 (not 403, to avoid probing). |
| **Worker isolation (D28)** | `WORKER` may read/act only on complaints where they hold the **active** assignment. Any list endpoint auto-filters; any `{id}` fetch/action re-checks. Analytics for a worker return **only their own** figures. |
| **Citizen scope (D2)** | `CITIZEN` reads only their own complaints + read-only assets/map in their local body; may act only on `SUBMIT` and `FEEDBACK` for their own complaints. |
| **Object-level checks** | Every `/{id}` action re-verifies ownership/scope after loading the row — never trust the path alone. |
| **Deactivated account** | Access token still valid until expiry (short); **refresh is denied** for `INACTIVE` accounts, so access ends within one access-token lifetime. |

---

## 4. Endpoint catalog (conceptual)

Legend for "Who": **A**dmin · **O**fficer · **W**orker · **C**itizen · *auth* = any
authenticated user. "Side effects" lists notification + audit per
[`COMPLAINT_STATE_MACHINE.md`](COMPLAINT_STATE_MACHINE.md).

### 4.1 Auth & account — `/api/v1/auth`, `/api/v1/admin/users`, `/api/v1/me`

| Method & path | Who | Purpose | Key input | Key output / effect |
|---------------|-----|---------|-----------|---------------------|
| `POST /auth/login` | public | Log in | identifier, password | access + refresh tokens; or `401 invalid_credentials` / `403 account_inactive` |
| `POST /auth/refresh` | public (valid refresh) | Rotate tokens | refresh token | new pair; old refresh revoked; `401` if revoked/expired/inactive |
| `POST /auth/logout` | auth | End session | refresh token | refresh revoked |
| `POST /auth/citizen/register` | public | Citizen self-registration (D16) | mobile number, password, name, language | citizen account (ACTIVE); tokens. Throttled; duplicate mobile → `409` |
| `POST /auth/password/change` | auth | Change own password | old, new | ok; all other refresh tokens revoked |
| `POST /auth/citizen/recovery/request` | public | Start self-contained citizen recovery (ADR-0007) | mobile number (+ recovery code **if** that option adopted) | generic `202 accepted` (no account enumeration). Staff-assisted path creates a pending request for an Officer/Admin to action. |
| `POST /admin/users` | A | Create Officer/Worker (D2) | name, role, identifier, localBodyId | account (ACTIVE); temp password out-of-band |
| `PATCH /admin/users/{id}` | A | Edit / activate / deactivate (FR-AUTH-003/010) | fields | updated; audit `ACCOUNT_*` |
| `POST /admin/users/{id}/reset-password` | A | Staff password reset (D21) | — | temp password; target's refresh tokens revoked; audit `PASSWORD_RESET` |
| `POST /admin/citizen-recovery/{requestId}/resolve` | A,O | Complete a staff-assisted citizen reset (ADR-0007) | verified identity note | temp password for citizen; audit `PASSWORD_RESET` |
| `GET /me` | auth | Own profile | — | id, role, name, localBody, language |
| `PATCH /me/language` | auth | Set EN/HI (D12) | language | updated |

### 4.2 Organisation — `/api/v1/local-bodies`, `/villages`, `/wards`

| Method & path | Who | Purpose |
|---------------|-----|---------|
| `POST /local-bodies`, `PATCH /local-bodies/{id}` | A | Create/edit/deactivate LocalBody (D19) |
| `GET /local-bodies`, `GET /local-bodies/{id}` | A (all) · O (own) | List/read |
| `POST /local-bodies/{id}/villages`, `PATCH /villages/{id}` | A | Village/Municipality CRUD |
| `POST /villages/{id}/wards`, `PATCH /wards/{id}` | A | Ward CRUD (name/number, optional centroid) |
| `GET /wards?localBodyId=&villageId=` | A,O,W,C | Ward list for pickers/filters |

Deactivate, never delete while referenced (DATA_MODEL §10). All mutations audited
(`LOCALBODY_CHANGE` / `WARD_CHANGE`).

### 4.3 Asset categories — `/api/v1/asset-categories`

| Method & path | Who | Purpose |
|---------------|-----|---------|
| `GET /asset-categories?localBodyId=` | A,O,W,C | List active categories (for pickers/filters) |
| `POST /asset-categories`, `PATCH /asset-categories/{id}` | A | Add/rename/deactivate (D23); audit `CATEGORY_CHANGE`. No code change needed. |

### 4.4 Assets — `/api/v1/assets`

| Method & path | Who | Purpose | Notes |
|---------------|-----|---------|-------|
| `POST /assets` | O,A | Register asset (FR-ASSET-001..003) | category, wardId, lat, lng, status, condition, installationDate, warranty? |
| `GET /assets` | A,O,W,C | List/filter (FR-ASSET-010) | `?wardId=&categoryId=&status=&condition=&q=&page=&size=&sort=` ; scoped to caller's local body |
| `GET /assets?view=map&bbox=minLng,minLat,maxLng,maxLat` | A,O,W,C | Map markers (FR-GIS-001/004) | returns light DTO `{id,lat,lng,category,status}`; `maxMarkers` cap from config |
| `GET /assets/{id}` | A,O,W,C | Detail + open complaints + maintenance history (FR-ASSET-011) | |
| `PATCH /assets/{id}` | O,A | Edit attributes (FR-ASSET-009) | audit `ASSET_UPDATE` |
| `POST /assets/{id}/decommission` | O,A | Set status DECOMMISSIONED, keep history | audit `ASSET_DECOMMISSION` |
| `POST /assets/{id}/photos` (multipart) | O,A | Add photo, ≤5 total (D25) | Media validation (SECURITY §7) |
| `DELETE /assets/{id}/photos/{photoId}` | O,A | Remove a photo | audit `ASSET_UPDATE` |
| `DELETE /assets/{id}` | A | Hard delete (FR-ASSET-012, Secondary) | blocked/confirmed if history exists; audited |

### 4.5 Complaints & maintenance — `/api/v1/complaints`

Reads are auto-scoped: **C** → own; **W** → assigned to them (active); **O/A** →
whole local body.

| Method & path | Who | Purpose | Side effects |
|---------------|-----|---------|--------------|
| `POST /complaints` (multipart: fields + optional photo) | C (O,A = Secondary) | Create against a registered asset (D3, FR-COMP-001/002) | state → `PENDING`; notify Officers; audit `COMPLAINT_CREATE` |
| `GET /complaints` | A,O,W,C | Queue/list (FR-COMP-005/007) | `?status=&wardId=&categoryId=&priority=&assignedTo=me&from=&to=&page=&size=&sort=` |
| `GET /complaints/{id}` | A,O,W(assignee),C(owner) | Detail | |
| `GET /complaints/{id}/history` | A,O,W(assignee),C(owner) | Status timeline | from `complaint_status_history` |
| `POST /complaints/{id}/photos` (multipart) | C(owner, pre-review) / W(assignee) | Add CITIZEN_REPORT / WORKER_BEFORE / WORKER_COMPLETION photo | Media validation |
| `POST /complaints/{id}/reject` | O | `PENDING → REJECTED` (+reason) | notify Citizen; audit `COMPLAINT_REJECT` |
| `POST /complaints/{id}/assign` | O | `PENDING → ASSIGNED` (workerId + priority) | notify Worker; audit status+assignment+priority |
| `POST /complaints/{id}/reassign` | O | change active Worker (+reason) | notify new Worker; audit `ASSIGNMENT_CHANGE` |
| `POST /complaints/{id}/priority` | O | set/change priority | audit `PRIORITY_CHANGE` |
| `POST /complaints/{id}/start` | W(assignee) | `ASSIGNED → IN_PROGRESS` | notify Citizen; audit status change |
| `POST /complaints/{id}/resolve` (multipart) | W(assignee) | `IN_PROGRESS → RESOLVED` — **requires ≥1 completion photo**, repairCost, remarks (D11) | notify Officer + Citizen; audit status change; `409 completion_photo_required` if missing |
| `POST /complaints/{id}/return` | O | `RESOLVED → IN_PROGRESS` (+reason); `returned_count++` (OQ-38) | notify Worker; audit status change |
| `POST /complaints/{id}/verify` | O | `RESOLVED → VERIFIED` | audit `COMPLAINT_VERIFY` |
| `POST /complaints/{id}/close` | O | `VERIFIED → CLOSED` (+optional assetStatus/assetCondition) | update asset; create `maintenance_history`; notify Citizen; audit `COMPLAINT_CLOSE` |

All transition bodies carry `expectedStatus` for optimistic concurrency (§2).

### 4.6 Maintenance history — `/api/v1/assets/{id}/maintenance-history`

| Method & path | Who | Purpose |
|---------------|-----|---------|
| `GET /assets/{id}/maintenance-history` | A,O,W,C | Reverse-chronological entries (FR-HIST-003) |

### 4.7 Feedback — `/api/v1/complaints/{id}/feedback`

| Method & path | Who | Purpose | Notes |
|---------------|-----|---------|-------|
| `POST /complaints/{id}/feedback` | C(owner) | Submit rating (+optional one-time text) (FR-FEED-001..004) | only if `CLOSED` & no existing feedback; `rating` 1..`feedback.rating.max` (OQ-37); audit `COMPLAINT_UPDATE` |
| `GET /complaints/{id}/feedback` | A,O,W(assignee) | Read rating/text (FR-FEED-005) | Worker sees only their own complaints' feedback |

### 4.8 Notifications — `/api/v1/notifications`

| Method & path | Who | Purpose |
|---------------|-----|---------|
| `GET /notifications?unread=&page=&size=` | auth | List own notifications (FR-NOTIF) |
| `GET /notifications/unread-count` | auth | Badge count (polled) |
| `POST /notifications/{id}/read` / `POST /notifications/read-all` | auth | Mark read |

In-app only (D10). No push/subscription endpoints in MVP.

### 4.9 Audit — `/api/v1/audit`

| Method & path | Who | Purpose |
|---------------|-----|---------|
| `GET /audit?actorId=&action=&entityType=&entityId=&from=&to=&page=&size=` | A (O = Secondary, subset) | Query the audit log (FR-AUDIT-004) |

Read-only. No create/update/delete endpoint exists (SECURITY §9).

### 4.10 Analytics & reports — `/api/v1/analytics`, `/api/v1/reports`

| Method & path | Who | Purpose |
|---------------|-----|---------|
| `GET /analytics/asset-summary?wardId=&categoryId=` | A,O | Counts by status & condition (FR-ANLY-001) |
| `GET /analytics/complaint-summary?from=&to=&wardId=&categoryId=&priority=` | A,O | Counts by status/ward/category/priority (FR-ANLY-002) |
| `GET /analytics/my-metrics` | W | Worker's own metrics only (D28) — assigned/completed/avg time/returned/avg rating |
| `POST /reports/complaint` | A,O | Generate Complaint report; body: filters + `format=PDF\|XLSX` (D33, FR-RPT-001/003) → file stream |
| `POST /reports/asset-register` | A,O | Generate Asset register report; filters + `format` (FR-RPT-002/003) → file stream |

Synchronous for MVP volumes; an async `job` variant is a documented later option
(SYSTEM_ARCHITECTURE §15). Advanced reports (FR-RPT-004..007) are Secondary — not
routed in MVP.

### 4.11 Media — `/api/v1/media`

| Method & path | Who | Purpose |
|---------------|-----|---------|
| `GET /media/{storageKey}` | A,O,W,C (subject to owning-entity visibility) | Stream an image; `Cache-Control` private |

Uploads go through the owning entity's `.../photos` endpoints (not a generic
upload), so validation and the ≤5 / mandatory-completion rules are enforced in one
place.

### 4.12 Meta / health — `/api/v1/meta`, `/health`

| Method & path | Who | Purpose |
|---------------|-----|---------|
| `GET /meta/enums` | public | Enumerations + labels bootstrap for the app (statuses, conditions, priorities, notification types) |
| `GET /health` | public | Liveness/readiness for supervision (SYSTEM_ARCHITECTURE §16) |

---

## 5. Common error codes (illustrative, not exhaustive)

| HTTP | `error` | When |
|------|---------|------|
| 400 | `validation_failed` | bad/missing fields; `details[]` per field |
| 401 | `invalid_credentials` / `token_expired` / `token_invalid` | auth |
| 403 | `forbidden_role` / `account_inactive` | RBAC / deactivated |
| 404 | `not_found` | missing, or out-of-scope id (deliberate, avoids probing) |
| 409 | `conflict_stale_state` | `expectedStatus` mismatch on a transition |
| 409 | `completion_photo_required` | `resolve` without a completion photo (D11) |
| 409 | `duplicate` | e.g. mobile already registered; feedback already given |
| 413 | `payload_too_large` | upload/body over cap |
| 415 | `unsupported_media_type` | non-image upload / wrong content type |
| 422 | `rule_violation` | domain rule (e.g. >5 asset photos, reject from non-PENDING) |
| 429 | `rate_limited` | auth endpoint throttle |
| 500 | `internal_error` | unexpected; `requestId` for logs |

---

## 6. Non-goals (MVP)

No GraphQL; no public/partner API; no external webhooks or callbacks (D20); no
server-sent events / WebSockets (polling only, D10); no file download links for
media without auth; no bulk import endpoints (Secondary/Future); no QR endpoints
(Future).

---

## 7. Validation checklist (this document)

| Check | Result |
|-------|--------|
| Every MVP FR area has endpoints | ✅ §4 (auth, org, category, asset, GIS via asset, complaint, maintenance, history, feedback, notification, audit, analytics, reports, media) |
| Complaint endpoints match the state machine exactly | ✅ §4.5 ↔ COMPLAINT_STATE_MACHINE §3 |
| Worker isolation expressed in the contract | ✅ §3, §4.5, §4.7, §4.10 (`my-metrics`) |
| Citizen limited to submit + feedback + own reads | ✅ §3, §4.5, §4.7 |
| Officer scope = whole local body, ward = filter param | ✅ §3, §4.4/§4.5 query params |
| In-app notifications only; no push endpoints | ✅ §4.8, §6 |
| Reports = Complaint + Asset register, PDF + XLSX | ✅ §4.10 |
| Audit is read-only | ✅ §4.9, §6 |
| No Future features routed (QR, offline sync, web dashboard, AI, external) | ✅ §6 |
| Asset status & condition both present as filters/fields | ✅ §4.4 |
| No capacity/availability numbers baked into the contract | ✅ caps are config, not requirements |

---

## 8. Open items

| ID | Item | API effect |
|----|------|-----------|
| OQ-37 | Rating scale | `rating` validated against config `feedback.rating.max`; `/meta/enums` can expose it |
| OQ-40 | Citizen recovery mechanism | `/auth/citizen/recovery/*` shape depends on the option chosen (ADR-0007); staff-assisted resolve endpoint included as the safe default |
| OQ-14 | Scale | page-size cap, `maxMarkers`, sync-vs-async reports are config/So-far-simple; revisit |
| Report async | If reports get heavy, add `POST /reports/... → {jobId}` + `GET /reports/jobs/{id}` | additive, non-breaking |

---

## 9. Change log

| Version | Date | Notes |
|---------|------|-------|
| 0.1 (PROPOSED) | 2026-09-04 | REST/JSON contract for Requirements v1.0. Action sub-resources for complaint transitions; server-side RBAC + local-body scope + worker isolation; in-app-only notifications; sync PDF/XLSX reports; read-only audit. |
| **1.0 (TEAM-APPROVED)** | 2026-09-04 | Team sign-off as **AD-03** (+ **AD-07** authorization / ADR-0012). Contract unchanged; status raised to APPROVED. Open items (OQ-37, OQ-40, OQ-14) preserved; async-report path remains a documented non-breaking future addition. No code. |
