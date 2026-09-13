# CAMS — Project Plan

**Status:** Requirements **BASELINED (v1.0)**. Architecture **TEAM-APPROVED
(2026-09-04)** — decisions **AD-01 … AD-25**, recorded as ADRs 0001–0019.
Environment / prerequisite readiness: **E1–E7 all PASS** — E1 (JDK 21 LTS), E2
(Maven 3.9.16), E3 (Flutter 3.47.2 + Dart 3.13.2), E4 (Android SDK/toolchain,
API 36, emulator), E5 (PostgreSQL 17.11), E6 (Git repo on `main`, commit
`6b689ff`), **E7 — final audit (2026-09-05): environment classified READY WITH
NON-BLOCKING WARNINGS**. **Implementation Phase 1 — project skeleton: DONE
(2026-09-05).** `backend/` (Spring Boot 4.1.1 / Java 21, modular-monolith
package structure, `/health` endpoint) and `mobile/` (Flutter + Riverpod +
Material 3 app shell) created and runnability-verified end to end; **no
business logic, no domain tables, no CAMS feature screens** at that point —
see `TASKS.md` §3–§5 and `memory/MEMORY.md` for full detail. **Baseline
PostgreSQL schema (Flyway `V1__init_schema.sql`): DONE (2026-09-12)** — the
first backend feature; physical schema from `docs/architecture/DATA_MODEL.md`,
verified against the real `cams_dev` database — see `TASKS.md` §6 and
`memory/MEMORY.md`. Remaining, non-blocking: Docker decision. **Git (current,
2026-09-12):** `main` is at `d53b9fc`, synchronized with the configured
`origin` remote — see the Git note under the E6 record in §6 below for how
this differs from the "no remote configured" state recorded at E6/E7 time.
Domain/business-logic implementation (Auth, Asset, Complaint, etc.) stays
gated until a specific feature task is started.
**Last updated:** 2026-09-12 (baseline schema task — see §6; OQ-42 recorded — see §5)

This document records the high-level plan. It separates **APPROVED** decisions from
**PROPOSED** ones. Missing details are listed as open questions, not guessed;
genuinely unresolved OQs are preserved as OPEN.

## Related documents

- `docs/requirements/REQUIREMENTS.md` — **Requirements v1.0 (BASELINE), 2026-09-04.**
  Product decisions **D1–D37** approved and incorporated. MVP / Secondary / Future
  scope defined. Remaining OPEN/PENDING items in REQUIREMENTS.md §15.3 / §5 below.
  **This baseline is the source of truth and is unchanged by the architecture work.**
- `docs/architecture/` — **Architecture v1.0 (TEAM-APPROVED, 2026-09-04):**
  `SYSTEM_ARCHITECTURE.md` (§1A carries the AD-01…AD-25 register),
  `DATA_MODEL.md`, `COMPLAINT_STATE_MACHINE.md`, `API_ARCHITECTURE.md`,
  `SECURITY_ARCHITECTURE.md`. Modular monolith; covers every MVP requirement;
  Secondary/Future modules isolated.
- `docs/decisions/` — **ADRs 0001–0019 (ACCEPTED):** backend stack, modular
  monolith, REST, org hierarchy, complaint state machine, auth, RBAC, citizen
  recovery, media, map/GIS, notifications, audit, reporting, analytics/charting,
  deferred-scope isolation, deployment/connectivity, Riverpod, backup/restore,
  performance baseline. Index + OPEN-item map in `docs/decisions/README.md`.

---

## 1. Project context (factual)

- **Name:** CAMS — Community Asset Management System.
- **Type:** University engineering project.
- **Duration:** approximately one year.
- **Current state:** Workspace contains only this control/documentation structure.
  No backend, frontend, database, or infrastructure code.

## 2. Scope areas — now specified in the requirements baseline

These functional areas are defined, with MVP / Secondary / Future classification,
in `docs/requirements/REQUIREMENTS.md` v1.0. Summary of where each landed:

