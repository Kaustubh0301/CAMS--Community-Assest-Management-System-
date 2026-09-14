package com.cams.backend.common.domain;

/**
 * Maps the PostgreSQL enum type {@code role} (DATA_MODEL.md §8, D1).
 * Shared here because user_account, complaint_status_history and audit_entry
 * all use it.
 */
public enum Role {
    ADMIN,
    OFFICER,
    WORKER,
    CITIZEN
}
