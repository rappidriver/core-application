package com.rappidrive.infrastructure.persistence.entities;

import com.rappidrive.domain.enums.ApprovalStatus;
import com.rappidrive.domain.valueobjects.TenantId;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "driver_approvals")
public class DriverApprovalJpaEntity {

    @Id
    private UUID id;

    @Column(name = "driver_id", nullable = false)
    private UUID driverId;

    @Column(name = "tenant_id", nullable = false)
    private TenantId tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalStatus status;

    @Column(name = "submitted_documents", nullable = false, columnDefinition = "TEXT")
    private String submittedDocuments;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "reviewed_by_admin_id")
    private UUID reviewedByAdminId;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public DriverApprovalJpaEntity() {}

    public DriverApprovalJpaEntity(UUID id,
                                   UUID driverId,
                                   TenantId tenantId,
                                   ApprovalStatus status,
                                   String submittedDocuments,
                                   LocalDateTime submittedAt,
                                   LocalDateTime reviewedAt,
                                   UUID reviewedByAdminId,
                                   String rejectionReason,
                                   LocalDateTime createdAt,
                                   LocalDateTime updatedAt) {
        this.id = id;
        this.driverId = driverId;
        this.tenantId = tenantId;
        this.status = status;
        this.submittedDocuments = submittedDocuments;
        this.submittedAt = submittedAt;
        this.reviewedAt = reviewedAt;
        this.reviewedByAdminId = reviewedByAdminId;
        this.rejectionReason = rejectionReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.submittedAt == null) {
            this.submittedAt = now;
        }
        if (this.status == null) {
            this.status = ApprovalStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getDriverId() { return driverId; }
    public TenantId getTenantId() { return tenantId; }
    public ApprovalStatus getStatus() { return status; }
    public String getSubmittedDocuments() { return submittedDocuments; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public UUID getReviewedByAdminId() { return reviewedByAdminId; }
    public String getRejectionReason() { return rejectionReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setStatus(ApprovalStatus status) { this.status = status; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public void setReviewedByAdminId(UUID reviewedByAdminId) { this.reviewedByAdminId = reviewedByAdminId; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