- **MVP:** authentication & 4 roles, local-body/village/ward setup, asset
  management, GIS/map (points only), citizen complaints (asset-linked), maintenance
  workflow, maintenance history, citizen feedback/rating, in-app notifications,
  audit trail, basic analytics, two reports (PDF/Excel).
- **Secondary:** inventory (one central store), budget/expenditure & allocation,
  advanced analytics & reports, SLA target reporting, citizen comment threads,
  duplicate linking.
- **Future:** offline + sync, QR identification, AI features, external
  integrations, web/desktop dashboard, external notification channels, officer
  hierarchy/escalation, languages beyond English/Hindi, iOS build.

## 3. Technology direction — **APPROVED (2026-09-04)**

All items below are **team-approved** (requirements-fixed items marked *(req)*;
the rest are the architecture ADRs AD-01…AD-25).

- Frontend: **Flutter** *(req: D5)*; single mobile app for all four roles.
- Flutter state management: **Riverpod** (AD-20 / ADR-0018).
- Flutter charts: **fl_chart** (AD-14 / ADR-0015).
- Target platform: **Android-first, Android 8.0 / API 26+** *(req: D26)*; iOS later.
- Backend: **Java 21 (LTS) + Spring Boot + Maven** (AD-01 / ADR-0001). *(Machine
  has JDK 24 installed — install/pin JDK 21 in the environment step; not done here.)*
- Database: **PostgreSQL** *(req: D36)*; **Flyway** migrations (AD-01). **No PostGIS**
  for MVP (points only, D13 / AD-10).
- Architecture style: **modular monolith**, one deployable (AD-02 / ADR-0002).
- API: **REST/JSON, `/api/v1`** (AD-03 / ADR-0003).
- Auth: **Spring Security; Argon2id hashing (BCrypt fallback); JWT access +
  rotating refresh** (AD-06 / ADR-0006); **server-side RBAC** (AD-07 / ADR-0012).
- Media: **server filesystem/object-like + DB metadata** (AD-09 / ADR-0008).
- Map: **Flutter OpenStreetMap-based** (AD-10 / ADR-0013); **tile provider/licensing
  still to be chosen** (tracked OPEN item).
- Notifications: **in-app, persisted in PostgreSQL, client polling** (AD-11 / ADR-0009).
- Reporting: **server-side Apache POI (Excel) + OpenPDF/simple PDF** (AD-13 / ADR-0011).
- Backup: **nightly `pg_dump` + media archive, ≥7-day retention, separate location,
  tested restore, MANIFEST/checksums** (AD-18 / ADR-0010).
- Deployment: **single host (app + PostgreSQL + media), HTTPS/TLS, no orchestration**
  (AD-19 / ADR-0017). **Online-only MVP** (AD-21). Specific host + Git remote
  undecided.

## 4. Approved decisions

**Requirements decisions:** product decisions **D1–D37** are approved and recorded
in `docs/requirements/REQUIREMENTS.md` §1 (Decision register) and Appendix A. The
requirements baseline (v1.0) is approved and **unchanged** by the architecture work.

**Architecture decisions:** the CAMS architecture review was **signed off by the
team on 2026-09-04**. Decisions **AD-01 … AD-25** are APPROVED and recorded as ADRs
0001–0019 (`docs/decisions/`) and in `SYSTEM_ARCHITECTURE.md` §1A.

