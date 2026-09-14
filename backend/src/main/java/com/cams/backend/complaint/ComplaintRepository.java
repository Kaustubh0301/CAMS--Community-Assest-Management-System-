package com.cams.backend.complaint;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface ComplaintRepository extends JpaRepository<Complaint, UUID> {
}
