package com.rappidrive.application.usecases.approval;

import com.rappidrive.application.exceptions.AdminNotFoundException;
import com.rappidrive.application.exceptions.AdminUnauthorizedException;
import com.rappidrive.application.ports.input.ListPendingApprovalsInputPort;
import com.rappidrive.application.ports.output.AdminUserRepositoryPort;
import com.rappidrive.application.ports.output.DriverApprovalRepositoryPort;
import com.rappidrive.application.ports.output.DriverRepositoryPort;
import com.rappidrive.domain.entities.Driver;
import com.rappidrive.domain.entities.DriverApproval;
import com.rappidrive.domain.enums.AdminRole;
import com.rappidrive.domain.exceptions.DriverNotFoundException;
import com.rappidrive.domain.valueobjects.TenantId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ListPendingApprovalsUseCase implements ListPendingApprovalsInputPort {

    private final DriverApprovalRepositoryPort approvalRepository;
    private final AdminUserRepositoryPort adminRepository;
    private final DriverRepositoryPort driverRepository;

    public ListPendingApprovalsUseCase(DriverApprovalRepositoryPort approvalRepository,
                                       AdminUserRepositoryPort adminRepository,
                                       DriverRepositoryPort driverRepository) {
        this.approvalRepository = approvalRepository;
        this.adminRepository = adminRepository;
        this.driverRepository = driverRepository;
    }

    @Override
    public ListPendingApprovalsResponse execute(ListPendingApprovalsCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        if (command.pageSize() <= 0) {
            throw new IllegalArgumentException("pageSize must be greater than zero");
        }
        if (command.pageNumber() < 0) {
            throw new IllegalArgumentException("pageNumber cannot be negative");
        }

        var admin = adminRepository.findById(command.adminId())
            .orElseThrow(() -> new AdminNotFoundException(command.adminId()));

        if (admin.role() == AdminRole.SUPPORT_ADMIN) {
            throw new AdminUnauthorizedException("Support admins cannot list pending approvals");
        }

        TenantId tenantId = admin.tenantId();
        DriverApprovalRepositoryPort.PaginatedResult<DriverApproval> page =
            approvalRepository.findPendingByTenant(tenantId, command.pageNumber(), command.pageSize());

        List<PendingApprovalDto> approvals = page.items().stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());

        return new ListPendingApprovalsResponse(
            approvals,
            page.totalCount(),
            page.pageNumber(),
            page.pageSize()
        );
    }

    private PendingApprovalDto mapToDto(DriverApproval approval) {
        UUID driverId = approval.driverId();
        Driver driver = driverRepository.findById(driverId)
            .orElseThrow(() -> new DriverNotFoundException(driverId));

        return new PendingApprovalDto(
            approval.id(),
            driverId,
            driver.getFullName(),
            driver.getEmail().getValue(),
            approval.submittedAt(),
            parseDocuments(approval.submittedDocuments())
        );
    }

    private static List<String> parseDocuments(String jsonArray) {
        if (jsonArray == null || jsonArray.isBlank()) {
            return List.of();
        }
        String trimmed = jsonArray.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        if (trimmed.isBlank()) {
            return List.of();
        }
        String[] parts = trimmed.split(",");
        return java.util.Arrays.stream(parts)
            .map(String::trim)
            .map(s -> s.replaceAll("^\"|\"$", ""))
            .filter(s -> !s.isBlank())
            .collect(Collectors.toList());
    }
}
