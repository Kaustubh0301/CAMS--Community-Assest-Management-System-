-- CAMS baseline schema
--
-- Source of truth: docs/architecture/DATA_MODEL.md v1.0 (TEAM-APPROVED).
-- Implements exactly the MVP entity catalog (§4) and enumerations (§8) of that
-- document. Secondary-module tables (Inventory, Budget/Expenditure, SLA — §9)
-- and CITIZEN_RECOVERY (ADR-0007 / OQ-40, only needed if the recovery-code
-- option is ever adopted) are deliberately NOT created here.
--
-- Working org-hierarchy terminology (local_body / village_municipality / ward)
-- follows ADR-0004; OQ-39 (final naming) is still open and unaffected by this
-- migration — a rename later is a follow-up migration, not a redesign.
--
-- Conventions (implementation-style choices; see task report for rationale):
--   * Primary keys: UUID, server-generated via gen_random_uuid() (built into
--     PostgreSQL core since v13 — no extension required).
--   * Enumerations: native PostgreSQL ENUM types, matching DATA_MODEL §8 value
--     sets verbatim.
--   * Timestamps: TIMESTAMPTZ (UTC on the wire, per API_ARCHITECTURE §2).
--   * Money (repair_cost): NUMERIC(12,2), single-currency (INR assumed, per
--     DATA_MODEL §13 "Currency" open item — flagged, not a hard decision).
--   * Row-count invariants that need cross-row logic (e.g. <=5 asset photos,
--     >=1 WORKER_COMPLETION photo before RESOLVE, one feedback per complaint
--     already enforced via UNIQUE) are enforced in the application/service
--     layer per SECURITY_ARCHITECTURE §7-§8, not via DB triggers.
--   * updated_at columns are maintained by the application; no DB trigger.

-- =============================================================================
-- 1. Enumerated types (DATA_MODEL §8)
-- =============================================================================

CREATE TYPE role AS ENUM ('ADMIN', 'OFFICER', 'WORKER', 'CITIZEN');

CREATE TYPE account_status AS ENUM ('ACTIVE', 'INACTIVE');

CREATE TYPE language AS ENUM ('EN', 'HI');

CREATE TYPE local_body_type AS ENUM ('PANCHAYAT', 'MUNICIPALITY');

CREATE TYPE asset_status AS ENUM ('WORKING', 'BROKEN', 'UNDER_MAINTENANCE', 'DECOMMISSIONED');

CREATE TYPE asset_condition AS ENUM ('GOOD', 'FAIR', 'POOR', 'CRITICAL');

CREATE TYPE complaint_status AS ENUM
    ('PENDING', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'VERIFIED', 'CLOSED', 'REJECTED');

CREATE TYPE complaint_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL');

CREATE TYPE complaint_event AS ENUM
    ('SUBMIT', 'ASSIGN', 'REASSIGN', 'PRIORITY_SET', 'START', 'RESOLVE', 'RETURN', 'VERIFY', 'CLOSE', 'REJECT');

CREATE TYPE complaint_photo_kind AS ENUM ('CITIZEN_REPORT', 'WORKER_BEFORE', 'WORKER_COMPLETION');

CREATE TYPE notification_type AS ENUM
    ('COMPLAINT_SUBMITTED', 'COMPLAINT_ASSIGNED', 'COMPLAINT_STARTED', 'COMPLAINT_RESOLVED',
     'COMPLAINT_RETURNED', 'COMPLAINT_VERIFIED', 'COMPLAINT_CLOSED', 'COMPLAINT_REJECTED');

-- DATA_MODEL §4.6 gives this list as the concrete audit_action value set (the
-- "e.g." wording there is illustrative prose, not an open-ended free-text
-- field); every value here is also referenced by name in
-- COMPLAINT_STATE_MACHINE.md §3 or ADR-0014. Extending this set later (a new
-- audited action) is an additive `ALTER TYPE ... ADD VALUE` migration.
CREATE TYPE audit_action AS ENUM
    ('ASSET_CREATE', 'ASSET_UPDATE', 'ASSET_DECOMMISSION',
     'COMPLAINT_CREATE', 'COMPLAINT_UPDATE', 'COMPLAINT_STATUS_CHANGE',
     'ASSIGNMENT_CHANGE', 'PRIORITY_CHANGE', 'COMPLAINT_VERIFY', 'COMPLAINT_CLOSE', 'COMPLAINT_REJECT',
     'ACCOUNT_CREATE', 'ACCOUNT_DEACTIVATE', 'ACCOUNT_ROLE_CHANGE', 'PASSWORD_RESET',
     'CATEGORY_CHANGE', 'LOCALBODY_CHANGE', 'WARD_CHANGE');

