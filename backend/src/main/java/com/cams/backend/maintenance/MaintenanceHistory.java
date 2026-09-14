package com.cams.backend.maintenance;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import org.hibernate.type.SqlTypes;

import com.cams.backend.asset.AssetCondition;
import com.cams.backend.asset.AssetStatus;

/** Created when a complaint reaches CLOSED (FR-HIST-001); one row per complaint. */
@Entity
@Table(name = "maintenance_history")
public class MaintenanceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    // asset, complaint and user_account belong to other modules: id references only (ADR-0002).
    @Column(name = "asset_id", nullable = false)
    private UUID assetId;

    @Column(name = "complaint_id", nullable = false)
    private UUID complaintId;

    @Column(name = "work_summary", nullable = false)
    private String workSummary;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "repair_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal repairCost;

    @Column(name = "worker_id", nullable = false)
    private UUID workerId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "resulting_status", nullable = false, columnDefinition = "asset_status")
    private AssetStatus resultingStatus;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "resulting_condition", nullable = false, columnDefinition = "asset_condition")
    private AssetCondition resultingCondition;

    // Ids of complaint_photo rows of kind WORKER_COMPLETION (uuid[] column in V1).
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "completion_photo_ids", nullable = false, columnDefinition = "uuid[]")
    private List<UUID> completionPhotoIds = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public void setAssetId(UUID assetId) {
        this.assetId = assetId;
    }

    public UUID getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(UUID complaintId) {
        this.complaintId = complaintId;
    }

    public String getWorkSummary() {
        return workSummary;
    }

    public void setWorkSummary(String workSummary) {
        this.workSummary = workSummary;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public BigDecimal getRepairCost() {
        return repairCost;
    }

    public void setRepairCost(BigDecimal repairCost) {
        this.repairCost = repairCost;
    }

    public UUID getWorkerId() {
        return workerId;
    }

    public void setWorkerId(UUID workerId) {
        this.workerId = workerId;
    }

    public AssetStatus getResultingStatus() {
        return resultingStatus;
    }

    public void setResultingStatus(AssetStatus resultingStatus) {
        this.resultingStatus = resultingStatus;
    }

    public AssetCondition getResultingCondition() {
        return resultingCondition;
    }

    public void setResultingCondition(AssetCondition resultingCondition) {
        this.resultingCondition = resultingCondition;
    }

    // Collections.unmodifiableList(new ArrayList<>(...)), not List.copyOf,
    // because List.copyOf rejects a null element and this must stay safe to
    // call even if the underlying list ever contains one.
    public List<UUID> getCompletionPhotoIds() {
        return Collections.unmodifiableList(new ArrayList<>(completionPhotoIds));
    }

    public void setCompletionPhotoIds(List<UUID> completionPhotoIds) {
        this.completionPhotoIds = new ArrayList<>(completionPhotoIds);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
