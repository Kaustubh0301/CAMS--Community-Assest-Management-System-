# CAMS — Tasks

**Status values:** NOT STARTED · IN PROGRESS · BLOCKED · DONE
**Rule:** never mark a task DONE without corresponding code / tests / docs evidence.
**Last updated:** 2026-09-12 (baseline schema task; OQ-42 recorded)

**Phase:** Requirements **BASELINED (v1.0)**; architecture **TEAM-APPROVED
(2026-09-04)** — AD-01…AD-25 / ADRs 0001–0019. Environment / prerequisite
readiness: **E1–E7 all PASS** — final audit (E7, 2026-09-05) classifies the
environment **READY WITH NON-BLOCKING WARNINGS** (JDK 21, Maven, Flutter/Dart,
Android SDK + live-tested emulator, PostgreSQL, Git repo baseline established
at E6/E7 time). **Remaining, non-blocking:** Docker decision. **Current Git
state (2026-09-12):** `main` is at `d53b9fc`, synchronized with the configured
`origin` remote — see the Git note under E6 in §1 below.
**Implementation Phase 1 — project skeleton: DONE (2026-09-05, PASS WITH
WARNINGS)** — `backend/` (Spring Boot 4.1.1 / Java 21) and `mobile/` (Flutter,
Riverpod, Material 3) skeletons created and runnability-verified; **no business
logic, no domain tables, no CAMS screens.** See §3–§5 below and
`memory/MEMORY.md` for full detail. **First backend feature — baseline
PostgreSQL schema (Flyway `V1__init_schema.sql`): DONE (2026-09-12)** — see §6
below and `memory/MEMORY.md`. Sections §7–§19 remain **GATED** — no
domain/business-logic (Auth, Asset, Complaint, etc.) implementation has
started; the schema alone does not implement any FR.

## 0. Project control & documentation
- [ ] NOT STARTED — Review this control/documentation structure
- [x] DONE (2026-09-04) — Fill `docs/requirements/` with real, agreed requirements
  *(`docs/requirements/REQUIREMENTS.md` **v1.0 BASELINE**; decisions D1–D37
  incorporated; MVP/Secondary/Future scope defined; OPEN items in REQUIREMENTS.md
  §15.3 and `PLAN.md` §5)*
- [x] DONE (2026-09-04) — Architecture review: `docs/architecture/` (SYSTEM,
  DATA_MODEL, COMPLAINT_STATE_MACHINE, API, SECURITY) + ADRs 0001–0019
- [x] DONE (2026-09-04) — **Architecture team review & sign-off.** Decisions
  **AD-01…AD-25 APPROVED**; recorded in `PLAN.md` §4; ADRs → ACCEPTED; architecture
  docs → TEAM-APPROVED. Requirements v1.0 unchanged.
