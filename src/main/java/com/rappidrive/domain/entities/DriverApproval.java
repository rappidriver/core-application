package com.rappidrive.domain.entities;

import com.rappidrive.domain.enums.ApprovalStatus;
import com.rappidrive.domain.exceptions.InvalidApprovalStateException;
import com.rappidrive.domain.valueobjects.TenantId;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root for driver approval workflow.
 * 
 * Represents a request for administrative approval of a driver's documentation
 * and eligibility to operate on the RappiDrive platform.
 * 
 * State Machine:
 * PENDING → APPROVED (via approve()) OR PENDING → REJECTED (via reject())
 * 
 * Invariants:
 * - Status transitions only allowed from PENDING
 * - Once APPROVED or REJECTED, status is final
 * - rejectionReason is required when REJECTED
 * - reviewedByAdminId is required when not PENDING
 */
public class DriverApproval {
    
    private final UUID id;
    private final UUID driverId;
    private final TenantId tenantId;
    private ApprovalStatus status;
    private final String submittedDocuments;  // JSON string with document URLs
    private final LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private UUID reviewedByAdminId;
    private String rejectionReason;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Creates a new approval request for a driver.
     * Initial status is PENDING.
     * 
     * @param id unique identifier for this approval request
     * @param driverId the driver being approved
     * @param tenantId tenant for multi-tenancy isolation
     * @param submittedDocuments JSON string with document URLs
     * @throws IllegalArgumentException if any required field is null
     */
    public DriverApproval(UUID id, UUID driverId, TenantId tenantId,
                                 String submittedDocuments) {
        if (id == null) {
            throw new IllegalArgumentException("Approval request ID cannot be null");
        }
        if (driverId == null) {
            throw new IllegalArgumentException("Driver ID cannot be null");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }
        if (submittedDocuments == null || submittedDocuments.isBlank()) {
            throw new IllegalArgumentException("Submitted documents cannot be null or empty");
        }
        
        this.id = id;
        this.driverId = driverId;
        this.tenantId = tenantId;
        this.submittedDocuments = submittedDocuments;
        this.submittedAt = LocalDateTime.now();
        this.status = ApprovalStatus.PENDING;
        this.reviewedAt = null;
        this.reviewedByAdminId = null;
        this.rejectionReason = null;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Reconstruction constructor for persistence layer.
     * Used by infrastructure adapters to recreate domain entities from storage.
     * 
     * @param id approval request ID
     * @param driverId driver ID
     * @param tenantId tenant ID
     * @param submittedDocuments JSON documents
     * @param status current status
     * @param submittedAt submission timestamp
     * @param reviewedAt review timestamp (null if pending)
     * @param reviewedByAdminId admin ID (null if pending)
     * @param rejectionReason rejection reason (null if not rejected)
     */
    public DriverApproval(UUID id, UUID driverId, TenantId tenantId,
                                 String submittedDocuments, ApprovalStatus status,
                                 LocalDateTime submittedAt, LocalDateTime reviewedAt,
                                 UUID reviewedByAdminId, String rejectionReason) {
        this.id = id;
        this.driverId = driverId;
        this.tenantId = tenantId;
        this.submittedDocuments = submittedDocuments;
        this.status = status;
        this.submittedAt = submittedAt;
        this.reviewedAt = reviewedAt;
        this.reviewedByAdminId = reviewedByAdminId;
        this.rejectionReason = rejectionReason;
        this.createdAt = LocalDateTime.now();  // Will be overridden by JPA
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Domain behavior: Approve this request.
     * 
     * Transitions from PENDING to APPROVED.
     * The approving admin is recorded for audit purposes.
     * 
     * @param adminId UUID of the approving admin
     * @throws DomainException if request is not in PENDING state
     * @throws IllegalArgumentException if adminId is null
     */
    public void approve(UUID adminId) {
        if (status != ApprovalStatus.PENDING) {
            throw new InvalidApprovalStateException(
                "Cannot approve request with status: " + status + 
                ". Only PENDING approvals can be approved."
            );
        }
        if (adminId == null) {
            throw new IllegalArgumentException("Admin ID cannot be null");
        }
        
        this.status = ApprovalStatus.APPROVED;
        this.reviewedByAdminId = adminId;
        this.reviewedAt = LocalDateTime.now();
        this.rejectionReason = null;  // Clear any previous reason
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Domain behavior: Reject this request.
     * 
     * Transitions from PENDING to REJECTED.
     * A rejection reason must be provided for audit and driver feedback purposes.
     * 
     * @param adminId UUID of the rejecting admin
     * @param reason detailed reason for rejection (required)
     * @throws DomainException if request is not in PENDING state
     * @throws IllegalArgumentException if adminId or reason is null/empty
     */
    public void reject(UUID adminId, String reason) {
        if (status != ApprovalStatus.PENDING) {
            throw new InvalidApprovalStateException(
                "Cannot reject request with status: " + status + 
                ". Only PENDING approvals can be rejected."
            );
        }
        if (adminId == null) {
            throw new IllegalArgumentException("Admin ID cannot be null");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason cannot be null or empty");
        }
        
        this.status = ApprovalStatus.REJECTED;
        this.reviewedByAdminId = adminId;
        this.reviewedAt = LocalDateTime.now();
        this.rejectionReason = reason.trim();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Query methods for state checks
    
    /**
     * @return true if this approval is pending review
     */
    public boolean isPending() {
        return status == ApprovalStatus.PENDING;
    }
    
    /**
     * @return true if this approval was approved
     */
    public boolean isApproved() {
        return status == ApprovalStatus.APPROVED;
    }
    
    /**
     * @return true if this approval was rejected
     */
    public boolean isRejected() {
        return status == ApprovalStatus.REJECTED;
    }
    
    /**
     * @return true if this approval has been finalized (not pending)
     */
    public boolean isFinalized() {
        return status != ApprovalStatus.PENDING;
    }
    
    // Accessors
    
    public UUID id() {
        return id;
    }
    
    public UUID driverId() {
        return driverId;
    }
    
    public TenantId tenantId() {
        return tenantId;
    }
    
    public ApprovalStatus status() {
        return status;
    }
    
    public String submittedDocuments() {
        return submittedDocuments;
    }
    
    public LocalDateTime submittedAt() {
        return submittedAt;
    }
    
    public LocalDateTime reviewedAt() {
        return reviewedAt;
    }
    
    public UUID reviewedByAdminId() {
        return reviewedByAdminId;
    }
    
    public String rejectionReason() {
        return rejectionReason;
    }
    
    public LocalDateTime createdAt() {
        return createdAt;
    }
    
    public LocalDateTime updatedAt() {
        return updatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DriverApproval)) return false;
        DriverApproval that = (DriverApproval) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "DriverApproval{" +
                "id=" + id +
                ", driverId=" + driverId +
                ", status=" + status +
                ", submittedAt=" + submittedAt +
                '}';
    }
}
