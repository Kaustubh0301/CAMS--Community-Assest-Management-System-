/**
 * Complaint module (MVP).
 *
 * Citizen complaint creation (asset-linked, D3), complaint queue (role-scoped,
 * worker isolation per D28), complaint detail, status history, priority
 * (D9), rejection. Hosts the complaint state machine (D7, ADR-0005).
 *
 * See docs/architecture/SYSTEM_ARCHITECTURE.md §5,
 * docs/architecture/COMPLAINT_STATE_MACHINE.md.
 * No business logic is implemented yet — Phase 1 skeleton only.
 */
package com.cams.backend.complaint;
