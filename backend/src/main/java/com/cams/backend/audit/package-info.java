/**
 * Audit module (MVP).
 *
 * Append-only recording of important actions with actor/action/time/entity/
 * note (D22, ADR-0014); Admin query. No update/delete path is ever exposed.
 *
 * See docs/architecture/SYSTEM_ARCHITECTURE.md §5 and §7,
 * docs/architecture/SECURITY_ARCHITECTURE.md §9.
 * No business logic is implemented yet — Phase 1 skeleton only.
 */
package com.cams.backend.audit;
