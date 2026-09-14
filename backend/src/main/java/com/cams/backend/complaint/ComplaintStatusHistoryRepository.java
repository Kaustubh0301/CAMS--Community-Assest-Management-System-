package com.cams.backend.complaint;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.Repository;

/** Append-only: deliberately exposes no update or delete operations. */
interface ComplaintStatusHistoryRepository extends Repository<ComplaintStatusHistory, UUID> {

    ComplaintStatusHistory save(ComplaintStatusHistory entry);

    Optional<ComplaintStatusHistory> findById(UUID id);
}
