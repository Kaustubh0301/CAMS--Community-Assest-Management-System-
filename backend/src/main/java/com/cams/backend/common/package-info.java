/**
 * Cross-cutting, shared building blocks used by every domain module:
 * configuration, the error model, and infrastructure that is not itself a
 * domain module (see docs/architecture/SYSTEM_ARCHITECTURE.md §7).
 *
 * This package must never hold domain/business logic — only shared
 * plumbing.
 */
package com.cams.backend.common;
