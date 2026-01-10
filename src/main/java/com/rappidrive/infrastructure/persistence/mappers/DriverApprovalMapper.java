package com.rappidrive.infrastructure.persistence.mappers;

import com.rappidrive.domain.entities.DriverApproval;
import com.rappidrive.infrastructure.persistence.entities.DriverApprovalJpaEntity;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class DriverApprovalMapper {

    public DriverApprovalJpaEntity toEntity(DriverApproval approval) {
        return new DriverApprovalJpaEntity(
            approval.id(),
            approval.driverId(),
            approval.tenantId(),
            approval.status(),
            approval.submittedDocuments(),
            approval.submittedAt(),
            approval.reviewedAt(),
            approval.reviewedByAdminId(),
            approval.rejectionReason(),
            approval.createdAt(),
            approval.updatedAt()
        );
    }

    public DriverApproval toDomain(DriverApprovalJpaEntity entity) {
        return new DriverApproval(
            entity.getId(),
            entity.getDriverId(),
            entity.getTenantId(),
            entity.getSubmittedDocuments(),
            entity.getStatus(),
            entity.getSubmittedAt(),
            entity.getReviewedAt(),
            entity.getReviewedByAdminId(),
            entity.getRejectionReason()
        );
    }
}
