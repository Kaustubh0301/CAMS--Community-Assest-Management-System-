/**
 * Cross-cutting, shared building blocks used by every domain module:
 * configuration, the error model, infrastructure that is not itself a
 * domain module (see docs/architecture/SYSTEM_ARCHITECTURE.md §7), and
 * small, behaviour-free enumerations whose PostgreSQL enum type is used as a
 * column type by more than one module ({@code common.domain} — see
 * DATA_MODEL.md §8).
 *
 * This package must never hold domain/business logic or workflow rules —
 * only shared plumbing and plain value types. A shared enum here has no
 * behaviour and imports nothing from a module package; it is not a
 * substitute for the module that owns the process which assigns or
 * interprets that value (e.g. role assignment stays owned by the Auth/User
 * module — see SYSTEM_ARCHITECTURE.md §5).
 */
package com.cams.backend.common;
