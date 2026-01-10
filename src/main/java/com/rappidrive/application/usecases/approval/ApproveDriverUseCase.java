package com.rappidrive.application.usecases.approval;

import com.rappidrive.application.exceptions.AdminNotFoundException;
import com.rappidrive.application.exceptions.AdminUnauthorizedException;
import com.rappidrive.application.exceptions.ApprovalNotFoundException;
import com.rappidrive.application.ports.input.ApproveDriverInputPort;
import com.rappidrive.application.ports.output.AdminUserRepositoryPort;
import com.rappidrive.application.ports.output.DriverApprovalRepositoryPort;
import com.rappidrive.application.ports.output.DriverRepositoryPort;
import com.rappidrive.domain.entities.Driver;
import com.rappidrive.domain.entities.DriverApproval;
import com.rappidrive.domain.enums.AdminRole;
import com.rappidrive.domain.events.DomainEventPublisher;
import com.rappidrive.domain.events.DriverApprovedEvent;
import com.rappidrive.domain.exceptions.InvalidApprovalStateException;
import com.rappidrive.domain.exceptions.DriverNotFoundException;
import com.rappidrive.domain.valueobjects.TenantId;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class ApproveDriverUseCase implements ApproveDriverInputPort {

    private final DriverApprovalRepositoryPort approvalRepository;
    private final AdminUserRepositoryPort adminRepository;
    private final DriverRepositoryPort driverRepository;
    private final DomainEventPublisher eventPublisher;

    public ApproveDriverUseCase(DriverApprovalRepositoryPort approvalRepository,
                                AdminUserRepositoryPort adminRepository,
                                DriverRepositoryPort driverRepository) {
        this.approvalRepository = approvalRepository;
        this.adminRepository = adminRepository;
        this.driverRepository = driverRepository;
        this.eventPublisher = DomainEventPublisher.instance();
    }

    @Override
    public ApproveDriverResponse execute(ApproveDriverCommand command) {
        validateCommand(command);

        var admin = adminRepository.findById(command.adminId())
            .orElseThrow(() -> new AdminNotFoundException(command.adminId()));

        ensureAdminAuthorized(admin.role());

        DriverApproval approval = approvalRepository.findById(command.approvalId())
            .orElseThrow(() -> new ApprovalNotFoundException(command.approvalId()));

        ensureSameTenant(admin.tenantId(), approval.tenantId());

        ensurePending(approval);
        // State validation happens before hitting the driver repository to surface correct domain error
        approval.approve(admin.id());

        Driver driver = driverRepository.findById(approval.driverId())
            .orElseThrow(() -> new DriverNotFoundException(approval.driverId()));
        ensureSameTenant(admin.tenantId(), driver.getTenantId());
        driver.activate();

        approvalRepository.save(approval);
        driverRepository.save(driver);

        eventPublisher.publish(new DriverApprovedEvent(
            UUID.randomUUID().toString(),
            LocalDateTime.now(),
            driver.getId(),
            approval.id(),
            admin.id(),
            admin.fullName()
        ));

        return new ApproveDriverResponse(approval.id(), driver.getId(), approval.status().name());
    }

    private static void validateCommand(ApproveDriverCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        if (command.adminId() == null) {
            throw new IllegalArgumentException("Admin ID cannot be null");
        }
        if (command.approvalId() == null) {
            throw new IllegalArgumentException("Approval ID cannot be null");
        }
    }

    private static void ensureAdminAuthorized(AdminRole role) {
        if (role == AdminRole.SUPPORT_ADMIN) {
            throw new AdminUnauthorizedException("Support admins cannot approve drivers");
        }
    }

    private static void ensureSameTenant(TenantId adminTenant, TenantId resourceTenant) {
        if (!Objects.equals(adminTenant, resourceTenant)) {
            throw new AdminUnauthorizedException("Admin cannot manage approvals from another tenant");
        }
    }

    private static void ensurePending(DriverApproval approval) {
        if (approval.isFinalized()) {
            throw new InvalidApprovalStateException("Approval already reviewed with status: " + approval.status());
        }
    }
}
