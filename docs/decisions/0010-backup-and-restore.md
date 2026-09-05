# ADR-0010 — Backup and restore approach

**Status:** **ACCEPTED** — team-approved at the CAMS architecture sign-off (2026-09-04) as **AD-18**.
**Date:** 2026-09-04 · **Review note:** approved as written. Nightly PostgreSQL backup + media backup, ≥ 7-day retention, separate backup location, restore verification/drill before final evaluation, and DB↔media traceability via the MANIFEST/checksum approach.
**Relates to:** REQUIREMENTS.md D36, NFR-BAK-001..004; SYSTEM_ARCHITECTURE.md §14;
SECURITY_ARCHITECTURE.md §13; team decision **AD-18**

## Context

D36 requires: **daily PostgreSQL backup**, **≥ 7 days** retention, **media/photos
included**, a **restore test before final evaluation**, and states that a
**documented/manual process is acceptable initially** for the student MVP.
Deployment is a single host with a Spring Boot JAR + PostgreSQL + a media directory
(ADR-0008).

## Decision

**A scheduled script that produces a dated backup set of three artifacts, kept on
a separate location, with a documented restore runbook and a pre-evaluation drill.**

**What is backed up (one set per run):**
1. **Database** — `pg_dump` in **custom/compressed format** (`-Fc`) of the CAMS
   database → `db.dump`.
2. **Media** — a `tar`/zip of the configured media root → `media.tar`.
3. **`MANIFEST`** — timestamp, app version, Flyway schema version, DB size, media
   file count + total size, and checksums of `db.dump` and `media.tar`.

**Schedule & retention:**
- **Nightly**, via OS scheduler (cron / Windows Task Scheduler) or a small
  `@Scheduled` trigger that shells out. A **manual run command** is documented for
  the demo host.
- Naming: `cams-backup-YYYYMMDD-HHMM/{db.dump, media.tar, MANIFEST}`.
- Keep **at least the last 7 daily sets** (config `backup.retentionDays`,
  default 14); prune older.

**Location:**
- Written to a **separate disk/volume or institute/external storage**, never the
  live data directory or the repo.
- If a copy leaves the host, it is **encrypted at rest** (SECURITY_ARCHITECTURE
  §13); credentials the script needs come from the environment.

**Restore (runbook, conceptual):**
1. Stop the backend.
2. Create a fresh database; `pg_restore` `db.dump` into it.
3. Extract `media.tar` into the media root.
4. Point the backend config at the restored DB + media root; start it.
5. Run the **smoke checklist**: log in as each role; open an asset (with a photo);
   open a complaint and its history; generate one report; check `GET /health`.
6. Compare against the `MANIFEST` (row/file counts).

**Drill:** perform the full restore on a scratch DB/host, record the result and
duration in `docs/database/`, **before final evaluation** (a task in `TASKS.md`).

## Alternatives considered

| Option | Why not (for MVP) |
|--------|-------------------|
| **Continuous archiving / PITR (WAL archiving)** | Best RPO, but real operational complexity (base backups + WAL shipping + recovery targets) that a student team is unlikely to run correctly; D36 only asks for daily + tested restore. |
| **Managed/cloud automated backups** | Assumes a managed database and network storage — not the single-host demo model; adds cost/accounts. |
| **`pg_dump` plain SQL (`-Fp`)** | Human-readable but larger and slower to restore; `-Fc` allows parallel restore and selective objects. |
| **DB backup only (skip media)** | Violates D36 / NFR-BAK-002. |
| **Filesystem snapshot of the whole host** | Depends on host/FS features not guaranteed; opaque; still needs a documented restore. |
| **App-level export (JSON dump via API)** | Incomplete, slow, and not transactionally consistent; not a real backup. |

## Consequences

**Positive**
- Meets every D36 clause with tools that ship with PostgreSQL and the OS.
- Two clear artifacts + a manifest make restore verifiable.
- Simple enough for a student to run and explain; automatable later (same script
  under cron) without redesign.

**Negative / trade-offs**
- **RPO = up to 24h** (last nightly). Acceptable per D36; can be tightened later if
  OQ-17 sets a target.
- **DB/media consistency:** they are separate artifacts taken sequentially; a
  photo could be added between the two steps. Mitigations: run media archive
  immediately after `pg_dump`; the `MANIFEST` counts flag gross mismatches; the
  post-restore smoke check catches a broken image. A brief maintenance pause during
  backup eliminates the window if needed.
- Restore is **manual** initially (minutes–hours). Acceptable for the student MVP
  (D36); scripting it is a later improvement.
- Backups contain full PII → must be access-controlled/encrypted
  (SECURITY_ARCHITECTURE §13).

**Follow-up**
- Add backup/restore scripts + the runbook to the repo (Area F).
- Schedule and run the restore drill; record it in `docs/database/`.
