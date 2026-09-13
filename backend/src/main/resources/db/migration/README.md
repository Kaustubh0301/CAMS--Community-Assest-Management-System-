# Flyway migrations

This is Flyway's default migration location (`classpath:db/migration`).

**`V1__init_schema.sql`** — the baseline physical schema, translated directly
from `docs/architecture/DATA_MODEL.md` §4 (entity catalog) and §8
(enumerations). It creates every MVP table (organisation hierarchy, users/auth,
assets, complaints/maintenance, feedback, notifications, audit, media) and the
native PostgreSQL enum types for the approved value sets (roles, account
status, asset status/condition, complaint status/priority/event, etc.).
Secondary-module tables (Inventory, Budget/Expenditure, SLA) and
`citizen_recovery` is deliberately not created. OQ-40 remains open regarding
the concrete citizen password-recovery mechanism, and OQ-42 remains open
regarding persistence of staff-assisted recovery requests. No recovery schema
is invented in this baseline migration.

`spring.flyway.enabled` is `true` in `application.yml` now that this migration
exists. `spring.jpa.hibernate.ddl-auto` stays `none` — Flyway is the only
source of schema truth; Hibernate never creates or alters schema.

Future schema changes are additional `V<n>__description.sql` files here, never
edits to an already-applied migration.