CREATE TYPE entity_type AS ENUM
    ('LOCAL_BODY', 'VILLAGE_MUNICIPALITY', 'WARD', 'ASSET_CATEGORY', 'ASSET', 'COMPLAINT',
     'WORKER_ASSIGNMENT', 'MAINTENANCE_HISTORY', 'FEEDBACK', 'USER_ACCOUNT');

CREATE TYPE media_purpose AS ENUM ('ASSET_PHOTO', 'COMPLAINT_PHOTO');

-- =============================================================================
-- 2. Organisation (DATA_MODEL §4.1) — LocalBody -> Village/Municipality -> Ward
-- =============================================================================

-- created_by references user_account, which in turn references local_body
-- (local_body_id). The circular dependency is resolved below: the column is
-- created here without a FK, and the FK constraint is added once user_account
-- exists (see section 4).
CREATE TABLE local_body (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(200) NOT NULL,
    code        VARCHAR(50)  NOT NULL,
    type        local_body_type NOT NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID NULL,
    CONSTRAINT uq_local_body_code UNIQUE (code)
);

CREATE TABLE village_municipality (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    local_body_id     UUID NOT NULL REFERENCES local_body (id),
    name              VARCHAR(200) NOT NULL,
    code              VARCHAR(50)  NULL,
    active            BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_village_municipality_local_body_name UNIQUE (local_body_id, name)
);

