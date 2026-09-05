# ADR-0006 — Authentication and session model

**Status:** **ACCEPTED** — team-approved at the CAMS architecture sign-off (2026-09-04) as **AD-06**.
**Date:** 2026-09-04 · **Review note:** approved as written. Concrete hashing choice recorded per AD-06: **Argon2id** is the primary algorithm; **BCrypt (cost ≥ 10)** is the accepted fallback if Argon2id proves impractical on the deployment host. Short-lived JWT access tokens + rotating refresh tokens; server-side only; never store plaintext.
**Relates to:** REQUIREMENTS.md D1, D16, D18, D28, NFR-SEC-001..003, NFR-SEC-006/007;
SECURITY_ARCHITECTURE.md §3–§5; team decisions **AD-06** (authentication) and **AD-07** (authorization — see ADR-0012)

## Context

One Flutter client, four roles, no external identity provider (D20), no SMS/email
infra (D16). Requirements: authenticate before any role function (FR-AUTH-001);
enforce role + local-body scope + worker isolation (D1, D6, D28); passwords never
stored in plaintext (D18); deactivated accounts lose access (FR-AUTH-003/010);
self-contained (NFR-SEC-007).

## Decision

- **Password hashing:** **Argon2id** (primary) via Spring Security
  `PasswordEncoder`; **BCrypt (cost ≥ 10)** is the approved fallback. Algorithm +
  params are stored with the hash so a later migration is possible.
- **Tokens:** stateless **access JWT** (short-lived, proposed 15–30 min) +
  opaque **refresh token** (proposed 14–30 days), stored **hashed** in a
  `refresh_token` table, **single-use with rotation**.
- **JWT claims:** `sub`, `role`, `localBodyId` (null for global Admin), `lang`,
  `iat`, `exp`, `jti`. Signed with a secret/key from external config. No PII in the
  token.
- **Authorization:** Spring Security method/endpoint rules for the role gate;
  local-body scoping and worker/citizen object-level checks in the service layer
  (SECURITY_ARCHITECTURE §4).
- **Revocation / deactivation:** logout, password change, and Admin reset revoke
  all of a user's refresh tokens; **`INACTIVE` accounts cannot refresh**, so
  deactivation is effective within one access-token lifetime.
- **Brute-force:** per-identifier + per-IP throttling on `/auth/*`; generic errors.
- **Client storage:** tokens in platform secure storage (Android Keystore-backed),
  not plain preferences; all traffic over HTTPS.
- **Citizen recovery:** see ADR-0007 (OQ-40).

## Alternatives considered

| Option | Why not |
|--------|---------|
| **Server-side sessions (cookie + session store)** | Simple and easy to revoke, but a mobile client + a possible future second client is a cleaner fit for bearer tokens; also avoids CSRF concerns. Session store adds stateful infra. Acceptable fallback if the team prefers it. |
| **Long-lived access JWT, no refresh token** | Simple, but you cannot revoke a stolen token before expiry, and you can't promptly enforce deactivation. Rejected. |
| **Access JWT + non-rotating refresh token** | Slightly simpler; loses replay detection on the refresh token. Rotation is cheap to implement and materially safer. |
| **OAuth2/OIDC with an external IdP** | Violates "self-contained" (NFR-SEC-007); nothing to integrate with; heavy for a student MVP. |
| **MFA / OTP** | No second channel available (D16); out of scope. |

## Consequences

**Positive**
- Standard, well-documented Spring Security path; low risk of rolling our own.
- Short access tokens + revocable rotating refresh tokens balance simplicity and
  the ability to enforce deactivation/logout (D18, FR-AUTH-010).
- Stateless access-token validation keeps the API simple and scale-flexible
  (OQ-14).

**Negative / trade-offs**
- A small amount of refresh-token bookkeeping (rotation chain, pruning expired
  rows via housekeeping).
- A stolen access token is valid until it expires (bounded by the short lifetime);
  accepted for MVP.
- Signing-key rotation requires invalidating refresh tokens; procedure documented
  in SECURITY_ARCHITECTURE §12.

**Follow-up**
- Fix exact token lifetimes and throttling thresholds in config during
  implementation; document defaults in `application-example.properties`.