| AD | Decision | ADR | Status | Date |
|----|----------|-----|--------|------|
| AD-01 | Java 21 LTS + Spring Boot + Maven + PostgreSQL + Flyway | 0001 | APPROVED | 2026-09-04 |
| AD-02 | Modular monolith; one deployable; service-interface boundaries; no microservices | 0002 | APPROVED | 2026-09-04 |
| AD-03 | REST/JSON, `/api/v1`; action sub-resources for complaint transitions | 0003 | APPROVED | 2026-09-04 |
| AD-04 | `LocalBody → Village/Municipality → Ward → Asset → Complaint → Maintenance`; multi-local-body | 0004 | APPROVED (structure) — **OQ-39 terminology OPEN** | 2026-09-04 |
| AD-05 | D7 lifecycle + `PENDING→REJECTED`; no `RETURNED` status; rework = `RESOLVED→IN_PROGRESS` + event + `returned_count` | 0005 | APPROVED (approach) — **OQ-38 residual OPEN** | 2026-09-04 |
| AD-06 | Spring Security; Argon2id (BCrypt fallback); JWT access + rotating refresh; server-side | 0006 | APPROVED | 2026-09-04 |
| AD-07 | Server-side RBAC (ADMIN/OFFICER/WORKER/CITIZEN); UI is not a security boundary | 0012 | APPROVED | 2026-09-04 |
| AD-08 | Staff-assisted citizen password reset = MVP default; no secure self-service claim | 0007 | APPROVED (MVP default) — **OQ-40 OPEN** | 2026-09-04 |
| AD-09 | Server filesystem/object-like media + DB metadata; upload validation + re-encode + EXIF strip; no mandatory AV | 0008 | APPROVED | 2026-09-04 |
| AD-10 | Flutter OSM-based map; lat/long + Ward ID; no PostGIS/polygons; bounded markers | 0013 | APPROVED — **tile provider OPEN** | 2026-09-04 |
| AD-11 | In-app notifications persisted in PostgreSQL + client polling; no SMS/WhatsApp/email | 0009 | APPROVED | 2026-09-04 |
| AD-12 | Audit trail for important business/security actions (actor/action/record/time/note); not every click | 0014 | APPROVED | 2026-09-04 |
| AD-13 | Server-side PDF + Excel (Apache POI + OpenPDF/simple PDF); Complaint report + Asset register | 0011 | APPROVED | 2026-09-04 |
| AD-14 | Backend SQL aggregation for analytics; Flutter `fl_chart`; no separate analytics platform | 0015 | APPROVED | 2026-09-04 |
| AD-15 | Inventory = Secondary; one central store initially | 0016 | APPROVED | 2026-09-04 |
| AD-16 | Budget = Secondary; allocated/actual/remaining; worker cost ≠ officer official expenditure | 0016 | APPROVED | 2026-09-04 |
| AD-17 | SLA = reporting-only; no auto-escalation in MVP | 0016 | APPROVED — **OQ-41 target values OPEN** | 2026-09-04 |
| AD-18 | Nightly PostgreSQL + media backup; ≥7-day retention; separate location; tested restore; MANIFEST/checksums | 0010 | APPROVED | 2026-09-04 |
| AD-19 | Single host (app + PostgreSQL + media); HTTPS/TLS; no k8s/orchestration; SPOF documented | 0017 | APPROVED — **OQ-17 availability OPEN** | 2026-09-04 |
| AD-20 | Riverpod for Flutter state management | 0018 | APPROVED | 2026-09-04 |
| AD-21 | MVP online-only; offline + sync = Future | 0017 | APPROVED | 2026-09-04 |
| AD-22 | Security baseline accepted (SECURITY_ARCHITECTURE.md); no dedicated AV in MVP | SECURITY_ARCHITECTURE.md | APPROVED | 2026-09-04 |
| AD-23 | Performance baseline: pagination, indexes, efficient queries, pooling, bounded/bbox map queries, bounded payloads | 0019 | APPROVED — **OQ-14 scale numbers OPEN** | 2026-09-04 |
| AD-24 | Six-member ownership model retained (SYSTEM_ARCHITECTURE §17); everyone understands the whole system | SYSTEM_ARCHITECTURE.md §17 | APPROVED | 2026-09-04 |
| AD-25 | AI not foundational; core workflow independent of AI; AI features Future/Secondary, must not block MVP | 0016 | APPROVED | 2026-09-04 |

## 5. Open questions / missing details

The architecture sign-off approved an **architectural approach** for three OQs but
**did not close the underlying requirements questions** — they remain OPEN:

- **OQ-38** — *approach* approved (AD-05: `RETURN` event, no `RETURNED` state).
  OPEN: return-reason taxonomy, whether the UI shows a "returned" indicator.
- **OQ-39** — *two-level structure* approved (AD-04). OPEN: the final
  LocalBody / Village/Municipality **terminology**.