CREATE TABLE ward (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    village_municipality_id   UUID NOT NULL REFERENCES village_municipality (id),
    name                      VARCHAR(200) NOT NULL,
    ward_number               VARCHAR(20) NULL,
    centroid_lat              NUMERIC(9, 6) NULL,
    centroid_lng              NUMERIC(9, 6) NULL,
    active                    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at                TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_ward_village_municipality ON ward (village_municipality_id);

-- =============================================================================
-- 3. Users & auth (DATA_MODEL §4.2)
-- =============================================================================

-- local_body_id is nullable: NULL for a deployment-global ADMIN, set for
-- OFFICER/WORKER/CITIZEN (DATA_MODEL §4.2). Per DATA_MODEL §6 this ADMIN rule
-- is itself flagged PROPOSED (per-body admin scoping is a Future refinement),
-- so no CHECK constraint is added coupling role to local_body_id nullability
-- — that would bake an explicitly-flexible rule into the schema.
CREATE TABLE user_account (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role                role NOT NULL,
    full_name           VARCHAR(200) NOT NULL,
    login_identifier    VARCHAR(100) NOT NULL,
    mobile_number       VARCHAR(20) NULL,
    password_hash       TEXT NOT NULL,
    status              account_status NOT NULL DEFAULT 'ACTIVE',
    preferred_language  language NOT NULL DEFAULT 'EN',
    local_body_id       UUID NULL REFERENCES local_body (id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by          UUID NULL REFERENCES user_account (id),
    deactivated_at      TIMESTAMPTZ NULL,
    deactivated_by      UUID NULL REFERENCES user_account (id),
    CONSTRAINT uq_user_account_login_identifier UNIQUE (login_identifier)
);

CREATE INDEX idx_user_account_local_body ON user_account (local_body_id);

-- =============================================================================
-- 4. Deferred FK now that user_account exists
-- =============================================================================

ALTER TABLE local_body
    ADD CONSTRAINT fk_local_body_created_by FOREIGN KEY (created_by) REFERENCES user_account (id);

-- =============================================================================
-- 5. Refresh tokens (DATA_MODEL §4.2 — ADR-0006)
-- =============================================================================

CREATE TABLE refresh_token (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_account_id  UUID NOT NULL REFERENCES user_account (id),
    token_hash       TEXT NOT NULL,
    issued_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at       TIMESTAMPTZ NOT NULL,
    revoked_at       TIMESTAMPTZ NULL,
    replaced_by      UUID NULL REFERENCES refresh_token (id),
    user_agent       VARCHAR(255) NULL,
    device_label     VARCHAR(100) NULL,
    CONSTRAINT uq_refresh_token_token_hash UNIQUE (token_hash)
);

CREATE INDEX idx_refresh_token_user_account ON refresh_token (user_account_id);

-- Note: CITIZEN_RECOVERY (DATA_MODEL §4.2) is intentionally NOT created.
-- ADR-0007 / OQ-40: the approved MVP default is staff-assisted-only reset,
-- which needs no extra table ("If the team chooses staff-assisted-only
-- recovery, this entity is not created.").

-- =============================================================================
-- 6. Assets (DATA_MODEL §4.3)
-- =============================================================================

CREATE TABLE asset_category (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    local_body_id  UUID NOT NULL REFERENCES local_body (id),
    name           VARCHAR(200) NOT NULL,
    active         BOOLEAN NOT NULL DEFAULT TRUE,
    is_seed        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_asset_category_local_body_name UNIQUE (local_body_id, name)
);

-- Media metadata (DATA_MODEL §4.7 — ADR-0008). Bytes live on the filesystem;
-- only metadata is in PostgreSQL.
CREATE TABLE media_object (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    storage_key   VARCHAR(500) NOT NULL,
    content_type  VARCHAR(100) NOT NULL,
    size_bytes    BIGINT NOT NULL,
    width         INTEGER NULL,
    height        INTEGER NULL,
    checksum      VARCHAR(128) NOT NULL,
    purpose       media_purpose NOT NULL,
    uploaded_by   UUID NOT NULL REFERENCES user_account (id),
    uploaded_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_media_object_storage_key UNIQUE (storage_key)
);

CREATE TABLE asset (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ward_id                UUID NOT NULL REFERENCES ward (id),
    asset_category_id      UUID NOT NULL REFERENCES asset_category (id),
    local_body_id          UUID NOT NULL REFERENCES local_body (id), -- denormalised from ward (DATA_MODEL §6)
    public_code            VARCHAR(50) NOT NULL,
    status                 asset_status NOT NULL DEFAULT 'WORKING',
    condition              asset_condition NOT NULL DEFAULT 'GOOD',
    latitude               NUMERIC(9, 6) NOT NULL,
    longitude              NUMERIC(9, 6) NOT NULL,
    installation_date      DATE NOT NULL,
    warranty_provider      VARCHAR(200) NULL,
    warranty_reference     VARCHAR(200) NULL,
    warranty_expiry_date   DATE NULL,
    created_by             UUID NOT NULL REFERENCES user_account (id),
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_asset_public_code UNIQUE (public_code)
);

-- DATA_MODEL §11 explicit indexing intent.
CREATE INDEX idx_asset_scope ON asset (local_body_id, ward_id, asset_category_id, status);
CREATE INDEX idx_asset_location ON asset (latitude, longitude);

CREATE TABLE asset_photo (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id          UUID NOT NULL REFERENCES asset (id),
    media_object_id   UUID NOT NULL REFERENCES media_object (id),
    caption           VARCHAR(300) NULL,
    sort_order        INTEGER NOT NULL DEFAULT 0,
    uploaded_by       UUID NOT NULL REFERENCES user_account (id),
    uploaded_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_asset_photo_media_object UNIQUE (media_object_id)
);

CREATE INDEX idx_asset_photo_asset ON asset_photo (asset_id);

-- =============================================================================
-- 7. Complaints & maintenance (DATA_MODEL §4.4)
-- =============================================================================

-- current_assignment_id references worker_assignment, which in turn
-- references complaint. Same circular-FK pattern as section 4: column here,
-- constraint added after worker_assignment exists (section 8).
CREATE TABLE complaint (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id                 UUID NOT NULL REFERENCES asset (id),
    ward_id                  UUID NOT NULL REFERENCES ward (id),          -- denormalised from asset
    local_body_id            UUID NOT NULL REFERENCES local_body (id),    -- denormalised from asset
    reported_by              UUID NOT NULL REFERENCES user_account (id),
    description              TEXT NOT NULL,
    status                   complaint_status NOT NULL DEFAULT 'PENDING',
    priority                 complaint_priority NULL,
    current_assignment_id    UUID NULL,
    returned_count           INTEGER NOT NULL DEFAULT 0,
    rejected_reason          TEXT NULL,
    submitted_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT now(),
    closed_at                TIMESTAMPTZ NULL
);

-- DATA_MODEL §11 explicit indexing intent.
CREATE INDEX idx_complaint_scope ON complaint (local_body_id, status, ward_id, priority);
CREATE INDEX idx_complaint_reported_by ON complaint (reported_by);
CREATE INDEX idx_complaint_current_assignment ON complaint (current_assignment_id);
CREATE INDEX idx_complaint_asset ON complaint (asset_id);

CREATE TABLE complaint_photo (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    complaint_id      UUID NOT NULL REFERENCES complaint (id),
    media_object_id   UUID NOT NULL REFERENCES media_object (id),
    kind              complaint_photo_kind NOT NULL,
    uploaded_by       UUID NOT NULL REFERENCES user_account (id),
    uploaded_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_complaint_photo_media_object UNIQUE (media_object_id)
);

CREATE INDEX idx_complaint_photo_complaint ON complaint_photo (complaint_id);

-- Append-only authoritative timeline (DATA_MODEL §4.4, §7; ADR-0005).
CREATE TABLE complaint_status_history (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    complaint_id  UUID NOT NULL REFERENCES complaint (id),
    from_status   complaint_status NULL,
    to_status     complaint_status NULL,
    event_type    complaint_event NOT NULL,
    actor_id      UUID NOT NULL REFERENCES user_account (id),
    actor_role    role NOT NULL,
    note          TEXT NULL,
    occurred_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- DATA_MODEL §11 explicit indexing intent.
CREATE INDEX idx_complaint_status_history_complaint_occurred
    ON complaint_status_history (complaint_id, occurred_at);

CREATE TABLE worker_assignment (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    complaint_id   UUID NOT NULL REFERENCES complaint (id),
    worker_id      UUID NOT NULL REFERENCES user_account (id),
    assigned_by    UUID NOT NULL REFERENCES user_account (id),
    assigned_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    unassigned_at  TIMESTAMPTZ NULL,
    active         BOOLEAN NOT NULL DEFAULT TRUE,
    reason         TEXT NULL
);

-- DATA_MODEL §11 explicit indexing intent.
CREATE INDEX idx_worker_assignment_worker_active ON worker_assignment (worker_id, active);
CREATE INDEX idx_worker_assignment_complaint ON worker_assignment (complaint_id);

-- "Exactly one active assignment per complaint at a time" (DATA_MODEL §4.4).
CREATE UNIQUE INDEX uq_worker_assignment_one_active_per_complaint
    ON worker_assignment (complaint_id)
    WHERE active = TRUE;

ALTER TABLE complaint
    ADD CONSTRAINT fk_complaint_current_assignment
        FOREIGN KEY (current_assignment_id) REFERENCES worker_assignment (id);

-- Created when a complaint reaches CLOSED (DATA_MODEL §4.4 — FR-HIST-001).
-- completion_photo_ids is stored as an array of complaint_photo ids: the
-- conceptual model describes it as a small reference list on this row, not a
-- first-class relation, so a UUID[] column is the simplest reasonable
-- physical representation (implementation-style choice — see task report).
CREATE TABLE maintenance_history (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id               UUID NOT NULL REFERENCES asset (id),
    complaint_id           UUID NOT NULL REFERENCES complaint (id),
    work_summary           TEXT NOT NULL,
    remarks                TEXT NULL,
    repair_cost            NUMERIC(12, 2) NOT NULL,
    worker_id              UUID NOT NULL REFERENCES user_account (id),
    resulting_status       asset_status NOT NULL,
    resulting_condition    asset_condition NOT NULL,
    completion_photo_ids   UUID[] NOT NULL DEFAULT '{}',
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_maintenance_history_complaint UNIQUE (complaint_id) -- one close -> one entry (MVP)
);

CREATE INDEX idx_maintenance_history_asset ON maintenance_history (asset_id);

-- =============================================================================
-- 8. Feedback (DATA_MODEL §4.5)
-- =============================================================================

-- rating_value upper bound is intentionally NOT enforced by a fixed CHECK:
-- feedback.rating.max is application config and its value is OQ-37 (OPEN,
-- default proposed 5) — the requirement is genuinely undecided, so no
-- specific max is baked into the schema. Only rating_value >= 1 is enforced
-- here; the configured upper bound is validated by the application.
CREATE TABLE feedback (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    complaint_id   UUID NOT NULL REFERENCES complaint (id),
    citizen_id     UUID NOT NULL REFERENCES user_account (id),
    rating_value   INTEGER NOT NULL,
    feedback_text  TEXT NULL,
    submitted_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_feedback_complaint UNIQUE (complaint_id), -- one feedback per complaint
    CONSTRAINT chk_feedback_rating_value_min CHECK (rating_value >= 1)
);

-- =============================================================================
-- 9. Notifications & audit (DATA_MODEL §4.6)
-- =============================================================================

-- body_params is a small structured payload for client-side localisation
-- (DATA_MODEL §4.6); JSONB is PostgreSQL's native fit for this.
CREATE TABLE notification (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id          UUID NOT NULL REFERENCES user_account (id),
    type                  notification_type NOT NULL,
    title_key             VARCHAR(200) NOT NULL,
    body_params           JSONB NULL,
    related_entity_type   entity_type NULL,
    related_entity_id     UUID NULL,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    read_at               TIMESTAMPTZ NULL
);

-- DATA_MODEL §11 explicit indexing intent.
CREATE INDEX idx_notification_recipient_read ON notification (recipient_id, read_at);

-- Append-only (DATA_MODEL §4.6 — D22, ADR-0014). No update/delete path is
-- exposed at the application layer; this migration does not add one either.
CREATE TABLE audit_entry (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id      UUID NULL REFERENCES user_account (id), -- NULL only for system actions
    actor_role    role NULL,
    action        audit_action NOT NULL,
    entity_type   entity_type NOT NULL,
    entity_id     UUID NOT NULL,
    note          TEXT NULL,
    occurred_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    request_id    VARCHAR(100) NULL
);

-- DATA_MODEL §11 explicit indexing intent.
CREATE INDEX idx_audit_entry_occurred_at ON audit_entry (occurred_at);
CREATE INDEX idx_audit_entry_entity ON audit_entry (entity_type, entity_id);
CREATE INDEX idx_audit_entry_actor ON audit_entry (actor_id);
