package com.cams.backend.notification;

/** Maps the PostgreSQL enum type {@code notification_type} (D10, FR-NOTIF). */
public enum NotificationType {
    COMPLAINT_SUBMITTED,
    COMPLAINT_ASSIGNED,
    COMPLAINT_STARTED,
    COMPLAINT_RESOLVED,
    COMPLAINT_RETURNED,
    COMPLAINT_VERIFIED,
    COMPLAINT_CLOSED,
    COMPLAINT_REJECTED
}
