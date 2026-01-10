package com.rappidrive.presentation.mappers.approval;

import com.rappidrive.application.ports.input.ListPendingApprovalsInputPort.PendingApprovalDto;
import com.rappidrive.application.ports.input.ListPendingApprovalsInputPort.ListPendingApprovalsResponse;
import com.rappidrive.presentation.dto.approval.PendingApprovalResponse;
import com.rappidrive.presentation.dto.approval.PendingApprovalsPageResponse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ApprovalDtoMapper {

    public PendingApprovalResponse toResponse(PendingApprovalDto dto) {
        return new PendingApprovalResponse(
            dto.approvalId(),
            dto.driverId(),
            dto.driverName(),
            dto.driverEmail(),
            dto.submittedAt(),
            dto.documents()
        );
    }

    public List<PendingApprovalResponse> toResponseList(List<PendingApprovalDto> dtos) {
        return dtos.stream().map(this::toResponse).toList();
    }

    public PendingApprovalsPageResponse toPage(ListPendingApprovalsResponse response) {
        return new PendingApprovalsPageResponse(
            toResponseList(response.approvals()),
            response.totalCount(),
            response.pageNumber(),
            response.pageSize()
        );
    }
}
