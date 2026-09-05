# CAMS — Security Architecture

**Document:** `docs/architecture/SECURITY_ARCHITECTURE.md`
**Version:** 1.0 (**TEAM-APPROVED — baseline**)
**Date:** 2026-09-04 · **Approved:** 2026-09-04 (architecture sign-off)
**Status:** 🟢 **TEAM-APPROVED as the MVP security baseline (AD-22).** Covers
authentication (AD-06), authorization/RBAC (AD-07 / ADR-0012), password hashing
(**Argon2id**, BCrypt fallback), JWT + rotating refresh tokens, input validation,
file-upload validation + media re-encode / EXIF stripping (AD-09), audit logging
(AD-12), rate limiting where specified, and safe error handling. **Known accepted
limitation:** MVP has **no dedicated antivirus scanning** for uploads (§16, §18) —
mitigated by re-encoding + type/size/dimension checks; may be added later.
Architecture-level review of Requirements `REQUIREMENTS.md` v1.0
(**NFR-SEC-001..007**, D2, D6, D16, D18, D21, D22, D28, D36). Not a penetration
test; not code.

Related: [`SYSTEM_ARCHITECTURE.md`](SYSTEM_ARCHITECTURE.md),
[`API_ARCHITECTURE.md`](API_ARCHITECTURE.md), [`DATA_MODEL.md`](DATA_MODEL.md),
ADR-0006 (auth/session), ADR-0007 (citizen recovery), ADR-0008 (media),
ADR-0010 (backup).

---

## 1. Scope & principles

- **Right-sized security for a student MVP** serving a local body: protect citizen
  personal data and the integrity of the asset/complaint record; don't
  over-engineer.
- **All rules enforced server-side.** The Flutter app is untrusted.
- **Least privilege** per role; **deny by default**.
- **Auditability** of important changes (D22).
- Every control below traces to a requirement or a standard baseline expectation.

---

## 2. Threat model (summary)

