# CAMS — Memory Index

Concise index of persistent project knowledge. Keep entries short; link out, do not
duplicate. **Last updated:** 2026-09-12 (baseline schema task; OQ-42 recorded)

## Project state
- Project: **CAMS** (Community Asset Management System), ~1-year university
  engineering project for villages / Gram Panchayats / municipalities.
- Phase: Requirements **BASELINED (v1.0)**; **architecture TEAM-APPROVED
  (2026-09-04)** — decisions **AD-01…AD-25** / ADRs **0001–0019 ACCEPTED**.
  Environment / prerequisite readiness: **E1–E7 all PASS** (JDK 21, Maven,
  Flutter/Dart, Android SDK, PostgreSQL, Git repo baseline on `main`, final
  audit). **E7 readiness decision: READY WITH NON-BLOCKING WARNINGS**
  (2026-09-05). **Implementation Phase 1 — project skeleton: DONE
  (2026-09-05)** — see "Phase 1 skeleton" entry below. **First backend
  feature — baseline PostgreSQL schema: DONE (2026-09-12)** — see "Baseline
  schema" entry below. No Auth/Asset/Complaint business logic implemented
  yet. Only Docker (optional) remains outstanding, non-blocking, in
  `../TASKS.md` §1. **Git (current, 2026-09-12):** `main` is at `d53b9fc`,
  synchronized with the configured `origin` remote — see the Git note under
  "E6 PASS" below.
- **Requirements baseline:** `../docs/requirements/REQUIREMENTS.md` **v1.0**
  (D1–D37; register in §1). **Unchanged by the architecture work.** Open items in
  §15.3 / `../PLAN.md` §5.
- **Architecture (TEAM-APPROVED):** `../docs/architecture/` —
  `SYSTEM_ARCHITECTURE.md` (§1A = AD register), `DATA_MODEL.md`,
  `COMPLAINT_STATE_MACHINE.md`, `API_ARCHITECTURE.md`, `SECURITY_ARCHITECTURE.md`;
  **ADRs 0001–0019** in `../docs/decisions/` (index + OPEN-map in its README).
  Decision table in `../PLAN.md` §4.

## Approved architecture (AD-01…AD-25 — team-approved 2026-09-04)
- **Modular monolith** (AD-02): one Spring Boot deployable, one PostgreSQL DB;
  modules = Auth/User, LocalBody/Ward, AssetCategory, Asset, Complaint, Maintenance,
  Feedback, Notification, Audit, Reporting/Analytics, Media; Inventory/Budget/SLA
  **isolated Secondary** (feature-flagged, id-only refs); **AI = Future, not
  foundational** (AD-25). Service-interface boundaries; no microservices.
- **Stack (AD-01):** **Java 21 LTS + Spring Boot + Maven + PostgreSQL + Flyway**.
  (Machine has JDK 24 — install/pin 21 in the env step.)
- **Frontend (AD-20, AD-14):** Flutter + **Riverpod** state management + **fl_chart**.
- **API (AD-03):** REST/JSON, `/api/v1`; complaint transitions as action
  sub-resources; consistent validation/errors.
- **Auth (AD-06):** Spring Security; **Argon2id** hashing (BCrypt fallback); short
  JWT access + rotating refresh; server-side; deactivation enforced on refresh.
- **AuthZ (AD-07):** server-side RBAC + local-body scope + 3-layer worker isolation;
  Flutter UI is **not** a security boundary.
- **Citizen recovery (AD-08 / OQ-40 OPEN):** **staff-assisted reset** is the MVP
  default; **no secure self-service claimed** without a trusted channel.
- **Media (AD-09):** server **filesystem/object-like** + `media_object` metadata
  (not bytea); server re-encode + EXIF strip + type/size/dimension validation; no
  mandatory AV (accepted limitation, AD-22).
- **Map (AD-10):** Flutter **OpenStreetMap-based**; store lat/long + Ward ID; **no
  PostGIS, no polygons**; bounded/bbox marker queries. **Tile provider/licensing =
  OPEN, tracked.**
- **Notifications (AD-11):** in-app `notification` rows in PostgreSQL + **client
  polling**; **no SMS/WhatsApp/email**.
- **Audit (AD-12):** append-only `audit_entry` (actor/action/entity/time/note),
  in-transaction, Admin-query; not every click.
- **Reporting (AD-13):** server-side **Apache POI** (Excel) + OpenPDF/simple PDF;
  MVP = Complaint report + Asset register.
- **Backup (AD-18):** nightly `pg_dump -Fc` + media `tar` + MANIFEST/checksums,
  ≥7-day retention, separate location, documented manual restore + pre-eval drill.
- **Deployment (AD-19, AD-21):** **single host** (app + PostgreSQL + media),
  **HTTPS/TLS**, no k8s/orchestration; **SPOF documented**; **online-only MVP**.
- **Org hierarchy (AD-04):** `LocalBody → Village/Municipality → Ward → Asset →
  Complaint → Maintenance`; multi-local-body; denormalised `local_body_id`.
  **Final terminology = OQ-39 OPEN.**