- **OQ-40** — *MVP default* approved (AD-08: staff-assisted reset). OPEN: whether to
  add a recovery-code option or change D16/D21 to allow an SMS/email channel. **No
  secure self-service is claimed.**

**Still fully OPEN / PENDING (do not assume values):**
- **OQ-14** — system scale / capacity numbers. None approved (AD-23 keeps
  scale-sensitive choices flexible; measurement plan in `SYSTEM_ARCHITECTURE.md` §15).
- **OQ-17** — MVP availability target (%/uptime). None approved; do **not** invent
  one (AD-19 documents the single-host SPOF; `SYSTEM_ARCHITECTURE.md` §16).
- **OQ-25** — detailed complaint-reopen policy (feature is Future/Secondary).
- **OQ-36** — university DSN3099 milestones / rubric / deadlines. **PENDING**
  faculty material; no dates to be invented.
- **OQ-37** — citizen rating scale + one-time feedback-text handling
  (`feedback.rating.max` is config; default proposed 5).
- **OQ-41** — SLA target values per priority (deferred; Secondary feature only).
- **OQ-42** — citizen recovery request persistence. ADR-0007's approved
  staff-assisted flow (`POST /auth/citizen/recovery/request` → staff
  `.../resolve`) implies a pending-request record; `DATA_MODEL.md` defines
  none. Unresolved: (A) no persisted record — the office/staff interaction is
  a manual process — vs (B) persist requests, which requires `DATA_MODEL.md`
  to first define the entity/lifecycle/fields/retention rules. Neither chosen;
  ADR-0007 not reinterpreted. Must be resolved before the Auth/User module is
  built. *(Identified 2026-09-12, baseline-schema architecture review.)*
- **Map tile provider / licensing** (AD-10 open sub-item) — choose before the map
  screens are built.

Still-missing project context (not a requirements gap): team size/roles/process;
specific deployment host; Git remote.

## 6. Near-term plan (process, not architecture)

1. ✅ **Done** — Requirements captured and **baselined** as `REQUIREMENTS.md` v1.0
   (2026-09-04), decisions D1–D37.
2. ✅ **Done (2026-09-04)** — Architecture review: `docs/architecture/` (system,
   data model, state machine, API, security) + ADRs 0001–0019.
3. ✅ **Done (2026-09-04)** — **Architecture review & team sign-off.** Decisions
   **AD-01 … AD-25** APPROVED and recorded in §4; ADR statuses → ACCEPTED; doc
   statuses → TEAM-APPROVED. OQ-38/OQ-39/OQ-40 approach approved but the OQs stay
   OPEN; OQ-14/OQ-17/OQ-25/OQ-36/OQ-37/OQ-41 preserved OPEN.
