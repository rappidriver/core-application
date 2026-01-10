package com.rappidrive.application.usecases.approval;

import com.rappidrive.application.ports.input.SubmitDriverApprovalInputPort.SubmitDriverApprovalCommand;
import com.rappidrive.application.ports.output.DriverApprovalRepositoryPort;
import com.rappidrive.application.ports.output.DriverRepositoryPort;
import com.rappidrive.domain.entities.Driver;
import com.rappidrive.domain.entities.DriverApproval;
import com.rappidrive.domain.enums.DriverStatus;
import com.rappidrive.domain.events.DomainEvent;
import com.rappidrive.domain.events.DomainEventHandler;
import com.rappidrive.domain.events.DomainEventPublisher;
import com.rappidrive.domain.events.DriverApprovalSubmittedEvent;
import com.rappidrive.domain.exceptions.DriverNotFoundException;
import com.rappidrive.domain.exceptions.InvalidDriverStateException;
import com.rappidrive.domain.valueobjects.TenantId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmitDriverApprovalUseCaseTest {

    @Mock
    private DriverApprovalRepositoryPort approvalRepository;
    @Mock
    private DriverRepositoryPort driverRepository;

    private SubmitDriverApprovalUseCase useCase;
    private CollectingHandler eventHandler;

    @BeforeEach
    void setUp() {
        useCase = new SubmitDriverApprovalUseCase(approvalRepository, driverRepository);
        eventHandler = new CollectingHandler();
        DomainEventPublisher.instance().clearHandlers();
        DomainEventPublisher.instance().register(eventHandler);
    }

    @AfterEach
    void tearDown() {
        DomainEventPublisher.instance().reset();
    }

    @Test
    void shouldSubmitApprovalWhenDriverIsPending() {
        UUID driverId = UUID.randomUUID();
        UUID approvalId = UUID.randomUUID();
        TenantId tenantId = new TenantId(UUID.randomUUID());

        Driver driver = mock(Driver.class);
        when(driver.getId()).thenReturn(driverId);
        when(driver.getTenantId()).thenReturn(tenantId);
        when(driver.getStatus()).thenReturn(DriverStatus.PENDING_APPROVAL);
        when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));

        ArgumentCaptor<DriverApproval> approvalCaptor = ArgumentCaptor.forClass(DriverApproval.class);
        when(approvalRepository.save(any())).thenAnswer(invocation -> {
            DriverApproval approval = invocation.getArgument(0);
            // Simulate persistence returning the same aggregate
            return approval;
        });

        var response = useCase.execute(new SubmitDriverApprovalCommand(
            driverId,
            List.of("cnh.pdf", "comprovante.pdf")
        ));

        assertNotNull(response);
        assertEquals("PENDING", response.status());
        verify(driverRepository).findById(driverId);
        verify(approvalRepository).save(approvalCaptor.capture());

        DriverApproval saved = approvalCaptor.getValue();
        assertEquals(driverId, saved.driverId());
        assertEquals(tenantId, saved.tenantId());
        assertTrue(saved.submittedDocuments().contains("cnh.pdf"));
        assertTrue(saved.submittedDocuments().contains("comprovante.pdf"));

        assertEquals(1, eventHandler.events.size());
        DomainEvent published = eventHandler.events.getFirst();
        assertTrue(published instanceof DriverApprovalSubmittedEvent);
        DriverApprovalSubmittedEvent event = (DriverApprovalSubmittedEvent) published;
        assertEquals(driverId, event.driverId());
        assertEquals(saved.id(), event.approvalRequestId());
        assertEquals(2, event.documentCount());
    }

    @Test
    void shouldFailWhenDriverNotFound() {
        UUID driverId = UUID.randomUUID();
        when(driverRepository.findById(driverId)).thenReturn(Optional.empty());

        assertThrows(DriverNotFoundException.class, () -> useCase.execute(new SubmitDriverApprovalCommand(
            driverId,
            List.of("doc1", "doc2")
        )));

        verify(approvalRepository, never()).save(any());
        assertTrue(eventHandler.events.isEmpty());
    }

    @Test
    void shouldFailWhenDriverNotPending() {
        UUID driverId = UUID.randomUUID();
        Driver driver = mock(Driver.class);
        when(driver.getStatus()).thenReturn(DriverStatus.ACTIVE);
        when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));

        assertThrows(InvalidDriverStateException.class, () -> useCase.execute(new SubmitDriverApprovalCommand(
            driverId,
            List.of("doc1", "doc2")
        )));

        verify(approvalRepository, never()).save(any());
        assertTrue(eventHandler.events.isEmpty());
    }

    @Test
    void shouldFailWhenDocumentListTooSmall() {
        UUID driverId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(new SubmitDriverApprovalCommand(
            driverId,
            List.of("only-one")
        )));
    }

    @Test
    void shouldFailWhenDocumentListNull() {
        UUID driverId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(new SubmitDriverApprovalCommand(
            driverId,
            null
        )));
    }

    private static class CollectingHandler implements DomainEventHandler<DriverApprovalSubmittedEvent> {
        private final List<DriverApprovalSubmittedEvent> events = new ArrayList<>();

        @Override
        public void handle(DriverApprovalSubmittedEvent event) {
            events.add(event);
        }

        @Override
        public boolean canHandle(DomainEvent event) {
            return event instanceof DriverApprovalSubmittedEvent;
        }
    }
}
