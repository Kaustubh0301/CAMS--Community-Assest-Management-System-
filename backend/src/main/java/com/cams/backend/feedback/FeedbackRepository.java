package com.cams.backend.feedback;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface FeedbackRepository extends JpaRepository<Feedback, UUID> {
}
