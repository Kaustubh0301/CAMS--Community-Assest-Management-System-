package com.cams.backend.complaint;

/**
 * Maps the PostgreSQL enum type {@code complaint_status}: exactly the D7 lifecycle
 * values. Rework is a RETURN history event plus returned_count, never a separate
 * status (ADR-0005).
 */
public enum ComplaintStatus {
    PENDING,
    ASSIGNED,
    IN_PROGRESS,
    RESOLVED,
    VERIFIED,
    CLOSED,
    REJECTED
}
