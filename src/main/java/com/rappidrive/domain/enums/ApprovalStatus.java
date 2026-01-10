package com.rappidrive.domain.enums;

/**
 * Status of a driver approval request.
 * 
 * Workflow:
 * PENDING → APPROVED or PENDING → REJECTED
 * 
 * Once APPROVED or REJECTED, the status is final and cannot be changed.
 */
public enum ApprovalStatus {
    /**
     * Approval submitted, awaiting admin review.
     */
    PENDING,
    
    /**
     * Approval granted, driver activated on the platform.
     */
    APPROVED,
    
    /**
     * Approval rejected by admin.
     */
    REJECTED
}