- **Complaint rework (AD-05):** **no `RETURNED` state** — `RESOLVED→IN_PROGRESS` +
  `RETURN` event + `returned_count`; states = exactly D7. **Residual detail =
  OQ-38 OPEN.**
- **Performance (AD-23):** pagination, indexes, efficient queries, connection
  pooling, bounded/bbox map queries, bounded payloads; measurement plan. **Scale
  numbers = OQ-14 OPEN.**
- **Team model (AD-24):** six areas — Core/Backend/Integration · Flutter/UI · Asset
  mgmt · Complaint/Maintenance workflow · DB/GIS/Notifications ·
  Reporting/Analytics/Testing/AI. Everyone understands the whole system.
- **No numbers invented** for scale (OQ-14) or availability (OQ-17).

## Approved product constraints (from D1–D37 — do not contradict without a new decision)
- **4 roles only:** Admin, Officer, Maintenance Worker, Citizen. No leadership role. (D1)
- **1 Flutter mobile app** for all roles; **Android-first, API 26+**; iOS later; no
  web/desktop in MVP. (D5, D26)
- **Online-only MVP**; offline + sync is Future. (D4)
- **PostgreSQL**; daily backup, ≥7 days retention, media included, tested restore. (D36)
- Org hierarchy: **LocalBody → Village/Municipality → Ward → Asset → Complaint**;
  multi-local-body capable, never hardcoded to one village. (D19)
- Every MVP complaint is **linked to a registered asset**. (D3)
- Complaint lifecycle: **PENDING → ASSIGNED → IN_PROGRESS → RESOLVED → VERIFIED →
  CLOSED**, plus **REJECTED** from review. (D7)
- Asset **status** {WORKING, BROKEN, UNDER_MAINTENANCE, DECOMMISSIONED} is
  **separate** from asset **condition** {GOOD, FAIR, POOR, CRITICAL}. (D8, D24)
- Complaint **priority** {LOW, MEDIUM, HIGH, CRITICAL}; Officer sets final. (D9)
- One complaint → **one individual Worker** (no crews). (D15)
- Worker **completion photo mandatory** before RESOLVED; citizen photo optional. (D11)
- Asset **1–5 photos**, compression controls; **categories Admin-configurable**. (D25, D23)
- **In-app notifications only**; **no external integrations**; **no AI** in MVP. (D10, D20)
- Citizens: mobile + password self-register, **no anonymous**, no OTP/Aadhaar;
  cannot delete own history; **passwords hashed only**; simple privacy statement. (D2, D16, D18)
- Citizen **rating + optional one-time feedback** on CLOSED complaints; **no comment
  threads**. (D2, D17)
- Workers see **only their own** task info/metrics. (D28)
- **Audit log** of important actions (actor/action/time/record/note); not every click. (D22)
- Reports **PDF + Excel**; MVP = Complaint report + Asset register report. (D33)
- Inventory (1 central store), budget/expenditure & allocation, advanced analytics/
  reports, SLA reporting (no escalation), QR (role-gated) = **Secondary/Future**. (D29–D35)
- English + Hindi only in MVP; strings externalized. (D12)

## Environment facts (as of 2026-09-04)
- **E1 PASS:** Eclipse **Temurin JDK 21.0.12.1 LTS** at
  `C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot` (winget). Machine
  `JAVA_HOME` = that path; its `\bin` is first on Machine PATH → fresh-shell
  `java`/`javac` = **21**. CAMS toolchain JDK (AD-01 / ADR-0001).
- **E2 PASS:** **Apache Maven 3.9.16** at `C:\Users\hp\apache-maven-3.9.16`
  (official Apache bin zip, SHA-512 verified). User `MAVEN_HOME` set; `…\bin` on
  **User** PATH. `mvn -version` → Maven 3.9.16 running on **JDK 21** (Eclipse
  Adoptium, `C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot`), **not**
  Java 24. `~/.m2` not created yet (appears on first build). Meets Spring Boot 3.x's
  Maven ≥ 3.6.3 requirement.
- Oracle **JDK 24.0.2** still installed (`C:\Program Files\Java\jdk-24`), still on
  PATH (after the Temurin entry), usable via full path — preserved. IntelliJ JBR 21
  present (IDE-managed, not used as JAVA_HOME).