| Element | Notes |
|---------|-------|
| **Assets to protect** | Citizen PII (name, mobile, photos, location); complaint/asset records & history; audit log; credentials & signing keys; backups. |
| **Actors** | Legitimate: Admin, Officer, Worker, Citizen. Adversarial: a curious/malicious citizen, a disgruntled ex-staff account, an attacker on the network, an attacker with the APK. |
| **Trust boundaries** | Device ↔ API (untrusted network); API ↔ DB/media (trusted host); API ↔ backup target. Map tiles come from outside but receive no CAMS data. |
| **Top risks** | Broken access control (worker/citizen seeing others' data); credential theft / weak recovery (OQ-40); malicious image upload; injection; PII in logs/backups; secrets in VCS. |
| **Explicitly out of scope (D20)** | Aadhaar/govt-portal/payment/SMS/IoT integrations — none exist, so their attack surface doesn't either. |

---

## 3. Authentication (NFR-SEC-002, D16, ADR-0006)

- **Password hashing (AD-06, concrete choice):** **Argon2id** via Spring Security's
  `PasswordEncoder` is the primary algorithm; **BCrypt (cost ≥ 10)** is the
  approved fallback if Argon2id is impractical on the deployment host. Algorithm +
  parameters are stored with the hash for future upgrades. **Plaintext never stored
  or logged** (D18).
- **Password policy (FR-AUTH-012):** minimum length (proposed ≥ 8), reject common/
  breached-obvious values by a small local denylist; no mandatory rotation. No
  external "breach API" call (self-contained, D20).
- **Login:** identifier + password → short-lived **access JWT** (proposed 15–30 min)
  + opaque **refresh token** (proposed 14–30 days), stored **hashed** in
  `refresh_token`, single-use with rotation.
- **JWT:** signed with a strong secret/asymmetric key from config (never in VCS);
  claims minimal (`sub, role, localBodyId, lang, iat, exp, jti`); verified on every
  request; clock-skew small. No sensitive data in the token.
- **Refresh & revocation:** rotation invalidates the previous token; logout,
  password change, and Admin reset revoke all of a user's refresh tokens;
  **`INACTIVE` accounts cannot refresh** (deactivation takes effect within one
  access-token lifetime).
- **Brute-force:** per-identifier + per-IP throttling on `/auth/login` and
  `/auth/citizen/*` (progressive delay / lockout window from config); generic error
  messages (no "user exists").
- **Transport:** tokens only over HTTPS; app stores them in platform secure storage
  (Keystore-backed), not plain shared preferences.

---

## 4. Authorization (D1, D6, D28, NFR-SEC-001)

- **RBAC**: four roles; each endpoint declares allowed roles
  ([`API_ARCHITECTURE.md`](API_ARCHITECTURE.md) §3–§4). 403 on role mismatch.
- **Local-body scoping**: non-admin queries filtered by `claims.localBodyId`;
  cross-body ids return **404** (not 403) to avoid existence probing.
- **Worker data isolation (D28)** — enforced at three layers:
  1. list endpoints auto-add `activeAssignee = self`;
  2. `/{id}` fetch/action re-checks the active assignment after loading;
  3. worker analytics endpoint returns only `my-metrics`; no local-body-wide
     analytics/report/audit/budget endpoint is in a WORKER's allowed-roles set.
- **Citizen isolation (D2)**: reads limited to own complaints + read-only asset/map
  in own local body; writes limited to `SUBMIT` and `FEEDBACK` on own complaints.
- **Object-level checks everywhere** — the path id is never the only guard.
- **Admin**: deployment-global operator (create local bodies/staff/categories,
  view audit). Per-body admin scoping is a **Future** refinement, not required by
  D1.
- **State-machine guards** double as authorization
  ([`COMPLAINT_STATE_MACHINE.md`](COMPLAINT_STATE_MACHINE.md) §4): e.g. only the
  active assignee can `start`/`resolve`; only an Officer can `assign`/`verify`/
  `close`/`reject`/`return`.

---

## 5. Account lifecycle & password recovery

- **Activation/deactivation (FR-AUTH-003/010):** Admin sets `status`; `INACTIVE`
  blocks login and refresh; the account and its history are retained (D18).
- **Staff reset (D21):** Admin-only `POST /admin/users/{id}/reset-password` →
  system-generated temporary password delivered out-of-band (in person / existing
  official channel); target's sessions revoked; audited `PASSWORD_RESET`.
- **Citizen recovery (D21 / OQ-40)** — see **ADR-0007**. Key security statement:

  > A citizen self-service password reset **cannot be made strongly secure without
  > a trusted recovery channel** (SMS/email), which D16/D21 exclude for MVP. We do
  > **not** claim otherwise.

  **Recommended safe default (PROPOSED):** **staff-assisted reset** — the citizen
  visits/telephones the local-body office; an Officer/Admin verifies identity by
  local knowledge / an ID shown in person, then triggers a temporary-password
  reset (`POST /admin/citizen-recovery/{id}/resolve`), audited. This is
  "self-contained" (no external infra) and its security rests on human
  verification.

  **Optional lower-assurance convenience:** a **one-time recovery code** shown once
  at registration that the citizen must save; entering mobile + code allows setting
  a new password. Clearly weaker (code can be lost/shared); offered only as a
  supplement, rate-limited, single-use, and **off by default**.

  **Options put to the team (decision required):**
  a) accept **staff-assisted only** for MVP (recommended);
  b) staff-assisted **+ recovery code** convenience;
  c) approve **SMS OTP** as a scoped exception — this **changes D16/D21** and needs
  a gateway (an external dependency) → record as a requirements change if chosen;
  d) approve **email reset** if citizens reliably have email — also a change to the
  "no external infra" intent.

  Until the team decides, the architecture ships path (a).

---

## 6. API access control (NFR-SEC-001, NFR-SEC-003)

- HTTPS-only; auth filter on every route except the small public set
  ([`API_ARCHITECTURE.md`](API_ARCHITECTURE.md) §2).
- Uniform error body; **no stack traces** to clients; `requestId` correlates to
  server logs.
- Request-size cap (config; small for JSON, larger for `multipart`); pagination
  `size` capped; list endpoints never return unbounded sets.
- No secrets, tokens, PII, or ids-of-others in URLs/query strings (NFR-SEC, privacy
  rules); tokens only in the `Authorization` header.
- CORS not enabled for the app; if a dev tool needs it, strict allow-list only.

---

## 7. File-upload security (D11, D25, ADR-0008)

Applies to asset photos, citizen complaint photos, worker before/completion photos.

| Control | Detail |
|---------|--------|
| **Type validation** | Accept only `image/jpeg`, `image/png` (proposed); verify by **magic bytes**, not just the client-declared MIME or extension. |
| **Size cap** | Per-file cap from config (e.g. a few MB after the app already compressed); `413`/`415` otherwise. |
| **Dimension cap** | Reject absurd pixel dimensions (decompression-bomb guard) before full decode. |
| **Re-encode** | Server decodes and **re-encodes** to a normalized JPEG; this strips embedded scripts/polyglots and **removes EXIF/GPS metadata** (privacy). |
| **Count rules** | ≤ 5 photos per asset (D25); ≥ 1 `WORKER_COMPLETION` required to `resolve` (D11) — enforced in the Media/Complaint services. |
| **Storage** | Written under a **media root outside any web/static path**, filename = server-generated UUID; original client filename never used on disk. |
| **Retrieval** | `GET /media/{key}` requires auth and re-applies the owning entity's visibility rules; `Cache-Control: private`. |
| **Malware** | Full AV scanning is **out of scope** for the student MVP; re-encoding + type/size/dimension checks are the mitigation. Flag as a residual risk (§18). |
| **Quota** | Per-request and (optional) per-user/day upload count limit to bound abuse and storage growth. |

