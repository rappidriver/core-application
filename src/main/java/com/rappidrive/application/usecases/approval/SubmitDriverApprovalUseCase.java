package com.rappidrive.application.usecases.approval;

import com.rappidrive.application.ports.input.SubmitDriverApprovalInputPort;
import com.rappidrive.application.ports.output.DriverApprovalRepositoryPort;
import com.rappidrive.application.ports.output.DriverRepositoryPort;
import com.rappidrive.domain.entities.Driver;
import com.rappidrive.domain.entities.DriverApproval;
import com.rappidrive.domain.enums.DriverStatus;
import com.rappidrive.domain.events.DomainEventPublisher;
import com.rappidrive.domain.events.DriverApprovalSubmittedEvent;
import com.rappidrive.domain.exceptions.DriverNotFoundException;
import com.rappidrive.domain.exceptions.InvalidDriverStateException;
import com.rappidrive.domain.valueobjects.TenantId;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SubmitDriverApprovalUseCase implements SubmitDriverApprovalInputPort {

    private static final int MIN_DOCUMENTS = 2;

    private final DriverApprovalRepositoryPort approvalRepository;
    private final DriverRepositoryPort driverRepository;
    private final DomainEventPublisher eventPublisher;

    public SubmitDriverApprovalUseCase(DriverApprovalRepositoryPort approvalRepository,
                                       DriverRepositoryPort driverRepository) {
        this.approvalRepository = approvalRepository;
        this.driverRepository = driverRepository;
        this.eventPublisher = DomainEventPublisher.instance();
    }

    @Override
    public SubmitDriverApprovalResponse execute(SubmitDriverApprovalCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        List<String> documents = sanitizeDocuments(command.documentUrls());
        if (documents.size() < MIN_DOCUMENTS) {
            throw new IllegalArgumentException("At least " + MIN_DOCUMENTS + " documents are required for approval");
        }

        Driver driver = driverRepository.findById(command.driverId())
            .orElseThrow(() -> new DriverNotFoundException(command.driverId()));

        if (driver.getStatus() != DriverStatus.PENDING_APPROVAL) {
            throw new InvalidDriverStateException(
                "Driver must be in PENDING_APPROVAL status to submit documents. Current: " + driver.getStatus()
            );
        }

        TenantId tenantId = driver.getTenantId();
        UUID approvalId = UUID.randomUUID();
        DriverApproval approval = new DriverApproval(
            approvalId,
            driver.getId(),
            tenantId,
            toJsonArray(documents)
        );

        DriverApproval saved = approvalRepository.save(approval);

        eventPublisher.publish(new DriverApprovalSubmittedEvent(
            approvalId.toString(),
            LocalDateTime.now(),
            driver.getId(),
            saved.id(),
            documents.size()
        ));

        return new SubmitDriverApprovalResponse(saved.id(), saved.status().name());
    }

    private static List<String> sanitizeDocuments(List<String> documents) {
        if (documents == null) {
            throw new IllegalArgumentException("Document list cannot be null");
        }
        return documents.stream()
            .filter(doc -> doc != null && !doc.isBlank())
            .map(String::trim)
            .collect(Collectors.toList());
    }

    private static String toJsonArray(List<String> items) {
        return items.stream()
            .map(item -> "\"" + item + "\"")
            .collect(Collectors.joining(",", "[", "]"));
    }
}