4. **← IN PROGRESS: Environment / prerequisite readiness** (`TASKS.md` §1), stepwise:
   - **E1 ✅ PASS (2026-09-04)** — Eclipse Temurin **JDK 21.0.12.1 LTS**
     (`C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot`); Machine
     `JAVA_HOME` → JDK 21; `java`/`javac` = 21 in a fresh shell; Java 24 preserved.
   - **E2 ✅ PASS (2026-09-04)** — **Apache Maven 3.9.16**
     (`C:\Users\hp\apache-maven-3.9.16`, official Apache zip, SHA-512 verified);
     User `MAVEN_HOME` + PATH; `mvn -version` runs on **JDK 21** (not Java 24).
   - **E3 ✅ PASS (2026-09-04)** — **Flutter 3.47.2 (stable)** + bundled **Dart
     3.13.2** (`C:\src\flutter`, official Google archive, SHA-256 verified); User
     PATH gained `C:\src\flutter\bin`. Fixed a cross-NTFS-volume pub-cache issue
     (`PUB_CACHE` → `C:\Users\hp\.pub-cache`, same volume as the SDK/TEMP; no
     deletions). `flutter doctor` green on Flutter itself; Android toolchain
     correctly not yet installed (→ E4); Visual Studio (Windows-desktop Flutter
     target) not applicable to CAMS's Android-only architecture.
   - **E4 ✅ PASS (2026-09-04)** — **Android SDK/toolchain** at `C:\Android\Sdk`
     (deliberately placed off `AppData`, confirmed same NTFS volume as
     `C:\src`/`%TEMP%`, per the E3 lesson) via the official Android SDK
     Command-line Tools (SHA-256 verified). Platform-Tools 37.0.1, Build-Tools
     36.0.0, Platform android-36, Emulator 37.1.11, system image
     `android-36;google_apis;x86_64`; all licenses accepted. `ANDROID_HOME` =
     `ANDROID_SDK_ROOT` = `C:\Android\Sdk` (User scope, consistent). One AVD
     (`Medium_Phone_API_36`) created, booted with **WHPX hardware acceleration
     confirmed working** (Hyper-V already active; no BIOS change needed), seen by
     `adb devices` and `flutter devices`, then shut down. `flutter doctor -v` →
     **Android toolchain [√]**. Android Studio not installed (not required). E1/E2/E3
     reverified unchanged.
   - **E5 ✅ PASS (2026-09-05)** — **PostgreSQL 17.11** (official EDB/PostgreSQL
     Global Development Group distribution, SHA-256 verified) at
     `C:\Program Files\PostgreSQL\17`, data dir `C:\Program Files\PostgreSQL\17\data`
     (installer default; confirmed not a reparse point, same system volume as the
     rest of the toolchain). Service `postgresql-x64-17` running/automatic; port
     5432 listening; `psql`/`pg_dump`/`pg_restore`/`postgres` all 17.11, added to
     **User** PATH. Connectivity proved (`SELECT version();`). Created an **empty**
     `cams_dev` database owned by a dedicated **non-superuser** `cams_dev` role —
     no tables/schema/Flyway files. `pg_dump`/`pg_restore` functionally
     smoke-tested. Auth restricted to localhost via `scram-sha-256`
     (`pg_hba.conf`); no firewall rule opens 5432. **No password recorded,
     displayed, or committed** — credentials live only in the user-set installer
     password and the standard, non-repo `%APPDATA%\postgresql\pgpass.conf`. One
     UAC approval was required for the service install (the user ran the
     installer directly). E1–E4 reverified unchanged.
   - **E6 ✅ PASS (2026-09-05)** — **Git repository initialised** at the CAMS
     root, default branch **`main`** (Git 2.51.0). `.gitignore` reviewed and
     extended *before* the first commit (PostgreSQL credential patterns, Android
     signing files, broader secret extensions, logs/temp, local Claude Code
     scratch state — on top of the existing Java/Flutter/Android/OS coverage).
     Git identity resolves from the existing global config
     (`Kaustubh <guptakaustubh03@gmail.com>`) — correct, left unchanged. Staged
     and inspected the 38 existing project-control/doc files (no
     passwords/tokens/keys/pgpass/binaries found) and made the first commit,
     **`6b689ff`** — "chore: baseline CAMS project documentation". Working tree
     clean; **no remote configured** (GitHub intentionally not set up — not
     requested), as of this step (2026-09-05).
     **Git note (current state, 2026-09-12):** this has since changed — a
     GitHub `origin` remote is now configured and `main` is synchronized with
     `origin/main` at `d53b9fc`. An earlier history rewrite (which removed the
     Claude co-author attribution from early commits) replaced the local
     history described above; the pre-rewrite history, including this
     `6b689ff` commit, is preserved on the local branch
     `backup-before-remove-claude`, kept intentionally as a safety branch.
   - **E7 ✅ PASS (2026-09-05) — Final environment readiness audit.**
     Re-verified E1–E6 end-to-end with live proof, not just version strings:
     Maven confirmed running on Java 21; `flutter doctor -v` Android toolchain
     **[√]**; the `Medium_Phone_API_36` emulator was **booted live**
     (WHPX-accelerated), detected by `adb devices`, then shut down cleanly;
     PostgreSQL connectivity and the empty `cams_dev` DB/non-superuser role
     reverified via the existing `pgpass.conf` mechanism with **no credentials
     displayed**; Git baseline commit `6b689ff` intact, no secrets/binaries
     tracked or present in the project tree. **Readiness decision: READY WITH
     NON-BLOCKING WARNINGS** — (1) Visual Studio absent in `flutter doctor`
     (correctly irrelevant, Android-only architecture), (2) `postgresql.conf`
     `listen_addresses='*'` (mitigated by `pg_hba.conf`/no firewall rule, per
     E5), (3) a harmless internal `.git/sg-hook-once-*` session-tooling
     artifact (untracked). Nothing installed, nothing architectural changed.
   - **Remaining:** (optional) Docker; document the setup. *(GitHub remote is
     no longer outstanding — see the Git note above.)*
