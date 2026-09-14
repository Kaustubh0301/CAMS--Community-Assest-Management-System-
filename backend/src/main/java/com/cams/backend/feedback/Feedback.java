package com.cams.backend.feedback;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

/**
 * One citizen rating per complaint (FR-FEED). V1 only enforces rating_value >= 1;
 * the upper bound is config (feedback.rating.max) because the scale is still
 * OQ-37.
 */
@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    // complaint and user_account belong to other modules: id references only (ADR-0002).
    @Column(name = "complaint_id", nullable = false)
    private UUID complaintId;

    @Column(name = "citizen_id", nullable = false, updatable = false)
    private UUID citizenId;

    @Column(name = "rating_value", nullable = false)
    private Integer ratingValue;

    @Column(name = "feedback_text")
    private String feedbackText;

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    public UUID getId() {
        return id;
    }

    public UUID getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(UUID complaintId) {
        this.complaintId = complaintId;
    }

    public UUID getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(UUID citizenId) {
        this.citizenId = citizenId;
    }

    public Integer getRatingValue() {
        return ratingValue;
    }

    public void setRatingValue(Integer ratingValue) {
        this.ratingValue = ratingValue;
    }

    public String getFeedbackText() {
        return feedbackText;
    }

    public void setFeedbackText(String feedbackText) {
        this.feedbackText = feedbackText;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }
}
