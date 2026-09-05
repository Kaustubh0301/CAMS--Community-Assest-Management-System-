# CAMS — Memory Index

Concise index of persistent project knowledge. Keep entries short; link out, do not
duplicate. **Last updated:** 2026-09-05

## Project state
- Project: **CAMS** (Community Asset Management System), ~1-year university
  engineering project for villages / Gram Panchayats / municipalities.
- Phase: Requirements **BASELINED (v1.0)**; **architecture TEAM-APPROVED
  (2026-09-04)** — decisions **AD-01…AD-25** / ADRs **0001–0019 ACCEPTED**.
  **Implementation NOT STARTED.** Environment / prerequisite readiness: **E1–E5
  all PASS** (JDK 21, Maven, Flutter/Dart, Android SDK, PostgreSQL); only Docker
  (optional) and Git remote/workflow remain in `../TASKS.md` §1 before coding can
  begin.
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
- Also present: Git 2.51, Node 24 / npm 11, Python 3.14, winget. Two AV products
  registered (Windows Defender + McAfee) — noted as the likely (not fully provable
  without admin tools) cause of the E3 `AppData` cross-volume behavior.
- **Still missing:** (optional) Docker; Git remote + workflow.

## Where to look
- `../CLAUDE.md` — how to operate on this project.
- `../PLAN.md` — plan; proposed vs approved decisions.
- `../TASKS.md` — task list and status.
- `../docs/requirements/REQUIREMENTS.md` — **Requirements v1.0 (BASELINE)**; decision
  register D1–D37 in §1; open items in §15.3.
- `../docs/architecture/` — **architecture v1.0 (TEAM-APPROVED)**: SYSTEM (+ §1A AD
  register), DATA_MODEL, COMPLAINT_STATE_MACHINE, API, SECURITY.
- `../docs/decisions/` — **ADRs 0001–0019 (ACCEPTED)**; index + OPEN-map in its README.
- `../docs/database/` — schema & migrations *(to be filled from DATA_MODEL.md,
  after the environment step)*.
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
- **Map tile provider / licensing** — AD-10 open sub-item; choose before map screens.
- Non-requirements gaps: environment/prerequisite readiness (active phase),
  specific deployment host, Git remote, team/process — `../PLAN.md` §4–§6.
- Minor flags from data model: single-currency (INR) assumed for costs; staff
  login identifier (mobile vs username) not fixed by requirements.
