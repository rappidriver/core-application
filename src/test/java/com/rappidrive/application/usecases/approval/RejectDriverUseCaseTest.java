package com.rappidrive.application.usecases.approval;

import com.rappidrive.application.exceptions.AdminNotFoundException;
import com.rappidrive.application.exceptions.AdminUnauthorizedException;
import com.rappidrive.application.exceptions.ApprovalNotFoundException;
import com.rappidrive.application.ports.input.RejectDriverInputPort.RejectDriverCommand;
import com.rappidrive.application.ports.output.AdminUserRepositoryPort;
import com.rappidrive.application.ports.output.DriverApprovalRepositoryPort;
import com.rappidrive.application.ports.output.DriverRepositoryPort;
import com.rappidrive.domain.entities.Driver;
import com.rappidrive.domain.entities.DriverApproval;
import com.rappidrive.domain.enums.AdminRole;
import com.rappidrive.domain.enums.ApprovalStatus;
import com.rappidrive.domain.events.DomainEvent;
import com.rappidrive.domain.events.DomainEventHandler;
import com.rappidrive.domain.events.DomainEventPublisher;
import com.rappidrive.domain.events.DriverRejectedEvent;
import com.rappidrive.domain.exceptions.InvalidApprovalStateException;
import com.rappidrive.domain.valueobjects.AdminUser;
import com.rappidrive.domain.valueobjects.Email;
import com.rappidrive.domain.valueobjects.TenantId;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RejectDriverUseCaseTest {

    @Mock
    private DriverApprovalRepositoryPort approvalRepository;
    @Mock
    private AdminUserRepositoryPort adminRepository;
    @Mock
    private DriverRepositoryPort driverRepository;

    private RejectDriverUseCase useCase;
    private RejectEventCollector eventCollector;

    @BeforeEach
    void setUp() {
        useCase = new RejectDriverUseCase(approvalRepository, adminRepository, driverRepository);
        eventCollector = new RejectEventCollector();
        DomainEventPublisher.instance().clearHandlers();
        DomainEventPublisher.instance().register(eventCollector);
    }

    @AfterEach
    void tearDown() {
        DomainEventPublisher.instance().reset();
    }

    @Test
    void shouldRejectDriverWithPermanentBan() {
        UUID adminId = UUID.randomUUID();
        UUID approvalId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        TenantId tenantId = new TenantId(UUID.randomUUID());
        AdminUser admin = new AdminUser(adminId, new Email("admin@ex.com"), AdminRole.SUPER_ADMIN, "Alice Admin", tenantId, LocalDateTime.now());
        when(adminRepository.findById(adminId)).thenReturn(Optional.of(admin));

        DriverApproval approval = new DriverApproval(approvalId, driverId, tenantId, "[\"doc1\"]");
        when(approvalRepository.findById(approvalId)).thenReturn(Optional.of(approval));
        when(approvalRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Driver driver = mock(Driver.class);
        when(driver.getId()).thenReturn(driverId);
        when(driver.getTenantId()).thenReturn(tenantId);
        doNothing().when(driver).block();
        when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(driverRepository.save(driver)).thenReturn(driver);

        var response = useCase.execute(new RejectDriverCommand(adminId, approvalId, "invalid documents", true));

        assertEquals(ApprovalStatus.REJECTED.name(), response.status());
        assertEquals("invalid documents", response.rejectionReason());
        verify(driver).block();
        verify(approvalRepository).save(approval);
        verify(driverRepository).save(driver);

        assertEquals(1, eventCollector.events.size());
        DriverRejectedEvent event = eventCollector.events.getFirst();
        assertEquals(driverId, event.driverId());
        assertEquals(approvalId, event.approvalRequestId());
        assertTrue(event.permanentBan());
    }

    @Test
    void shouldFailWhenRejectionReasonMissing() {
        UUID adminId = UUID.randomUUID();
        UUID approvalId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(new RejectDriverCommand(
            adminId,
            approvalId,
            " ",
            false
        )));
    }

    @Test
    void shouldFailWhenAdminUnauthorized() {
        UUID adminId = UUID.randomUUID();
        UUID approvalId = UUID.randomUUID();
        TenantId tenantId = new TenantId(UUID.randomUUID());
        AdminUser admin = new AdminUser(adminId, new Email("support@ex.com"), AdminRole.SUPPORT_ADMIN, "Support", tenantId, LocalDateTime.now());
        when(adminRepository.findById(adminId)).thenReturn(Optional.of(admin));

        assertThrows(AdminUnauthorizedException.class, () -> useCase.execute(new RejectDriverCommand(
            adminId,
            approvalId,
            "invalid",
            false
        )));
        verifyNoInteractions(approvalRepository);
    }

    @Test
    void shouldFailWhenApprovalNotFound() {
        UUID adminId = UUID.randomUUID();
        UUID approvalId = UUID.randomUUID();
        TenantId tenantId = new TenantId(UUID.randomUUID());
        AdminUser admin = new AdminUser(adminId, new Email("admin@ex.com"), AdminRole.SUPER_ADMIN, "Alice Admin", tenantId, LocalDateTime.now());
        when(adminRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(approvalRepository.findById(approvalId)).thenReturn(Optional.empty());

        assertThrows(ApprovalNotFoundException.class, () -> useCase.execute(new RejectDriverCommand(
            adminId,
            approvalId,
            "invalid",
            false
        )));
    }

    @Test
    void shouldFailWhenAlreadyReviewed() {
        UUID adminId = UUID.randomUUID();
        TenantId tenantId = new TenantId(UUID.randomUUID());
        AdminUser admin = new AdminUser(adminId, new Email("admin@ex.com"), AdminRole.SUPER_ADMIN, "Alice Admin", tenantId, LocalDateTime.now());
        when(adminRepository.findById(adminId)).thenReturn(Optional.of(admin));

        DriverApproval approval = new DriverApproval(UUID.randomUUID(), UUID.randomUUID(), tenantId, "[\"doc\"]", ApprovalStatus.APPROVED, LocalDateTime.now(), LocalDateTime.now(), adminId, null);
        when(approvalRepository.findById(approval.id())).thenReturn(Optional.of(approval));

        assertThrows(InvalidApprovalStateException.class, () -> useCase.execute(new RejectDriverCommand(
            adminId,
            approval.id(),
            "invalid",
            false
        )));
    }

    private static class RejectEventCollector implements DomainEventHandler<DriverRejectedEvent> {
        private final List<DriverRejectedEvent> events = new ArrayList<>();

        @Override
        public void handle(DriverRejectedEvent event) {
            events.add(event);
        }

        @Override
        public boolean canHandle(DomainEvent event) {
            return event instanceof DriverRejectedEvent;
        }
    }
}
