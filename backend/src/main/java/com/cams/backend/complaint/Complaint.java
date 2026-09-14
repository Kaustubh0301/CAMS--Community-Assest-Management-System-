package com.cams.backend.complaint;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

/**
 * Persistence mapping only. Status changes are meant to go through the complaint
 * state machine (COMPLAINT_STATE_MACHINE.md), which is not implemented yet.
 */
@Entity
@Table(name = "complaint")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    // asset, ward, local_body, user_account and worker_assignment belong to other
    // modules: id references only (ADR-0002).
    @Column(name = "asset_id", nullable = false)
    private UUID assetId;

    // ward_id and local_body_id are denormalised from the asset (DATA_MODEL.md §6).
    @Column(name = "ward_id", nullable = false)
    private UUID wardId;

    @Column(name = "local_body_id", nullable = false)
    private UUID localBodyId;

    @Column(name = "reported_by", nullable = false, updatable = false)
    private UUID reportedBy;

    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "complaint_status")
    private ComplaintStatus status = ComplaintStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "priority", columnDefinition = "complaint_priority")
    private ComplaintPriority priority;

    @Column(name = "current_assignment_id")
    private UUID currentAssignmentId;

    @Column(name = "returned_count", nullable = false)
    private int returnedCount = 0;

    @Column(name = "rejected_reason")
    private String rejectedReason;

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    public UUID getId() {
        return id;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public void setAssetId(UUID assetId) {
        this.assetId = assetId;
    }

    public UUID getWardId() {
        return wardId;
    }

    public void setWardId(UUID wardId) {
        this.wardId = wardId;
    }

    public UUID getLocalBodyId() {
        return localBodyId;
    }

    public void setLocalBodyId(UUID localBodyId) {
        this.localBodyId = localBodyId;
    }

    public UUID getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(UUID reportedBy) {
        this.reportedBy = reportedBy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public ComplaintPriority getPriority() {
        return priority;
    }

    public void setPriority(ComplaintPriority priority) {
        this.priority = priority;
    }

    public UUID getCurrentAssignmentId() {
        return currentAssignmentId;
    }

    public void setCurrentAssignmentId(UUID currentAssignmentId) {
        this.currentAssignmentId = currentAssignmentId;
    }

    public int getReturnedCount() {
        return returnedCount;
    }

    public void setReturnedCount(int returnedCount) {
        this.returnedCount = returnedCount;
    }

    public String getRejectedReason() {
        return rejectedReason;
    }

    public void setRejectedReason(String rejectedReason) {
        this.rejectedReason = rejectedReason;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }
}
