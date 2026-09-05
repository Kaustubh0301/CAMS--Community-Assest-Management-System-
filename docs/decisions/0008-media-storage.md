# ADR-0008 — Media (photo) storage

**Status:** **ACCEPTED** — team-approved at the CAMS architecture sign-off (2026-09-04) as **AD-09**.
**Date:** 2026-09-04 · **Review note:** approved as written. Server-side filesystem/object-like storage with DB metadata for asset, complaint, and maintenance/repair photos; large image binaries are **not** stored in normal domain tables. Upload controls per SECURITY_ARCHITECTURE §7 (type/size/dimension validation, server-side re-encode, EXIF stripping). No mandatory antivirus infrastructure for MVP (known limitation, AD-22).
**Relates to:** REQUIREMENTS.md D11, D25, D36, NFR-PERF-003, NFR-BAK-002;
SYSTEM_ARCHITECTURE.md §8.2, §14; SECURITY_ARCHITECTURE.md §7; team decision **AD-09**

## Context

CAMS stores asset photos (1–5 each), optional citizen complaint photos, and
mandatory worker completion photos. Requirements: image compression (NFR-PERF-003,
D25), and **media included in backups** (D36, NFR-BAK-002). Deployment is a single
host (SYSTEM_ARCHITECTURE §10); scale is unknown (OQ-14).

## Decision

- **Store image bytes on the server filesystem**, under a configured **media root**
  outside any web/static path; keep **metadata in PostgreSQL** (`media_object` +
  the `asset_photo` / `complaint_photo` rows).
- Path layout: `<media-root>/<entity>/<yyyy>/<mm>/<uuid>.<ext>`;
  filename = server-generated UUID (never the client filename).
- **Compression:** the Flutter app downscales/compresses before upload (target
  long-edge + quality from config); the **server re-encodes** to a normalized JPEG,
  which also strips EXIF/GPS and neutralises polyglot files
  (SECURITY_ARCHITECTURE §7).
- **Validation:** magic-byte type check (`image/jpeg`, `image/png`), per-file size
  cap, max-dimension guard, per-request count enforcement (≤5 asset photos;
  ≥1 completion photo to `resolve`).
- **Retrieval:** `GET /media/{storageKey}` — authenticated, re-applies the owning
  entity's visibility rules, `Cache-Control: private`.
- **Abstraction:** access the store only through a `StorageService` interface
  (`put`, `get`, `delete`, `exists`) so an object-store implementation can be
  swapped in later without touching modules.
- **Backup:** the nightly job archives the media root alongside the DB dump
  (ADR-0010).

## Alternatives considered

| Option | Why not (for MVP) |
|--------|-------------------|
| **Store bytes in PostgreSQL (`bytea` / large objects)** | Bloats the DB and every `pg_dump`; slows backups/restores; couples image serving to DB connections; no CDN/streaming benefits. The one upside (single backup artifact) is cheaply replicated by archiving the media dir next to the dump. |
| **External object storage (S3 / MinIO) now** | Cleanest at scale and for durability, but adds a service/credentials/bucket-policy to run, secure, and back up — extra moving parts for a single-host student MVP with unknown load. Kept as the **first** future step via `StorageService`. |
| **Serve media from a static web directory** | Bypasses authorization → citizen/worker isolation (D28) would leak via direct URLs. Rejected. |
| **No server-side re-encode (trust client compression)** | Loses the security benefit (EXIF strip, polyglot defense) and lets malformed/oversized images through. |

## Consequences

**Positive**
- DB stays small → fast `pg_dump`/restore; DB backup and media backup are separate,
  simple artifacts.
- Straightforward to implement and explain; standard file I/O.
- Authorized retrieval preserves worker/citizen data isolation.
- `StorageService` seam makes object storage a later, non-breaking change.

**Negative / trade-offs**
- **Backup consistency:** DB and media are two artifacts; the backup job must
  capture them close together and the restore must extract both (ADR-0010 covers
  the runbook). A photo referenced in the DB but missing on disk (or vice-versa)
  after a partial restore is possible — mitigated by the backup `MANIFEST`
  (counts/checksums) and the post-restore smoke check.
- Filesystem storage on one host is a single point of failure (availability —
  OQ-17); the backup is the recovery path.
- Storage growth is unbounded in principle; mitigated by compression, the 1–5
  asset-photo cap, per-request/day upload limits, and the growth-watch metrics
  (SYSTEM_ARCHITECTURE §15).

**Follow-up**
- Fix concrete caps (max file size, max dimensions, per-day upload count) in config
  during implementation; document in `application-example.properties`.
