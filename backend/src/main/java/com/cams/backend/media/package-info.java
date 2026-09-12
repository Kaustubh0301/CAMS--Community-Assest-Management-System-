/**
 * Media module (MVP).
 *
 * Validates uploads (type/size/dimensions), strips EXIF, re-compresses
 * server-side, stores to the media volume, streams on authorised read
 * (D11, D25, ADR-0008). The only component touching the filesystem for
 * media; other modules go through this module's service interface.
 *
 * See docs/architecture/SYSTEM_ARCHITECTURE.md §5,
 * docs/architecture/SECURITY_ARCHITECTURE.md §7.
 * No business logic is implemented yet — Phase 1 skeleton only.
 */
package com.cams.backend.media;
