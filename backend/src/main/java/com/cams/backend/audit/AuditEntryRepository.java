package com.cams.backend.audit;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.Repository;

/** Append-only (ADR-0014): deliberately exposes no update or delete operations. */
interface AuditEntryRepository extends Repository<AuditEntry, UUID> {

    AuditEntry save(AuditEntry entry);

    Optional<AuditEntry> findById(UUID id);
}
