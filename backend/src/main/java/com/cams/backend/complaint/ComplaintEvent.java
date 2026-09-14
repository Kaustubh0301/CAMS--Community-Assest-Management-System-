package com.cams.backend.complaint;

/** Maps the PostgreSQL enum type {@code complaint_event} (DATA_MODEL.md §8, ADR-0005). */
public enum ComplaintEvent {
    SUBMIT,
    ASSIGN,
    REASSIGN,
    PRIORITY_SET,
    START,
    RESOLVE,
    RETURN,
    VERIFY,
    CLOSE,
    REJECT
}