- **E3 PASS:** **Flutter 3.47.2 (stable)** + bundled **Dart 3.13.2** at
  `C:\src\flutter` (official Google archive, SHA-256 verified). User `PATH`
  gained `C:\src\flutter\bin`; no `FLUTTER_HOME` needed; bundled Dart used (no
  separate Dart SDK). `flutter doctor`: Flutter itself green; Android toolchain
  correctly absent (**E4**, not installed on purpose); Visual Studio (Windows-
  desktop target) not applicable to CAMS (Android-only architecture).
  **Known machine quirk (fixed, non-destructive):** the default Dart pub cache
  (`%LOCALAPPDATA%\Pub\Cache`) lives on a **different NTFS volume** than
  `C:\src`/`%TEMP%` (confirmed via `fsutil`; no bad env var, no visible junction —
  likely AV/security-software virtualizing `AppData`; both Windows Defender and
  McAfee are present). This broke `pub`'s atomic rename (`errno 17` /
  `ERROR_NOT_SAME_DEVICE`). **Fix:** User `PUB_CACHE` = `C:\Users\hp\.pub-cache`
  (same volume as the SDK/TEMP) — the standard Dart env var for this. **Remember
  this for E4+**: if any future tool (Gradle, Android SDK Manager, etc.) does
  similar temp-then-rename installs, the same cross-volume symptom could recur;
  the fix pattern is the same — redirect its cache/temp to a path under
  `C:\Users\hp` or `C:\src` (confirmed-good volume), not deep under
  `AppData\Local` (confirmed-bad volume for large/atomic operations).
- **E4 PASS:** **Android SDK/toolchain** at `C:\Android\Sdk` — deliberately
  chosen off `AppData` and confirmed on the **same NTFS volume** as
  `C:\src\flutter`/`%TEMP%` before use (applying the E3 lesson proactively).
  Installed via the official Android SDK **Command-line Tools** only (SHA-256
  verified) — **Android Studio not installed** (not required; kept minimal/CLI
  like E1–E3). Components: **Platform-Tools 37.0.1**, **cmdline-tools "latest"**,
  **Platform android-36**, **Build-Tools 36.0.0**, **Emulator 37.1.11**, system
  image `android-36;google_apis;x86_64`. compileSdk/targetSdk **36** was read
  directly from the bundled Flutter 3.47.2 Gradle plugin defaults (not guessed);
  CAMS's **minSdkVersion 26** (D26) is a future app-level `build.gradle` setting,
  not a separate SDK install. All licenses accepted. **User** env vars:
  `ANDROID_HOME` = `ANDROID_SDK_ROOT` = `C:\Android\Sdk` (kept identical); PATH
  gained `cmdline-tools\latest\bin`, `platform-tools`, `emulator`. No physical
  device available → created **one** AVD `Medium_Phone_API_36` (medium_phone,
  API 36, x86_64); booted headless and **confirmed WHPX hardware acceleration
  working** ("Windows Hypervisor Platform accelerator is operational" — Hyper-V
  was already active on this machine, so **no BIOS/UEFI change was needed**);
  reached full boot (`adb devices` → `device`), was recognized by `flutter
  devices`, then shut down cleanly. `flutter doctor -v` → **Android toolchain
  [√]**. E1/E2/E3 reverified unchanged throughout.
- **E5 PASS:** **PostgreSQL 17.11** at `C:\Program Files\PostgreSQL\17`
  (official EDB/PostgreSQL Global Development Group installer, SHA-256 verified),
  data dir `C:\Program Files\PostgreSQL\17\data` (installer default; confirmed not
  a reparse point / same system volume as the rest of the toolchain). Service
  **`postgresql-x64-17`** running, StartType Automatic, port **5432** listening.
  `psql`/`pg_dump`/`pg_restore`/`postgres` all **17.11**, on **User** PATH
  (`C:\Program Files\PostgreSQL\17\bin`, appended). Connectivity proved
  (`SELECT version()`). Auth: `pg_hba.conf` restricts to `127.0.0.1`/`::1` via
  `scram-sha-256`; no firewall rule opens 5432 (`postgresql.conf` has the
  installer's default `listen_addresses='*'`, left as installed per instruction —
  not externally reachable given the above). Dev database **`cams_dev`** (empty,
  no tables/schema) owned by a dedicated **non-superuser** role **`cams_dev`**
  (`rolsuper=false`). `pg_dump`/`pg_restore` functionally smoke-tested against it.
  **No password ever recorded, displayed, or committed** — the postgres superuser
  password was set by the user directly in the installer GUI; the `cams_dev` role
  credential lives only in the standard, non-repo
  `%APPDATA%\postgresql\pgpass.conf`. Installing the service required one UAC
  approval, which the user granted by running the (checksum-verified) installer
  themselves — this was the one E-step, unlike E1–E4, that could not be completed
  fully unattended from a non-interactive session.