---

## 8. Injection & input validation (NFR-SEC-001)

- **SQL injection:** all DB access via JPA/parameterized queries; **no string-
  concatenated SQL**; dynamic filters built with a safe criteria API and a
  **whitelist** of sortable/filterable fields.
- **Input validation:** DTO validation at the HTTP boundary (required, length,
  range, enum membership, lat/lng bounds, photo counts); domain invariants in
  services.
- **Output:** JSON responses; free text stored/returned as-is but never
  interpolated into SQL/HTML/headers. Reports (PDF/XLSX) treat user text as **data,
  not formulas** — prefix/escape values that could be read as spreadsheet formulas
  (`=`, `+`, `-`, `@`) to prevent CSV/Excel formula injection.
- **Path traversal:** media `storageKey` is validated against an allow pattern; no
  client-supplied path segments.
- **Mass assignment:** explicit DTOs; server ignores client-set fields like
  `status`, `role`, `localBodyId`, `createdBy`.

---

## 9. Audit integrity (D22, FR-AUDIT)

- `audit_entry` is **append-only**: no update/delete endpoint, no ORM cascade, no
  admin UI to edit it.
- **Actor and timestamp come from the server** (auth principal + server clock),
  never from the request body.
- Written **in the same transaction** as the change it records, so a rolled-back
  change leaves no audit and a committed change always has one.
- Optional hardening (Secondary): a hash chain (`prev_hash`) to make tampering
  detectable; not required for MVP.
- Audit is queryable by Admin only in MVP (Officer read-subset is Secondary).
- Routine navigation/clicks are **not** logged (D22) — only the defined action set.

---

## 10. Personal data protection (D18, NFR-SEC-004, NFR-SEC-006)

- **Minimisation:** only name, mobile, complaint text, photos, location are
  collected (DATA_MODEL §10).
- **Retention:** kept for the deployment lifetime unless an **audited** Admin
  removal (FR-AUTH-010); citizens **cannot** self-delete complaint history
  (FR-COMP-011).
- **Access:** PII visible only to the citizen themselves and to Officers/Admin of
  their local body; Workers see the complaint's operational data and any photos for
  **their** assigned complaints, plus the citizen rating for those — not a citizen
  directory.
- **Privacy statement (FR-AUTH-009):** shown in-app; states what is collected, that
  it is retained, and that history can't be self-deleted.
- **Logs:** application logs must not contain passwords, tokens, or full PII;
  mobile numbers masked in logs where practical.
- **Location:** stored as needed for the asset/complaint; **EXIF GPS stripped** from
  uploaded images (§7); never placed in URLs.

---

## 11. Transport security (NFR-SEC-003)

- **TLS for all device↔API traffic**; HTTP disabled or 301→HTTPS. HSTS if a proxy
  is used.
- Modern cipher suites; certificate from a real CA on the demo host (self-signed
  only for local dev, with the dev app configured accordingly — never shipped).
- DB and media are on the same host as the API for MVP (loopback/unix socket); if
  ever remote, that link must be TLS too.
- No sensitive data in query strings, logs, or error messages.

---

## 12. Secrets & configuration (NFR-SEC-002)

- **Nothing secret in Git.** DB credentials, JWT signing key, media root, backup
  target, feature flags come from **environment variables** or an **untracked**
  `application-<env>.properties` (the repo `.gitignore` already excludes `.env*`,
  `application-local*`, `*.pem`, `secrets.*`).
- Distinct credentials per environment (`local`, `demo`); the DB user for the app
  has only the privileges it needs (DML + limited DDL for Flyway, not superuser).
- JWT signing key: long random; rotation procedure documented (invalidate refresh
  tokens on rotation).
- A checked-in `application-example.properties` documents required keys with
  **placeholder** values only.

---

## 13. Backup security (D36, ADR-0010)

- Backups **contain full PII and media** → treated as sensitive:
  - stored on an **access-controlled** location (institute storage / a restricted
    directory / an encrypted external drive), not world-readable, not in the repo;
  - retention ≥ 7 days, older sets pruned (also limits exposure window);
  - the restore runbook is documented; a **restore drill** is done and recorded
    before final evaluation (NFR-BAK-003).
- If backups leave the host (e.g. copied to cloud/drive), **encrypt at rest**
  (e.g. GPG / an encrypted volume); key handled like other secrets (§12).
- Backup/restore scripts live in the repo; **credentials they need come from the
  environment**, not the scripts.

---

## 14. Availability / abuse resistance (OQ-17 OPEN)

