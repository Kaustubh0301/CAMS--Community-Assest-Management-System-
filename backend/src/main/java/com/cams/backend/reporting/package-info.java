/**
 * Reporting/Analytics module (MVP).
 *
 * Basic counts (asset status/condition, complaint counts by status/ward/
 * category/priority); generates the Complaint report and Asset register
 * report as PDF/Excel (D33, ADR-0011, ADR-0015). Read-only across other
 * modules — the one deliberate exception to the module-boundary rule.
 *
 * See docs/architecture/SYSTEM_ARCHITECTURE.md §5 and §13.
 * No business logic is implemented yet — Phase 1 skeleton only.
 */
package com.cams.backend.reporting;