- **E6 PASS:** **Git repository initialised** at `E:\EPICS 26\CAMS` (was not a
  repo before), Git **2.51.0**, default branch **`main`**. `.gitignore` reviewed
  and extended before the first commit: added PostgreSQL credential patterns
  (`pgpass.conf`, `.pgpass`, `*.pgpass`), Android signing (`key.properties`),
  broader secret extensions (`*.key`, `*.crt`), `logs/`/`*.temp`, and local
  Claude Code scratch-state patterns — on top of the pre-existing Java/Maven/
  Gradle/Flutter/Dart/Android/iOS/OS/IDE coverage. Git identity resolves from
  the existing **global** config (`Kaustubh <guptakaustubh03@gmail.com>`) —
  already correct, not changed. First commit **`6b689ff`** — "chore: baseline
  CAMS project documentation" — 38 files (all existing docs/control files +
  `.gitignore`; no application/database code). Staged list and diff were
  inspected before committing: no passwords/tokens/keys/pgpass/binaries staged.
  Working tree clean; **no remote configured** — GitHub setup intentionally not
  done (not requested), as of this step (2026-09-05).
  **Git note (current state, 2026-09-12):** this has since changed — a GitHub
  `origin` remote is now configured and `main` is synchronized with
  `origin/main` at `d53b9fc`. An earlier history rewrite (which removed the
  Claude co-author attribution from early commits) replaced the local history
  described above; the pre-rewrite history, including this `6b689ff` commit,
  is preserved on the local branch `backup-before-remove-claude`, kept
  intentionally as a safety branch.
- **E7 PASS — Final environment readiness audit (2026-09-05).** Re-verified
  E1–E6 with live evidence, not just `--version` strings: Maven confirmed
  actually running on JDK 21; `flutter doctor -v` → Android toolchain **[√]**;
  the `Medium_Phone_API_36` emulator was booted, confirmed WHPX-accelerated,
  detected by `adb devices` (`emulator-5554 device`), then shut down cleanly;
  PostgreSQL connectivity + `cams_dev` (empty, non-superuser owner) reverified
  via the existing `pgpass.conf` — **no credentials displayed or logged**; Git
  baseline commit `6b689ff` intact on `main`, working tree has only the
  expected E6/E7 doc updates uncommitted, no secrets/binaries/installers
  tracked or present anywhere in the project. **Readiness: READY WITH
  NON-BLOCKING WARNINGS** — (1) `flutter doctor`'s Visual Studio gap is
  correctly irrelevant (Android-only architecture); (2) `postgresql.conf` still
  has the installer default `listen_addresses='*'` (mitigated by `pg_hba.conf`
  + no firewall rule); (3) a harmless untracked `.git/sg-hook-once-*` session-
  tooling artifact was observed. Nothing installed or changed during the audit.
- Also present: Git 2.51, Node 24 / npm 11, Python 3.14, winget. Two AV products
  registered (Windows Defender + McAfee) — noted as the likely (not fully provable
  without admin tools) cause of the E3 `AppData` cross-volume behavior.
- **Still missing (non-blocking):** (optional) Docker. *(A GitHub remote is no
  longer outstanding — see the Git note above.)*

