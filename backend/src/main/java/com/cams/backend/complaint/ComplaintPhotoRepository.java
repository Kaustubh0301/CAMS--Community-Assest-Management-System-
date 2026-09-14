package com.cams.backend.complaint;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface ComplaintPhotoRepository extends JpaRepository<ComplaintPhoto, UUID> {
}
