# CAMS — Tasks

**Status values:** NOT STARTED · IN PROGRESS · BLOCKED · DONE
**Rule:** never mark a task DONE without corresponding code / tests / docs evidence.
**Last updated:** 2026-09-05

**Phase:** Requirements **BASELINED (v1.0)**; architecture **TEAM-APPROVED
(2026-09-04)** — AD-01…AD-25 / ADRs 0001–0019. **Active phase: §1 Environment /
prerequisite readiness — E1–E5 done (JDK 21, Maven, Flutter/Dart, Android SDK,
PostgreSQL); remaining: Docker decision, Git remote/workflow.** Implementation
sections (§3–§19) stay **GATED** until §1 is complete. **No application code
exists.**

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
- [ ] NOT STARTED — Confirm Git workflow + remote; decide whether to use GitHub CLI
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
- [ ] NOT STARTED — Initialise Git repository
- [ ] NOT STARTED — Create backend project skeleton
- [ ] NOT STARTED — Create frontend project skeleton
- [ ] NOT STARTED — Wire up build / run scripts
- [ ] NOT STARTED — CI configuration (if used)

## 4. Backend
- [ ] NOT STARTED — Core application skeleton and configuration
- [ ] NOT STARTED — Persistence layer setup
- [ ] NOT STARTED — Domain model (per approved requirements)
- [ ] NOT STARTED — Error handling, validation, and logging conventions

## 5. Frontend  *(Flutter; Riverpod — AD-20/ADR-0018; fl_chart — AD-14/ADR-0015)*
- [ ] NOT STARTED — App skeleton, navigation, role routing, theming
- [ ] NOT STARTED — EN/HI i18n framework + language selection
- [ ] NOT STARTED — API client layer (JWT + refresh handling)
- [ ] NOT STARTED — Riverpod conventions doc + reference screens
- [ ] NOT STARTED — Shared UI components (status chips, photo picker, on-device image compression)

## 6. Database
- [ ] NOT STARTED — Physical schema design from `docs/architecture/DATA_MODEL.md`
- [ ] NOT STARTED — Migration tooling (Flyway — ADR-0001) and baseline migration
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