## Phase 1 skeleton (implementation, 2026-09-05) — PASS WITH WARNINGS
- **Backend (`backend/`):** Spring Boot **4.1.1** on **Java 21.0.12.1**
  (Temurin), Maven. **Real current version discovered live** (Spring
  Initializr metadata + Maven Central), not assumed from training data. **Boot
  4.x quirk:** Maven Central coordinate has **no `.RELEASE` suffix**
  (`4.1.1`, not `4.1.1.RELEASE` as Initializr's own metadata `id` field
  suggests) — verified against `spring-boot-starter-parent`'s real
  `maven-metadata.xml` before fixing `pom.xml`. Dependencies are exactly the
  approved ADR-0001 set: `spring-boot-starter-{webmvc,data-jpa,security,
  validation,actuator}`, `flyway-database-postgresql`, `postgresql` driver —
  **deliberately no Spring Modulith** (thematically fits modular monolith but
  isn't in the approved list — good dependency-discipline example for future
  reference), no Docker/Testcontainers/message broker. One package + a
  documenting `package-info.java` per approved module (`auth`, `localbody`,
  `assetcategory`, `asset`, `complaint`, `maintenance`, `feedback`,
  `notification`, `audit`, `reporting`, `media`, plus `common`/`common.config`)
  — **zero business logic**. Deny-by-default `SecurityFilterChain`
  (`common/config/SecurityConfig.java`) permits only `GET /health`; CSRF/
  httpBasic/formLogin disabled (stateless JSON API). `application.yml`: **no
  credential in any file** — datasource username/password come from
  `SPRING_DATASOURCE_USERNAME`/`_PASSWORD` env vars (empty default);
  `spring.flyway.enabled: false`; `hibernate.ddl-auto: none`;
  `management.health.db.enabled: false`; `/health` exposed at the bare
  approved path (API_ARCHITECTURE.md §4.12) via Actuator. Verified: `mvn
  compile`/`mvn test` → BUILD SUCCESS (1/1); packaged jar started standalone,
  `GET /health` → `200 {"status":"UP"}`, confirmed running on Java
  **21.0.12.1**, stopped cleanly (Windows needed `taskkill /F` — plain
  `taskkill` fails on this kind of detached background java.exe; harmless,
  not a app-level issue). One expected, non-actionable log line each run:
  Spring Security auto-generates a random in-memory default-user password
  because no `UserDetailsService` bean exists yet (that's the future
  Auth/User module's job, ADR-0006/ADR-0012) — irrelevant to `/health`
  (`permitAll`), not a stored secret.
- **Runtime DB-connectivity discovery (investigated, confirmed benign — not a
  security incident):** both `mvn test` and the standalone jar genuinely
  connect Hibernate/HikariCP to the real `cams_dev` DB (PostgreSQL 17.11) at
  startup, even though **no `SPRING_DATASOURCE_*`/`PG*` env var is set
  anywhere** on this machine and `application.yml`'s password default is
  empty. Root cause, confirmed by three diagnostic `psql` tests: **pgjdbc (like
  libpq) treats an empty-string password as "not provided" and falls back to
  the OS-default `pgpass.conf`** (`%APPDATA%\postgresql\pgpass.conf`, created
  in E5) — (1) a deliberately wrong password was correctly **rejected**
  (proves `scram-sha-256` is genuinely enforced, no `trust` fallback); (2) the
  same empty password with `PGPASSFILE` pointed at a nonexistent file
  **hung waiting for an interactive prompt** instead of succeeding (proves the
  empty password itself has no special power — the earlier success came from
  the pgpass **file**, not the empty value). This is exactly the "other secure
  local-development mechanism" the environment setup allowed as an alternative
  to env vars — **no credential was read, displayed, logged, hardcoded, or
  committed by this session** at any point. No config change was made as a
  result (kept `management.health.db.enabled: false` since not every future
  developer machine will have this pgpass entry).
- **Frontend (`mobile/`):** `flutter create --platforms android` (org
  `com.cams`, package `com.cams.mobile`). Added **`flutter_riverpod ^3.4.3`**
  (real latest verified via the pub.dev API — an initial guess of `^3.0.2` was
  caught and corrected before use); **removed `cupertino_icons`** (unused,
  Android-only skeleton — dependency-discipline check). `lib/main.dart`:
  minimal `CamsApp` (Material 3, `ColorScheme.fromSeed`) wrapped in a Riverpod
  `ProviderScope`, one `HomeShell` screen showing only "CAMS" / "Community
  Asset Management System" — **no navigation, role routing, or CAMS feature
  screens**. `test/widget_test.dart` rewritten to match (the generated
  counter-app test was removed). Verified: `flutter pub get`; `flutter
  analyze` → 0 issues (after removing one unused import the generator/edit
  left behind); `flutter test` → 1/1 pass; built + installed the debug APK on
  the **`Medium_Phone_API_36`** emulator, confirmed via `adb logcat` (Flutter
  engine loaded, Impeller/OpenGLES backend, Dart VM service listening, no
  crash) and a screenshot that the shell actually rendered; then uninstalled
  the test app and shut the emulator down cleanly (confirmed no lingering
  `qemu`/`emulator` process).
- **Environment obstacle hit and fixed (non-destructive, worth remembering for
  any future Flutter/Android build on this machine):** `flutter build apk`
  invokes the **Gradle wrapper**, which tried to download
  `gradle-9.3.1-all.zip` from `services.gradle.org` (→ redirects through
  GitHub release assets). The Gradle wrapper's own Java `HttpURLConnection`
  downloader **consistently timed out** connecting (2 attempts, not a one-off
  blip), while `curl` in the same shell reached the **exact same final URL**
  successfully — a JVM-socket-specific network restriction in this sandboxed
  environment, not a config/dependency problem. **Fix:** downloaded the
  official zip via `curl`, verified its **SHA-256 against Gradle's own
  published checksum** (`17f277867f6914d61b1aa02efab1ba7bb439ad652ca485cd8ca6
  842fccec6e43`), and placed it directly into the wrapper's own expected cache
  path (`~/.gradle/wrapper/dists/gradle-9.3.1-all/<hash>/`, confirmed by the
  `.lck`/`.part` placeholder files the failed attempts had already created
  there) so the wrapper found it locally instead of re-downloading. One-time
  cost (~27 min total, dominated by a first-time Android **NDK r28c**
  download+unzip that the Android Gradle Plugin pulled in on its own and which
  *did* succeed over the network normally) — a **future Gradle/Flutter Android
  build on this machine should not need this workaround again**, since the
  distribution is now cached. Non-blocking build warning seen once: "SDK
  processing... understands SDK XML versions up to 3 but... version 4" — a
  known, harmless cmdline-tools/AGP version-skew notice.
- **Git:** nothing committed this session (not instructed to). `backend/` and
  `mobile/` are new untracked directories; `git add -n` dry-run + a
  secret-pattern scan across every file that would be staged found **no
  credentials, no `pgpass.conf`, no build output/binaries** — the two
  pattern-scan hits were both false positives (Maven Wrapper's own
  `MVNW_PASSWORD` env-var-name boilerplate; this session's own safe
  `${SPRING_DATASOURCE_PASSWORD:}` placeholder). `.gitignore` (root +
  Spring-Initializr-generated `backend/.gitignore` + Flutter-generated
  `mobile/android/.gitignore`) already correctly excludes `target/`,
  `build/`, `.dart_tool/`, the Android Gradle wrapper jar/scripts, and
  `local.properties` — no `.gitignore` change was needed.
- **D26 correction (2026-09-12), independently re-verified.** A dedicated
  `cams-verifier` audit of the Phase 1 skeleton found
  `mobile/android/app/build.gradle.kts` had left `minSdk =
  flutter.minSdkVersion`, which resolves to Flutter's own default (**24**) —
  contradicting the approved **D26** requirement (Android 8.0 / API 26+;
  `../docs/requirements/REQUIREMENTS.md` D26, NFR-COMPAT-002). Fixed to an
  explicit `minSdk = 26` (only that one line changed; no other build file,
  dependency, or architecture touched). A second, independent `cams-verifier`
  pass confirmed **26** at every layer: source (`build.gradle.kts:23`); build
  output (`output-metadata.json` → `minSdkVersionForDexing: 26`); merged
  manifest (`processDebugManifestForPackage/AndroidManifest.xml` →
  `android:minSdkVersion="26"`) — and re-ran `flutter analyze` (0 issues),
  `flutter test` (1/1 pass), `flutter build apk --debug` (success), all still
  passing. **The 24-vs-26 contradiction is resolved.** No new ADR — this is
  an implementation correction, not an architecture decision.

## Baseline schema (implementation, 2026-09-12) — first backend feature, PASS
- **File:** `backend/src/main/resources/db/migration/V1__init_schema.sql` —
  one Flyway migration, translating `docs/architecture/DATA_MODEL.md` §4
  (entity catalog) and §8 (enumerations) directly into PostgreSQL DDL. A
  single baseline file was used (no concrete reason found in the approved
  docs to split it for MVP scope).
- **Schema objects:** all 17 MVP tables — `local_body`,
  `village_municipality`, `ward`, `user_account`, `refresh_token`,
  `asset_category`, `asset`, `asset_photo`, `complaint`, `complaint_photo`,
  `complaint_status_history`, `worker_assignment`, `maintenance_history`,
  `feedback`, `notification`, `audit_entry`, `media_object` — plus **14**
  native PostgreSQL enum types (`role`, `account_status`, `language`,
  `local_body_type`, `asset_status`, `asset_condition`, `complaint_status`,
  `complaint_priority`, `complaint_event`, `complaint_photo_kind`,
  `notification_type`, `audit_action`, `entity_type`, `media_purpose`), each
  matching its DATA_MODEL §8 value set **verbatim** (verified by direct
  `pg_enum` query — see below). Notably `complaint_status` is **exactly** the
  D7 7-value set (`PENDING, ASSIGNED, IN_PROGRESS, RESOLVED, VERIFIED, CLOSED,
  REJECTED`) — **no `RETURNED` value**, consistent with AD-05/ADR-0005;
  rework is `complaint_status_history.event_type = 'RETURN'` +
  `complaint.returned_count`. `asset_status`/`asset_condition` are two
  independent enum columns on `asset` (D8/D24, never conflated).
- **Org hierarchy (ADR-0004):** `local_body → village_municipality → ward`
  using the working names from DATA_MODEL.md (OQ-39 terminology untouched — a
  later rename is a follow-up migration, not this one). `asset` and
  `complaint` both carry a **denormalised `local_body_id`** per DATA_MODEL
  §6; `complaint` also denormalises `ward_id` from its asset.
- **Key constraints:** `(local_body_id, name)` uniqueness on
  `village_municipality` and `asset_category`; globally-unique `asset.
  public_code` and `user_account.login_identifier`; **one active
  `worker_assignment` per complaint** via a **partial unique index**
  (`UNIQUE (complaint_id) WHERE active = true`) rather than a plain unique
  constraint (a complaint legitimately has many *historical* — inactive —
  assignments, only one *active* one at a time); **one `feedback` per
  complaint** and **one `maintenance_history` per complaint** via plain
  unique constraints on `complaint_id`. Two **circular FK pairs** — `local_
  body.created_by → user_account.id` and `complaint.current_assignment_id →
  worker_assignment.id` — resolved by creating the column first and adding
  the FK via a deferred `ALTER TABLE ... ADD CONSTRAINT` once the referenced
  table exists (standard pattern for this kind of mutual reference, not a
  workaround for a design flaw).
- **Indexes:** every index called out explicitly in DATA_MODEL §11 — `asset
  (local_body_id, ward_id, asset_category_id, status)`, `asset(latitude,
  longitude)`, `complaint(local_body_id, status, ward_id, priority)`,
  `complaint(reported_by)`, `complaint(current_assignment_id)`,
  `worker_assignment(worker_id, active)`, `complaint_status_history
  (complaint_id, occurred_at)`, `notification(recipient_id, read_at)`,
  `audit_entry(occurred_at)`, `audit_entry(entity_type, entity_id)`,
  `audit_entry(actor_id)` — plus a modest set of additional single-column FK
  indexes not otherwise covered (e.g. `ward.village_municipality_id`,
  `refresh_token.user_account_id`, `asset_photo.asset_id`,
  `complaint.asset_id`, `complaint_photo.complaint_id`, `worker_assignment.
  complaint_id`, `maintenance_history.asset_id`), per ADR-0019's general
  "index foreign keys and filter columns" baseline practice.
- **Deliberately excluded** (confirmed absent by direct catalog query):
  `citizen_recovery` (ADR-0007 / OQ-40 — the approved MVP default,
  staff-assisted-only reset, needs no extra table; the entity is explicitly
  documented as "not created" under that default) and every Inventory/
  Budget/SLA Secondary-module table (`inventory_item`, `stock_movement`,
  `expenditure_entry`, `budget_allocation`, `sla_target` — DATA_MODEL §9).
- **Config changes:** `backend/src/main/resources/application.yml` —
  `spring.flyway.enabled` flipped `false → true`; `spring.jpa.hibernate.
  ddl-auto` **stays `none`** (Flyway is the only schema-authority; Hibernate
  never creates/alters schema — verified: zero Hibernate-created sequences
  and exactly 18 tables in `public`, i.e. the 17 domain tables +
  `flyway_schema_history`, nothing extra). `backend/src/main/resources/
  db/migration/README.md` rewritten to describe the new baseline (no longer
  says "no migrations exist yet").
- **Implementation-style choices made where the docs were silent** (flagged
  for review, not silent deviations from an approved decision):
  - **UUID primary keys**, server-generated via `gen_random_uuid()` (built
    into PostgreSQL core since v13 — confirmed no `pgcrypto` extension is
    needed on this PostgreSQL 17.11 instance). Matches API_ARCHITECTURE §2's
    "IDs: Opaque strings (UUID proposed)".
  - **Native PostgreSQL `ENUM` types** (not `varchar` + `CHECK`) for every
    DATA_MODEL §8 enumeration — makes the approved value sets directly
    inspectable via `\dT`/`pg_enum`, matching how this task's own
    verification step was framed.
  - **`NUMERIC(12,2)`** for `maintenance_history.repair_cost` (single-
    currency INR assumed per DATA_MODEL §13's flagged, not-yet-confirmed,
    currency note).
  - `ward.ward_number` stored as `VARCHAR(20)` (not integer), since
    DATA_MODEL only says "number?" without a type, and ward numbers may be
    alphanumeric (e.g. "7A").
  - `maintenance_history.completion_photo_ids` (DATA_MODEL: "references to
    COMPLAINT_PHOTO of kind WORKER_COMPLETION") modelled as a `UUID[]` array
    column rather than a join table, since the conceptual model describes it
    as a small reference list attribute on the row, not a first-class
    relation.
  - `notification.body_params` (DATA_MODEL: "small structured payload for
    localisation") modelled as `JSONB`.
  - `feedback.rating_value` has only a `CHECK (rating_value >= 1)` — **no
    upper bound** — because `feedback.rating.max` is application config and
    its value is **OQ-37, still OPEN** (default only *proposed* as 5); baking
    an unapproved number into a DB constraint was avoided per CLAUDE.md rule
    7 ("do not invent requirements").
  - `user_account.local_body_id` nullability has **no CHECK constraint**
    coupling it to `role` (e.g. forcing NULL only for ADMIN), because
    DATA_MODEL §6 itself flags that ADMIN-is-always-global rule as
    "PROPOSED" / a future per-body-admin refinement is possible — encoding
    the current default as a hard DB constraint would foreclose that
    documented flexibility.
  - Row-count invariants that need cross-row logic and can't be expressed as
    a simple constraint (≤5 `asset_photo` rows per asset; ≥1
    `WORKER_COMPLETION` `complaint_photo` before a complaint can `RESOLVE`)
    are **not** enforced by DB triggers — left to the application/service
    layer, consistent with SECURITY_ARCHITECTURE §7–§8 describing these as
    Media/Complaint-service-enforced rules, not schema rules.
  - `updated_at` columns (`asset.updated_at`, `complaint.updated_at`) are
    **not** auto-maintained by a DB trigger; the application sets them, as
    with the rest of the write path.
- **Verification evidence (against the real local `cams_dev` PostgreSQL 17.11
  database, via the existing `pgpass.conf` mechanism — no credentials
  handled, displayed, or logged by this session):**
  - `mvn compile` → BUILD SUCCESS (no errors).
  - `mvn test` → BUILD SUCCESS, 1/1 tests pass; Spring Boot context startup
    log shows Flyway creating `flyway_schema_history`, then "Migrating schema
    "public" to version "1 - init schema"" → "Successfully applied 1
    migration". A **second** `mvn test` run shows Flyway "Successfully
    validated 1 migration" → "Schema \"public\" is up to date. No migration
    necessary." (repeatable/idempotent, no re-apply).
  - `SELECT version, description, success FROM flyway_schema_history;` →
    `1 | init schema | t`.
  - `psql \dt` → all 17 domain tables + `flyway_schema_history` (18 total).
  - `psql \dT` + a `pg_type`/`pg_enum` query → all 14 enum types present with
    their exact approved value lists (spot-checked every one, not just
    `complaint_status`).
  - An `information_schema` FK query → all 37 expected foreign keys present,
    including both deferred/circular ones.
  - A `pg_constraint` query (unique + check constraints) and `\di` (all
    indexes) → every constraint and index described above present, including
    the partial unique index on `worker_assignment`.
  - `SELECT table_name FROM information_schema.tables WHERE table_name IN
    ('citizen_recovery','inventory_item','stock_movement',
    'expenditure_entry','budget_allocation','sla_target')` → **0 rows**
    (confirmed absent).
  - `SELECT sequence_name FROM information_schema.sequences` → 0 rows (no
    Hibernate-created sequences; UUID PKs use `gen_random_uuid()`, not
    sequences); table count in `public` = 18 exactly (no Hibernate schema
    drift given `ddl-auto: none`).
  - Packaged jar started standalone (`java -jar target/backend-0.0.1-
    SNAPSHOT.jar`); log shows Flyway "Successfully validated 1 migration" /
    "up to date"; `GET /health` → `HTTP 200
    {"groups":["liveness","readiness"],"status":"UP"}`; process stopped
    cleanly afterward (Windows needed `taskkill /F` for the detached
    `java.exe`, as in the Phase 1 skeleton entry — not an app-level issue).
- **No conflicts found** between DATA_MODEL.md and any other approved
  document (SYSTEM_ARCHITECTURE, the ADRs, SECURITY_ARCHITECTURE) that would
  have required stopping and reporting instead of implementing — the
  documents were consistent for everything this migration covers.
- **Scope note:** this task is schema-only. No JPA entities, repositories,
  services, controllers, or Auth/RBAC logic were added — `backend/src/main/
  java` module packages are still each just a `package-info.java` (Phase 1
  skeleton state unchanged on the Java side). `docs/requirements/` and
  `docs/decisions/` were **not** touched (out of scope for an implementation
  task per the task instructions).

## Where to look
- `../CLAUDE.md` — how to operate on this project.
- `../PLAN.md` — plan; proposed vs approved decisions.
- `../TASKS.md` — task list and status.
- `../docs/requirements/REQUIREMENTS.md` — **Requirements v1.0 (BASELINE)**; decision
  register D1–D37 in §1; open items in §15.3.
- `../docs/architecture/` — **architecture v1.0 (TEAM-APPROVED)**: SYSTEM (+ §1A AD
  register), DATA_MODEL, COMPLAINT_STATE_MACHINE, API, SECURITY.
- `../docs/decisions/` — **ADRs 0001–0019 (ACCEPTED)**; index + OPEN-map in its README.
- `../docs/database/` — schema & migrations narrative *(not yet filled in as a
  doc; the physical schema itself now lives in
  `../backend/src/main/resources/db/migration/V1__init_schema.sql` — see
  "Baseline schema" entry above)*.
- `../docs/api/` — API reference *(to be filled; contract in architecture/API_ARCHITECTURE.md)*.
- `../docs/testing/` — test strategy *(to be filled)*.

## Open / pending items (do not assume values; all still OPEN)
- **OQ-38** — architectural *approach* approved (AD-05); OPEN: return-reason
  taxonomy / UI "returned" indicator.
- **OQ-39** — two-level org *structure* approved (AD-04); OPEN: final
  LocalBody/Village/Municipality **terminology**.
- **OQ-40** — MVP *default* approved (AD-08: staff-assisted reset); OPEN:
  recovery-code option, or change D16/D21 for SMS/email. No secure self-service claimed.
- **OQ-14** — system scale/capacity numbers — none approved.
- **OQ-17** — MVP availability target (%) — none approved; do not invent (single-host SPOF documented).
- **OQ-25** — detailed complaint-reopen policy — Future/Secondary; no policy approved.
- **OQ-36** — university DSN3099 milestones/rubric/deadlines — **PENDING faculty**; no dates invented.
- **OQ-37** — citizen rating scale (`feedback.rating.max` config; default proposed 5) + one-time feedback text.
- **OQ-41** — SLA target values per priority — deferred (Secondary only).
- **OQ-42** — citizen recovery request persistence — ADR-0007's staff-assisted
  flow (`POST /auth/citizen/recovery/request` → staff `.../resolve`) implies a
  pending-request record; DATA_MODEL.md defines no such entity. Unresolved: (A)
  no persisted record (manual office process) vs (B) persist requests (needs
  DATA_MODEL.md to define the entity first). Neither chosen; ADR-0007 not
  reinterpreted. Resolve before the Auth/User module is built. Found
  2026-09-12 during the independent `cams-architecture-reviewer` pass on the
  baseline schema.
- **Map tile provider / licensing** — AD-10 open sub-item; choose before map screens.
- Non-requirements gaps: environment/prerequisite readiness (active phase),
  specific deployment host, Git remote, team/process — `../PLAN.md` §4–§6.
- Minor flags from data model: single-currency (INR) assumed for costs; staff
  login identifier (mobile vs username) not fixed by requirements.