5. ✅ **Done (2026-09-05) — Implementation Phase 1: project skeleton.**
   `backend/` — Spring Boot **4.1.1** on **Java 21.0.12.1**, Maven; dependencies
   limited to the approved ADR-0001 stack (web, data-jpa, security, validation,
   actuator, Flyway, PostgreSQL driver — no Spring Modulith, no Docker,
   no Testcontainers, no message broker); one package per approved module
   (Auth/User, LocalBody/Ward, AssetCategory, Asset, Complaint, Maintenance,
   Feedback, Notification, Audit, Reporting/Analytics, Media) each holding only
   a `package-info.java` describing its future scope; deny-by-default
   `SecurityFilterChain` permitting only `GET /health`; `application.yml` reads
   DB credentials from `SPRING_DATASOURCE_USERNAME`/`_PASSWORD` env vars with
   **no credential in any file**; Flyway disabled and `ddl-auto: none` (no
   schema exists yet). Verified: `mvn compile`/`mvn test` → BUILD SUCCESS;
   packaged jar started standalone, `GET /health` → `200 UP`, stopped cleanly.
   `mobile/` — `flutter create --platforms android` (org `com.cams`); minimal
   `CamsApp` shell (Material 3 + Riverpod `ProviderScope`, one screen, no
   navigation/business screens). Verified: `flutter pub get` /
   `analyze` (0 issues) / `test` (1/1 pass); built + installed the debug APK on
   the `Medium_Phone_API_36` emulator, confirmed the shell rendered with no
   crash, then cleaned up (uninstalled app, shut emulator down). **No domain
   tables were created; `cams_dev` remains empty.** Full detail, including a
   runtime DB-connectivity discovery that was investigated and confirmed
   benign, is recorded in `memory/MEMORY.md`. Nothing in `docs/requirements/`
   or `docs/decisions/` was changed.
6. Iterate feature by feature (MVP scope first, Area 4 critical path), keeping
   tests and project-state files current.
   - ✅ **Done (2026-09-12) — Baseline PostgreSQL schema (Flyway
     `V1__init_schema.sql`).** Physical schema translated directly from
     `docs/architecture/DATA_MODEL.md` §4/§8: all 17 MVP tables, 14 native
     enum types matching the approved D-number value sets exactly, the org
     hierarchy (ADR-0004), denormalised scoping, the DATA_MODEL §11 indexes,
     and the called-out uniqueness invariants (one-active-assignment,
     one-feedback, one-maintenance-history per complaint). Secondary
     (Inventory/Budget/SLA) and `citizen_recovery` (OQ-40, ADR-0007) tables
     deliberately not created. `spring.flyway.enabled` → `true`;
     `ddl-auto` stays `none`. Verified against the real `cams_dev` DB
     (`mvn test`, `psql` catalog inspection, `/health` with Flyway on). See
     `TASKS.md` §6 and `memory/MEMORY.md` for full detail. This is a schema
     foundation only — no Auth/Asset/Complaint business logic yet.

**Gate:** the environment / prerequisite readiness step and the Phase 1
skeleton are both complete, and the baseline database schema now exists. No
**feature/business-logic** implementation (authentication, assets, complaints,
maps, maintenance, dashboards, reports, etc.) has started — that is the next
gated step, one feature at a time.