- No availability **target** is set (OQ-17) and none is invented.
- Cheap protections included: per-IP auth rate limiting, global request-size caps,
  pagination caps, `maxMarkers` on the map query, upload count/size quotas, DB
  constraints preventing invalid state, transactional writes, `GET /health` for a
  process supervisor to auto-restart.
- DoS from a determined attacker is **not** fully mitigated at MVP (no WAF/CDN);
  acceptable for a scoped local-body deployment; revisit if OQ-17 sets a target.

---

## 15. Security testing approach (NFR-MAINT-001)

- **Dependency scanning:** enable the build's known-vuln check (e.g. OWASP
  Dependency-Check / `gradle`/`mvn` audit) in CI; review before releases.
- **Automated tests:** auth tests (no token → 401; wrong role → 403; cross-body id
  → 404; worker fetching another worker's complaint → 404/403; citizen fetching
  another citizen's complaint → 404); upload tests (non-image, oversized, huge
  dimensions rejected); state-machine guard tests.
- **Manual review:** a short checklist pass (this document) before the evaluation
  build; review any new endpoint against §4 rules.
- **Secrets scan:** a pre-commit / CI check that no `.env`/keys are committed.

---

## 16. Deliberately NOT done (avoiding over-engineering)

- No OAuth2/OIDC provider, SSO, or external IdP.
- No MFA (no second channel available; D16).
- No field-level DB encryption / HSM / KMS.
- No SIEM, WAF, IDS, or anomaly detection.
- No antivirus pipeline for uploads (mitigated by re-encoding, §7).
- No per-body Admin, no fine-grained ABAC — role + local-body scope is enough for
  MVP.
- No rate-limiting infrastructure beyond in-process counters.

Each of these is a reasonable **Future** hardening if the deployment context grows.

---

## 17. Requirements coverage

| Requirement | Covered by |
|-------------|-----------|
| NFR-SEC-001 (authn + RBAC gate everything) | §3, §4, §6 |
| NFR-SEC-002 (no plaintext passwords; no secrets in VCS) | §3, §12 |
| NFR-SEC-003 (encrypted transit) | §11 |
| NFR-SEC-004 (PII minimisation) | §10, DATA_MODEL §10 |
| NFR-SEC-005 (attributable actions) | §9 |
| NFR-SEC-006 (retention; no citizen self-delete; privacy statement) | §10 |
| NFR-SEC-007 (no external identity/verification services) | §3, §5, §16 |
| D22 / FR-AUDIT (audit content + integrity) | §9 |
| D28 (worker isolation) | §4 |
| D18 (data retention, hashing, privacy) | §3, §10, §13 |
| D36 / NFR-BAK (backup incl. media, tested restore) | §13, ADR-0010 |

---

## 18. Residual risks / open items

| Item | Risk | Disposition |
|------|------|-------------|
| **OQ-40** citizen recovery (OPEN) | Weak self-service reset if not staff-assisted | AD-08 / ADR-0007: staff-assisted default is approved for MVP; OQ-40 stays OPEN; no secure self-service claimed |
| No upload AV scanning (**accepted limitation, AD-22**) | Malicious file stored (mitigated, not eliminated) | Accepted for MVP; re-encode + type/size/dimension checks; may add a scanner later |
| Single host, no WAF/standby (**OQ-17** OPEN) | Availability / volumetric DoS | AD-19 / ADR-0017: accepted for scoped deployment, SPOF documented; revisit with a target |
| Self-signed TLS in dev | Habituation to cert warnings | Real cert on demo host; dev app pinned to dev cert |
| Backup encryption optional | PII exposure if a backup copy leaks | Mandate encryption **if** backups leave the host (§13) |
| Hash-chained audit not in MVP | Undetected audit tampering by a DB-level actor | App has no tamper path; DB access is host-restricted; add chain later if needed |
| Rating/text free input in reports | Spreadsheet formula injection | Escaped on export (§8) |

---

## 19. Change log

| Version | Date | Notes |
|---------|------|-------|
| 0.1 (PROPOSED) | 2026-09-04 | Architecture security review of Requirements v1.0. Argon2id/BCrypt hashing; short JWT + rotating refresh; RBAC + local-body scope + 3-layer worker isolation; upload re-encode/EXIF-strip; parameterized DB + formula-injection guard; append-only server-sourced audit; PII retention per D18; secrets out of VCS; backup sensitivity; OQ-40 handled as a flagged decision with a safe default. |
| **1.0 (TEAM-APPROVED — baseline)** | 2026-09-04 | Team sign-off as **AD-22** (with AD-06, AD-07/ADR-0012, AD-09, AD-12). Concrete hashing fixed to **Argon2id** (BCrypt fallback). Accepted limitation recorded: no dedicated AV scanning in MVP. OQ-40 stays OPEN; OQ-17 SPOF accepted for MVP. No code, no installs. |
