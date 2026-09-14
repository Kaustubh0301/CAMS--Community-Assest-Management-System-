package com.cams.backend.common.domain;

/**
 * Maps the PostgreSQL enum type {@code entity_type} (DATA_MODEL.md §8).
 * Shared here because both audit_entry and notification use it.
 */
public enum EntityType {
    LOCAL_BODY,
    VILLAGE_MUNICIPALITY,
    WARD,
    ASSET_CATEGORY,
    ASSET,
    COMPLAINT,
    WORKER_ASSIGNMENT,
    MAINTENANCE_HISTORY,
    FEEDBACK,
    USER_ACCOUNT
}
