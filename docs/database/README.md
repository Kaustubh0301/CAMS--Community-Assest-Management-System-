# Database

**What goes here:** entity / relationship design, table definitions, migration
history, data dictionary, indexing and spatial (GIS) notes, and the backup /
restore procedure.

**Status:** **PostgreSQL is the approved DBMS** (AD-01 / ADR-0001 — see
`../../PLAN.md` §3). The baseline physical schema exists as a Flyway migration,
[`V1__init_schema.sql`](../../backend/src/main/resources/db/migration/V1__init_schema.sql),
translated directly from [`../architecture/DATA_MODEL.md`](../architecture/DATA_MODEL.md);
it has been applied successfully to the local `cams_dev` development database.
No separate schema/ER narrative has been written in this directory yet — the
migration file and `DATA_MODEL.md` remain the current source of truth for
schema shape.
