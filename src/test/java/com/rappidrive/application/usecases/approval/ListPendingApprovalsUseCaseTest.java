package com.rappidrive.application.usecases.approval;

import com.rappidrive.application.exceptions.AdminNotFoundException;
import com.rappidrive.application.exceptions.AdminUnauthorizedException;
import com.rappidrive.application.ports.input.ListPendingApprovalsInputPort.ListPendingApprovalsCommand;
import com.rappidrive.application.ports.input.ListPendingApprovalsInputPort.ListPendingApprovalsResponse;
import com.rappidrive.application.ports.output.AdminUserRepositoryPort;
import com.rappidrive.application.ports.output.DriverApprovalRepositoryPort;
import com.rappidrive.application.ports.output.DriverRepositoryPort;
import com.rappidrive.domain.entities.Driver;
import com.rappidrive.domain.entities.DriverApproval;
import com.rappidrive.domain.enums.AdminRole;
import com.rappidrive.domain.enums.DriverStatus;
import com.rappidrive.domain.exceptions.DriverNotFoundException;
import com.rappidrive.domain.valueobjects.Email;
import com.rappidrive.domain.valueobjects.TenantId;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListPendingApprovalsUseCaseTest {

    @Mock
    private DriverApprovalRepositoryPort approvalRepository;
    @Mock
    private AdminUserRepositoryPort adminRepository;
    @Mock
    private DriverRepositoryPort driverRepository;

    @Test
    void shouldListPendingApprovals() {
        UUID adminId = UUID.randomUUID();
        TenantId tenantId = new TenantId(UUID.randomUUID());
        var admin = new com.rappidrive.domain.valueobjects.AdminUser(
            adminId,
            new Email("admin@rappidrive.com"),
            AdminRole.SUPER_ADMIN,
            "Alice Admin",
            tenantId,
            LocalDateTime.now()
        );
        when(adminRepository.findById(adminId)).thenReturn(Optional.of(admin));

        DriverApproval approval = new DriverApproval(
            UUID.randomUUID(),
            UUID.randomUUID(),
            tenantId,
            "[\"doc1\", \"doc2\"]"
        );
        Driver driver = mock(Driver.class);
        when(driver.getFullName()).thenReturn("Driver One");
        when(driver.getEmail()).thenReturn(new Email("driver@ex.com"));
        when(driverRepository.findById(approval.driverId())).thenReturn(Optional.of(driver));

        var page = new DriverApprovalRepositoryPort.PaginatedResult<>(
            List.of(approval),
            1,
            0,
            10
        );
        when(approvalRepository.findPendingByTenant(eq(tenantId), anyInt(), anyInt())).thenReturn(page);

        ListPendingApprovalsUseCase useCase = new ListPendingApprovalsUseCase(
            approvalRepository, adminRepository, driverRepository
        );

        ListPendingApprovalsResponse response = useCase.execute(new ListPendingApprovalsCommand(
            adminId,
            0,
            10
        ));

        assertEquals(1, response.approvals().size());
        var dto = response.approvals().getFirst();
        assertEquals(approval.id(), dto.approvalId());
        assertEquals(approval.driverId(), dto.driverId());
        assertEquals("Driver One", dto.driverName());
        assertEquals("driver@ex.com", dto.driverEmail());
        assertEquals(2, dto.documents().size());
        verify(approvalRepository).findPendingByTenant(tenantId, 0, 10);
        verify(driverRepository).findById(approval.driverId());
    }

    @Test
    void shouldFailWhenAdminNotFound() {
        UUID adminId = UUID.randomUUID();
        when(adminRepository.findById(adminId)).thenReturn(Optional.empty());

        ListPendingApprovalsUseCase useCase = new ListPendingApprovalsUseCase(
            approvalRepository, adminRepository, driverRepository
        );

        assertThrows(AdminNotFoundException.class, () -> useCase.execute(new ListPendingApprovalsCommand(
            adminId,
            0,
            10
        )));
    }

    @Test
    void shouldFailWhenAdminNotAuthorized() {
        UUID adminId = UUID.randomUUID();
        TenantId tenantId = new TenantId(UUID.randomUUID());
        var admin = new com.rappidrive.domain.valueobjects.AdminUser(
            adminId,
            new Email("support@rappidrive.com"),
            AdminRole.SUPPORT_ADMIN,
            "Support Admin",
            tenantId,
            LocalDateTime.now()
        );
        when(adminRepository.findById(adminId)).thenReturn(Optional.of(admin));

        ListPendingApprovalsUseCase useCase = new ListPendingApprovalsUseCase(
            approvalRepository, adminRepository, driverRepository
        );

        assertThrows(AdminUnauthorizedException.class, () -> useCase.execute(new ListPendingApprovalsCommand(
            adminId,
            0,
            10
        )));
        verifyNoInteractions(approvalRepository);
    }

    @Test
    void shouldFailWhenDriverNotFound() {
        UUID adminId = UUID.randomUUID();
        TenantId tenantId = new TenantId(UUID.randomUUID());
        var admin = new com.rappidrive.domain.valueobjects.AdminUser(
            adminId,
            new Email("admin@rappidrive.com"),
            AdminRole.SUPER_ADMIN,
            "Alice Admin",
            tenantId,
            LocalDateTime.now()
        );
        when(adminRepository.findById(adminId)).thenReturn(Optional.of(admin));

        DriverApproval approval = new DriverApproval(
            UUID.randomUUID(),
            UUID.randomUUID(),
            tenantId,
            "[\"doc1\"]"
        );
        var page = new DriverApprovalRepositoryPort.PaginatedResult<>(List.of(approval), 1, 0, 10);
        when(approvalRepository.findPendingByTenant(eq(tenantId), anyInt(), anyInt())).thenReturn(page);
        when(driverRepository.findById(approval.driverId())).thenReturn(Optional.empty());

        ListPendingApprovalsUseCase useCase = new ListPendingApprovalsUseCase(
            approvalRepository, adminRepository, driverRepository
        );

        assertThrows(DriverNotFoundException.class, () -> useCase.execute(new ListPendingApprovalsCommand(
            adminId,
            0,
            10
        )));
    }

    @Test
    void shouldValidatePagingArguments() {
        UUID adminId = UUID.randomUUID();
        ListPendingApprovalsUseCase useCase = new ListPendingApprovalsUseCase(
            approvalRepository, adminRepository, driverRepository
        );

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(new ListPendingApprovalsCommand(
            adminId,
            -1,
            10
        )));

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(new ListPendingApprovalsCommand(
            adminId,
            0,
            0
        )));
    }
}