- [ ] NOT STARTED — Track / resolve the preserved OPEN items as information arrives:
  **OQ-14** (scale), **OQ-17** (availability), **OQ-25** (reopen policy), **OQ-36**
  (university dates — PENDING faculty), **OQ-37** (rating scale), **OQ-38** residual
  (return-reason taxonomy / UI indicator), **OQ-39** (final org terminology),
  **OQ-40** (citizen recovery beyond staff-assisted), **OQ-41** (SLA target values),
  **OQ-42** (citizen recovery request persistence — no entity defined in
  DATA_MODEL.md for ADR-0007's pending-request step; found 2026-09-12),
  and the **map tile-provider / licensing** decision (AD-10 / ADR-0013).

## 1. Environment / prerequisite readiness  ← ACTIVE PHASE
*(prerequisites per `SYSTEM_ARCHITECTURE.md` §18; technology now APPROVED — AD-01.
Implementation §3–§19 unblocks only when this section is complete.)*
- [ ] NOT STARTED — Decide and document required tool versions (pin them)
- [x] DONE (2026-09-04) — **E1: JDK 21 LTS installed + configured.** Eclipse
  Temurin **21.0.12.1+1 LTS** (winget `EclipseAdoptium.Temurin.21.JDK`) at
  `C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot`. Machine `JAVA_HOME`
  set to that path by the MSI; its `\bin` prepended to Machine PATH, so a fresh
  shell's `java`/`javac` = 21. **Java 24 (Oracle, `C:\Program Files\Java\jdk-24`)
  left installed** and still usable via its full path. IntelliJ JBR 21 untouched
  (not used as `JAVA_HOME`). *Warning:* the default machine-wide `java` flipped
  24 → 21 — other software assuming `java`=24 is affected; to keep 24 as the global
  default, move `…\Java\jdk-24\bin` ahead of the Adoptium entry in Machine PATH and
  leave `JAVA_HOME`=21 for CAMS.
- [x] DONE (2026-09-04) — **E2: Apache Maven installed + verified.** **Apache Maven
  3.9.16** (official Apache `downloads.apache.org` bin zip, SHA-512 verified) at
  `C:\Users\hp\apache-maven-3.9.16`. User `MAVEN_HOME` set to that path;
  `…\bin` appended to **User** PATH (no admin; no existing entries changed).
  `mvn -version` (fresh shell) → *Apache Maven 3.9.16*, **Java 21.0.12.1 (Eclipse
  Adoptium), runtime `C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot`** —
  Maven runs on the E1 JDK 21, **not** Java 24. Compatible with Spring Boot 3.x
  (needs Maven ≥ 3.6.3). Java 24 and the E1 JDK 21 setup unchanged.
- [x] DONE (2026-09-04) — **E3: Flutter SDK + bundled Dart installed + verified.**
  Official **Flutter 3.47.2 (stable channel)**, bundled **Dart 3.13.2**, from the
  official Google archive (`storage.googleapis.com/flutter_infra_release`,
  SHA-256 verified), extracted to `C:\src\flutter` (no spaces, not inside the CAMS
  project dir). User `PATH` gained `C:\src\flutter\bin` (appended; no existing
  entries changed; no `FLUTTER_HOME` needed). `flutter --version` / `dart
  --version` / `where flutter` / `where dart` all resolve correctly to this SDK;
  Dart is the **bundled** one (no separate standalone Dart installed).
  `flutter doctor -v`: Flutter itself **[√]**; the only issues are **[X] Android
  toolchain** (expected — deferred to **E4**, not installed on purpose) and
  **[X] Visual Studio** (Windows-desktop Flutter target — **not part of CAMS's
  approved Android-only architecture**, irrelevant, not installed).
  *Mid-install issue found and fixed (non-destructive):* the machine's default Dart
  package cache (`%LOCALAPPDATA%\Pub\Cache`) sits on a **different NTFS volume**
  than `C:\src` and `%TEMP%` (confirmed via `fsutil`; no misconfigured env var, no
  visible junction — likely AV/security-software virtualization of `AppData`;
  Windows Defender + McAfee both present). This broke pub's atomic rename step
  (`errno 17`/`ERROR_NOT_SAME_DEVICE`). Fixed by setting **User** `PUB_CACHE` =
  `C:\Users\hp\.pub-cache` (same volume as the SDK/TEMP) — the standard, documented
  Dart env var for relocating the cache. Nothing was deleted; the old (unused,
  `_temp`-only) cache directory was left in place.
- [x] DONE (2026-09-04) — **E4: Android SDK + toolchain installed, licensed, and
  verified.** Installed via the official **Android SDK Command-line Tools**
  (`dl.google.com/android/repository`, build `15859902`, SHA-256 verified) —
  Android Studio was **not** installed (not required; kept the setup minimal/CLI,
  consistent with E1–E3). SDK root: **`C:\Android\Sdk`** — deliberately chosen
  (not the default `%LOCALAPPDATA%\Android\Sdk`) after `fsutil` confirmed it sits
  on the **same NTFS volume** as `C:\src\flutter` and `%TEMP%` (the E3 cross-volume
  lesson applied proactively). Installed components: **Platform-Tools 37.0.1**,
  **cmdline-tools "latest"** (`C:\Android\Sdk\cmdline-tools\latest`), **Platform
  android-36** (compileSdk/targetSdk 36 — read directly from the installed
  Flutter 3.47.2 Gradle plugin defaults, which also default `minSdk=24`; CAMS's
  own `minSdkVersion` will be set to **26** per D26 at app-creation time — that is
  a build-file setting, not a separate SDK install), **Build-Tools 36.0.0**,
  **Emulator 37.1.11**, and **system image `system-images;android-36;google_apis;
  x86_64`**. All Android SDK licenses accepted (`flutter doctor --android-licenses`
  → "All SDK package licenses accepted"). Env vars (**User** scope): `ANDROID_HOME`
  = `ANDROID_SDK_ROOT` = `C:\Android\Sdk` (kept identical, no contradiction); User
  `PATH` gained `cmdline-tools\latest\bin`, `platform-tools`, `emulator`. No
  physical Android device was connected, so created **one** AVD, **`Medium_Phone_
  API_36`** (medium_phone profile, Android 16/API 36, x86_64, at
  `C:\Users\hp\.android\avd\` — also confirmed on the correct volume). Booted it
  headless to test acceleration: log confirmed **"WHPX on Windows … detected —
  Windows Hypervisor Platform accelerator is operational"** (Hyper-V already
  active on this machine; no BIOS changes needed); it reached `adb devices` →
  `device` (fully booted) and `flutter devices` listed it
  (`sdk gphone64 x86 64 … Android 16 (API 36) (emulator)`); then shut down cleanly
  (`adb emu kill`) to free resources. `flutter doctor -v` → **Android toolchain
  [√]**. E1 (`JAVA_HOME`=Temurin 21, Java 24 preserved), E2 (`MAVEN_HOME`), and E3
  (`PUB_CACHE`) all reverified unchanged throughout.
- [x] DONE (2026-09-05) — **E5: PostgreSQL 17 server + client tools installed,
  configured, and verified.** **PostgreSQL 17.11**, official EDB/PostgreSQL Global
  Development Group Windows distribution (installer SHA-256 verified against
  winget's own manifest metadata before use). Install dir
  `C:\Program Files\PostgreSQL\17`; data dir `C:\Program Files\PostgreSQL\17\data`
  (installer's own default location — confirmed not a reparse point / on the same
  system volume as the rest of the toolchain). Windows service
  **`postgresql-x64-17`** — Running, StartType Automatic. Listening on **port
  5432** (confirmed via `Get-NetTCPConnection`). Client tools `psql`, `pg_dump`,
  `pg_restore`, `postgres` all report **17.11**, resolved via `C:\Program
  Files\PostgreSQL\17\bin` (added to **User** PATH, appended only). Connectivity
  proved with `SELECT version();` over `localhost:5432` → *"PostgreSQL 17.11 on
  x86_64-windows…"*. Created an **empty** development database **`cams_dev`**
  owned by a dedicated **non-superuser** login role **`cams_dev`**
  (`rolsuper=false`) — no tables/schema/Flyway files. `pg_dump`/`pg_restore`
  functionally smoke-tested against the empty `cams_dev` DB (dump + TOC listing
  succeeded). Security: `pg_hba.conf` restricts all auth to `127.0.0.1`/`::1` via
  `scram-sha-256`; no Windows Firewall rule opens 5432 *(note: `postgresql.conf`
  has the installer's default `listen_addresses='*'`, broader than strictly
  needed for local dev, but not reachable externally per the above — left as
  installed as instructed)*. Credentials: superuser password set by the user
  directly in the installer GUI (never seen by this session); the `cams_dev` role
  password was generated and stored **only** in the standard, non-repo
  `%APPDATA%\postgresql\pgpass.conf` — **no password recorded, displayed, or
  committed anywhere.** E1–E4 reverified unchanged throughout.
- [ ] NOT STARTED — Decide whether to use Docker (for Testcontainers integration tests) or a local PostgreSQL for tests
- [x] DONE (2026-09-05) — **E6: Git repository/workflow baseline established.**
  Git **2.51.0** confirmed. Repository initialised at `E:\EPICS 26\CAMS` (was not
  a repo before), default branch **`main`**. `.gitignore` reviewed and extended
  *before* the first commit — added PostgreSQL credential patterns
  (`pgpass.conf`, `.pgpass`, `*.pgpass`), Android signing (`key.properties`),
  broader secrets (`*.key`, `*.crt`), logs/temp (`logs/`, `*.temp`), and local
  Claude Code scratch-state patterns (`.claude/settings.local.json`,
  `.claude/*.local.*`) — on top of the existing Java/Maven/Gradle/Flutter/Dart/
  Android/iOS/OS/IDE coverage. Git identity resolves from the existing **global**
  config (`Kaustubh <guptakaustubh03@gmail.com>`) — already correct, left
  unchanged. Staged only the 38 existing project-control/doc files (`.gitignore`,
  `CLAUDE.md`, `PLAN.md`, `TASKS.md`, `docs/**`, `memory/MEMORY.md`,
  `.claude/.gitkeep`); inspected the staged list and diff content before
  committing — no passwords/tokens/keys/pgpass/binaries/installers found (one
  filename match on "password" was `docs/decisions/0007-…-password-recovery-…`,
  a design-decision doc, not a credential). First commit **`6b689ff`** — "chore:
  baseline CAMS project documentation". Working tree clean; **no remote
  configured** (GitHub setup intentionally not done — not requested), as of
  this step (2026-09-05). **Git note (current state, 2026-09-12):** this has
  since changed — a GitHub `origin` remote is now configured and `main` is
  synchronized with `origin/main` at `d53b9fc`. An earlier history rewrite
  (which removed the Claude co-author attribution from early commits) replaced
  the local history described above; the pre-rewrite history, including this
  `6b689ff` commit, is preserved on the local branch
  `backup-before-remove-claude`, kept intentionally as a safety branch. No
  application/database code included.
- [x] DONE (confirmed 2026-09-12) — GitHub remote `origin` is configured;
  `main` is synchronized with `origin/main` at `d53b9fc` *(see Git note above —
  date/actor of the original setup not recorded here)*
- [x] DONE (2026-09-05) — **E7: Final environment readiness audit — READY WITH
  NON-BLOCKING WARNINGS.** Full re-verification of E1–E6, live end-to-end proof
  (not just version checks): Java 21 (Temurin) is `JAVA_HOME`, Java 24 preserved;
  Maven 3.9.16 confirmed running on Java 21; Flutter 3.47.2 / Dart 3.13.2 healthy,
  `flutter doctor -v` → Android toolchain **[√]**; Android SDK (`C:\Android\Sdk`,
  platform android-36, build-tools 36.0.0) confirmed, **`Medium_Phone_API_36`
  emulator booted live** (WHPX-accelerated), detected by `adb devices`
  (`emulator-5554 device`) and shut down cleanly; PostgreSQL 17.11 service
  running/automatic on port 5432, connectivity + `cams_dev` (empty, non-superuser
  owner) reverified via the existing `pgpass.conf` mechanism with **no
  credentials displayed**; Git repo on `main`, baseline commit `6b689ff` intact,
  working tree has only the expected E6/E7 doc updates as uncommitted changes, no
  secrets/binaries/installers tracked or present anywhere in the project tree.
  **Non-blocking warnings noted:** (1) `flutter doctor`'s only red item is
  Visual Studio — correctly irrelevant to CAMS's Android-only architecture; (2)
  `postgresql.conf` still has the installer default `listen_addresses='*'`
  (mitigated by `pg_hba.conf` + no firewall rule, per E5); (3) a stray
  `.git/sg-hook-once-*` marker file was observed inside `.git/` — an internal
  session-tooling artifact, not tracked by Git, harmless. No architecture
  decision was changed; no dependency/tool was installed during this audit.
- [ ] NOT STARTED — Document setup steps under `docs/`

## 2. Architecture / system design  — ✅ COMPLETE & TEAM-APPROVED (2026-09-04)

- [x] DONE (2026-09-04) — Architecture review covering every MVP requirement;
  Secondary/Future modules isolated (`SYSTEM_ARCHITECTURE.md`, §1A AD register)
- [x] DONE (2026-09-04) — ADRs **0001–0019 ACCEPTED** (backend stack; modular
  monolith; REST; org hierarchy; complaint state machine; auth; RBAC; citizen
  recovery; media; map/GIS; notifications; audit; reporting; analytics/charting;
  deferred-scope isolation; deployment/connectivity; Riverpod; backup/restore;
  performance baseline)
- [x] DONE (2026-09-04) — `DATA_MODEL.md`, `COMPLAINT_STATE_MACHINE.md`,
  `API_ARCHITECTURE.md`, `SECURITY_ARCHITECTURE.md` → **v1.0 TEAM-APPROVED**
- [x] DONE (2026-09-04) — Team sign-off; decisions recorded in `PLAN.md` §4
- [ ] NOT STARTED — *(carried)* Resolve the **map tile-provider / licensing**
  decision (AD-10 / ADR-0013) **before** the map screens are built
- [ ] NOT STARTED — *(carried, build task)* Add an `ArchUnit`/lint check enforcing
  the module-boundary rule (AD-02 / ADR-0002) — after the backend skeleton
- [ ] NOT STARTED — *(carried)* Confirm final org terminology (OQ-39) at physical
  data-model design; decide OQ-38 residual detail then too

## 3. Project initialization
- [x] DONE (2026-09-05) — Initialise Git repository *(done in E6; `main`,
  commit `6b689ff`)*
- [x] DONE (2026-09-05) — **Create backend project skeleton.** `backend/` —
  Spring Boot **4.1.1**, Java **21**, Maven, generated via Spring Initializr
  then corrected (parent POM version has no `.RELEASE` suffix on Boot 4.x —
  verified against Maven Central metadata directly). Package structure under
  `com.cams.backend.*` with one package (+ `package-info.java` documenting its
  approved scope/D-numbers/ADRs) per approved module: `auth`, `localbody`,
  `assetcategory`, `asset`, `complaint`, `maintenance`, `feedback`,
  `notification`, `audit`, `reporting`, `media`, plus `common`/`common.config`
  for cross-cutting plumbing. **No business logic in any module.**
- [x] DONE (2026-09-05) — **Create frontend project skeleton.** `mobile/` —
  `flutter create --platforms android`, org `com.cams`. Minimal `CamsApp`
  (Material 3, `ColorScheme.fromSeed`) wrapped in a Riverpod `ProviderScope`;
  one `HomeShell` screen showing only the app name — **no CAMS screens,
  navigation, or role routing yet.**
- [x] DONE (2026-09-05) — Wire up build / run scripts *(none needed beyond the
  generated Maven Wrapper (`mvnw`) and Flutter's own CLI — both verified
  runnable; no custom scripts added, per dependency-discipline)*
- [ ] NOT STARTED — CI configuration (if used)

## 4. Backend
- [x] DONE (2026-09-05) — **Core application skeleton and configuration.**
  Dependencies (all from the approved ADR-0001 stack, nothing extra):
  `spring-boot-starter-{webmvc,data-jpa,security,validation,actuator}`,
  `flyway-database-postgresql`, `postgresql` driver. Deny-by-default
  `SecurityFilterChain` (only `GET /health` permitted; CSRF/httpBasic/formLogin
  disabled — stateless JSON API, no browser client). `application.yml`:
  datasource URL fixed, username/password from
  `SPRING_DATASOURCE_USERNAME`/`_PASSWORD` env vars (**no credentials in any
  file**); `spring.jpa.hibernate.ddl-auto: none`; `spring.flyway.enabled:
  false` (no migrations exist yet); `/health` exposed via Actuator at the
  approved bare path (API_ARCHITECTURE.md §4.12). Verified: `mvn compile` and
  `mvn test` → BUILD SUCCESS (1/1 tests); packaged jar started standalone on
  **Java 21.0.12.1**, `GET /health` → `200 {"status":"UP"}`, stopped cleanly.
  Deliberately did **not** add Spring Modulith (thematically fitting, but not
  in the approved dependency list). *(One notable runtime discovery,
  investigated and confirmed benign — see `memory/MEMORY.md` "Phase 1
  skeleton" entry: the app genuinely connects to the real `cams_dev` DB at
  startup via the existing `pgpass.conf` mechanism, not via any credential
  supplied by this session.)*
- [ ] NOT STARTED — Persistence layer setup
- [ ] NOT STARTED — Domain model (per approved requirements)
- [ ] NOT STARTED — Error handling, validation, and logging conventions

## 5. Frontend  *(Flutter; Riverpod — AD-20/ADR-0018; fl_chart — AD-14/ADR-0015)*
- [x] DONE (2026-09-05) — **App shell skeleton + Riverpod/Material 3
  wiring** — `mobile/lib/main.dart`, `ProviderScope` + `MaterialApp` +
  minimal `HomeShell`. Verified: `flutter pub get`, `flutter analyze` (0
  issues), `flutter test` (1/1 pass), built + installed debug APK on the
  **`Medium_Phone_API_36`** emulator, confirmed the shell rendered (screenshot)
  with no crash, then uninstalled the app and shut the emulator down cleanly.
  **Navigation, role routing, and real theming are NOT part of this
  skeleton** — remain NOT STARTED below.
- [x] DONE (2026-09-12) — **D26 correction: Android `minSdk` config fix,
  independently verified.** An independent `cams-verifier` audit found
  `mobile/android/app/build.gradle.kts` left `minSdk = flutter.minSdkVersion`,
  which resolves to Flutter's own default (**24**) — contradicting the
  approved **D26** requirement (Android 8.0 / API 26+;
  `docs/requirements/REQUIREMENTS.md` D26, NFR-COMPAT-002). Corrected to an
  explicit `minSdk = 26` (only that one line changed). A second, independent
  `cams-verifier` pass confirmed **26** at every layer — source
  (`build.gradle.kts`), build output (`output-metadata.json` →
  `minSdkVersionForDexing: 26`), and the merged manifest
  (`processDebugManifestForPackage/AndroidManifest.xml` →
  `android:minSdkVersion="26"`) — and re-ran `flutter analyze` (0 issues),
  `flutter test` (1/1 pass), `flutter build apk --debug` (success), all
  passing. **The 24-vs-26 contradiction is resolved.** No requirement,
  architecture decision, or ADR changed; implementation correction only.
- [ ] NOT STARTED — Navigation, role routing, real theming
- [ ] NOT STARTED — EN/HI i18n framework + language selection
- [ ] NOT STARTED — API client layer (JWT + refresh handling)
- [ ] NOT STARTED — Riverpod conventions doc + reference screens
- [ ] NOT STARTED — Shared UI components (status chips, photo picker, on-device image compression)

## 6. Database
- [x] DONE (2026-09-12) — **Physical schema design from `docs/architecture/DATA_MODEL.md`
  + Flyway baseline migration.** `backend/src/main/resources/db/migration/
  V1__init_schema.sql` — translates DATA_MODEL.md §4 (entity catalog) and §8
  (enumerations) directly into PostgreSQL DDL: all 17 MVP tables (`local_body`,
  `village_municipality`, `ward`, `user_account`, `refresh_token`,
  `asset_category`, `asset`, `asset_photo`, `complaint`, `complaint_photo`,
  `complaint_status_history`, `worker_assignment`, `maintenance_history`,
  `feedback`, `notification`, `audit_entry`, `media_object`) and 14 native
  PostgreSQL enum types matching the D-number value sets **exactly**
  (`complaint_status` = exactly D7's 7 values, no `RETURNED`; `asset_status`
  D8; `asset_condition` D24 as an independent field; `role` D1; etc.).
  Implements: org hierarchy per ADR-0004 (`local_body → village_municipality →
  ward`, working names, OQ-39 untouched); denormalised `local_body_id` on
  `asset`/`complaint` per DATA_MODEL §6; `(local_body_id, name)` uniqueness on
  `village_municipality`/`asset_category`; one-active-`worker_assignment`-per-
  complaint via a partial unique index; one-`feedback`-per-complaint and
  one-`maintenance_history`-per-complaint via unique constraints; the
  DATA_MODEL §11 indexes (asset scope+location, complaint scope/reported_by/
  current_assignment, `worker_assignment(worker_id, active)`,
  `complaint_status_history(complaint_id, occurred_at)`,
  `notification(recipient_id, read_at)`, `audit_entry(occurred_at)` /
  `(entity_type, entity_id)` / `(actor_id)`); circular FKs
  (`local_body.created_by → user_account`, `complaint.current_assignment_id →
  worker_assignment`) resolved via deferred `ALTER TABLE ... ADD CONSTRAINT`.
  **Deliberately excluded** (per task scope / ADR-0007 / DATA_MODEL §9):
  `citizen_recovery` (OQ-40 open; staff-assisted-only MVP default needs no
  table), and all Inventory/Budget/SLA Secondary tables. `application.yml`:
  `spring.flyway.enabled` → `true`; `spring.jpa.hibernate.ddl-auto` stays
  `none`. `db/migration/README.md` updated to describe the new baseline.
  **Verified against the real local `cams_dev` PostgreSQL 17.11 database**
  (via the existing `pgpass.conf` mechanism — no credentials handled by this
  session): `mvn compile` / `mvn test` → BUILD SUCCESS (1/1), Flyway applies
  `V1__init_schema.sql` cleanly and `flyway_schema_history` records it
  `success = t`; a second `mvn test` run shows Flyway validates and makes "no
  migration necessary" (repeatable/idempotent); packaged jar started
  standalone, `GET /health` → `200 {"status":"UP"}` with Flyway now enabled,
  then stopped cleanly. `psql` catalog inspection confirmed: all 17 expected
  tables + `flyway_schema_history` (18 total, nothing extra — Hibernate
  created zero schema objects, `ddl-auto: none` holds); all 14 enum types with
  the exact approved value sets; all 37 expected foreign keys (incl. both
  circular ones); all unique/check constraints and all DATA_MODEL §11 indexes
  present; **explicitly confirmed absent:** `citizen_recovery`,
  `inventory_item`, `stock_movement`, `expenditure_entry`,
  `budget_allocation`, `sla_target`. Implementation-style choices made where
  the docs were silent (UUID PKs via `gen_random_uuid()`; native Postgres
  ENUM types over varchar+check; `NUMERIC(12,2)` for `repair_cost`;
  `completion_photo_ids` as a `UUID[]` array column; `feedback.rating_value`
  has only a `>= 1` DB check, no upper bound, because the max is OQ-37-open
  config) — see `memory/MEMORY.md` for the full list. No `docs/requirements/`
  or `docs/decisions/` content changed.
- [x] DONE (2026-09-12) — Migration tooling (Flyway — ADR-0001) and baseline
  migration *(same entry as above — Flyway was already a dependency from the
  Phase 1 skeleton; this task enabled it and supplied the first migration)*
- [ ] NOT STARTED — Seed / reference data strategy (12 asset categories per local body, per D23)
- [ ] NOT STARTED — Backup & restore scripts + runbook per ADR-0010; **run the
  pre-evaluation restore drill** and record it here (NFR-BAK-003)

## 7. Authentication & authorization  *(AD-06/ADR-0006 auth; AD-07/ADR-0012 RBAC)*
- [ ] NOT STARTED — User model + single-role + local-body scope (per DATA_MODEL §4.2)
- [ ] NOT STARTED — Spring Security; **Argon2id** hashing (BCrypt fallback)
- [ ] NOT STARTED — Login / JWT access + rotating refresh / logout / staff reset
- [ ] NOT STARTED — Staff-assisted citizen recovery flow (AD-08/ADR-0007; OQ-40 OPEN)
- [ ] NOT STARTED — Server-side RBAC + local-body scoping + 3-layer worker isolation + authz test suite

## 8. Assets
- [ ] NOT STARTED — Requirements
- [ ] NOT STARTED — Data model
- [ ] NOT STARTED — Backend endpoints
- [ ] NOT STARTED — Frontend screens
- [ ] NOT STARTED — Tests

## 9. GIS  *(AD-10/ADR-0013: Flutter OSM-based map; lat/long + Ward ID; no PostGIS)*
- [ ] NOT STARTED — **Choose map tile provider + confirm licensing/usage terms** (OPEN)
- [ ] NOT STARTED — Backend: bounded/bbox marker endpoint + `maxMarkers` cap
- [ ] NOT STARTED — Flutter OSM map widget; marker filter + lookup; GPS/pin location capture
- [ ] NOT STARTED — Tests

## 10. Complaints
- [ ] NOT STARTED — Requirements
- [ ] NOT STARTED — Data model
- [ ] NOT STARTED — Backend endpoints
- [ ] NOT STARTED — Frontend screens
- [ ] NOT STARTED — Tests

## 11. Maintenance
- [ ] NOT STARTED — Requirements
- [ ] NOT STARTED — Data model
- [ ] NOT STARTED — Backend endpoints
- [ ] NOT STARTED — Frontend screens
- [ ] NOT STARTED — Tests

## 12. Inventory
- [ ] NOT STARTED — Requirements
- [ ] NOT STARTED — Data model
- [ ] NOT STARTED — Backend endpoints
- [ ] NOT STARTED — Frontend screens
- [ ] NOT STARTED — Tests

## 13. Budget
- [ ] NOT STARTED — Requirements
- [ ] NOT STARTED — Data model
- [ ] NOT STARTED — Backend endpoints
- [ ] NOT STARTED — Frontend screens
- [ ] NOT STARTED — Tests

## 14. Analytics  *(AD-14/ADR-0015: backend SQL aggregation; Flutter fl_chart)*
- [ ] NOT STARTED — MVP metrics: asset status/condition counts; complaint counts by status/ward/category/priority (FR-ANLY-001/002)
- [ ] NOT STARTED — Aggregation / query layer (shared with reports)
- [ ] NOT STARTED — Dashboard UI with `fl_chart`; Worker `my-metrics` only (D28)
- [ ] NOT STARTED — Tests

## 15. Reports
- [ ] NOT STARTED — Define report types and output formats
- [ ] NOT STARTED — Report generation backend
- [ ] NOT STARTED — Export / download UI
- [ ] NOT STARTED — Tests

## 16. Testing
- [ ] NOT STARTED — Testing strategy document (`docs/testing/`)
- [ ] NOT STARTED — Backend unit / integration test setup
- [ ] NOT STARTED — Frontend widget / unit test setup
- [ ] NOT STARTED — End-to-end test approach
- [ ] NOT STARTED — Coverage targets and reporting

## 17. Deployment  *(AD-19/AD-21/ADR-0017: single host, HTTPS/TLS, online-only, no orchestration)*
- [ ] NOT STARTED — Choose the specific demo host; obtain a real TLS certificate
- [ ] NOT STARTED — Build / release process (single Spring Boot JAR)
- [ ] NOT STARTED — Environment configuration + secrets handling (env vars / untracked properties; `application-example.properties`)
- [ ] NOT STARTED — Runbook incl. process auto-restart + `/health`; document the single-host SPOF (OQ-17 OPEN)

## 18. Documentation
- [ ] NOT STARTED — Keep `docs/` current with each feature
- [ ] NOT STARTED — API reference (`docs/api/`)
- [ ] NOT STARTED — User / operator guide
- [ ] NOT STARTED — Project report material

## 19. Final evaluation
- [ ] NOT STARTED — Requirements traceability check
- [ ] NOT STARTED — Full test pass and coverage review
- [ ] NOT STARTED — Demo preparation
- [ ] NOT STARTED — Final report and submission
