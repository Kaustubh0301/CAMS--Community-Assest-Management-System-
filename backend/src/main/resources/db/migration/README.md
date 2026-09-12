# Flyway migrations

This is Flyway's default migration location (`classpath:db/migration`).

**Empty by design in Phase 1** — no CAMS schema exists yet. The first real
migration (e.g. `V1__init_local_body_hierarchy.sql`) is a later task, once the
physical schema is designed from `docs/architecture/DATA_MODEL.md`.

`spring.flyway.enabled` is `false` in `application.yml` until the first
migration lands (see that file for why).
