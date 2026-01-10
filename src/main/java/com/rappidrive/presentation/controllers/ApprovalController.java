package com.rappidrive.presentation.controllers;

import com.rappidrive.application.ports.input.ApproveDriverInputPort;
import com.rappidrive.application.ports.input.ListPendingApprovalsInputPort;
import com.rappidrive.application.ports.input.RejectDriverInputPort;
import com.rappidrive.application.ports.input.SubmitDriverApprovalInputPort;
import com.rappidrive.presentation.dto.approval.*;
import com.rappidrive.presentation.mappers.approval.ApprovalDtoMapper;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/approvals")
public class ApprovalController {

    private final SubmitDriverApprovalInputPort submitUseCase;
    private final ListPendingApprovalsInputPort listUseCase;
    private final ApproveDriverInputPort approveUseCase;
    private final RejectDriverInputPort rejectUseCase;
    private final ApprovalDtoMapper mapper;

    public ApprovalController(SubmitDriverApprovalInputPort submitUseCase,
                              ListPendingApprovalsInputPort listUseCase,
                              ApproveDriverInputPort approveUseCase,
                              RejectDriverInputPort rejectUseCase,
                              ApprovalDtoMapper mapper) {
        this.submitUseCase = submitUseCase;
        this.listUseCase = listUseCase;
        this.approveUseCase = approveUseCase;
        this.rejectUseCase = rejectUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<SubmitApprovalResponse> submit(@RequestBody SubmitApprovalRequest request) {
        var command = new SubmitDriverApprovalInputPort.SubmitDriverApprovalCommand(
            UUID.fromString(request.driverId()),
            request.documentUrls()
        );
        var response = submitUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SubmitApprovalResponse(response.approvalRequestId().toString(), response.status()));
    }

    @GetMapping
    public ResponseEntity<PendingApprovalsPageResponse> list(
            @RequestParam UUID adminId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var response = listUseCase.execute(new ListPendingApprovalsInputPort.ListPendingApprovalsCommand(adminId, page, size));
        return ResponseEntity.ok(mapper.toPage(response));
    }

    @PostMapping("/{approvalId}/approve")
    public ResponseEntity<ApproveResponse> approve(@PathVariable UUID approvalId, @RequestBody ApproveRequest request) {
        var result = approveUseCase.execute(new ApproveDriverInputPort.ApproveDriverCommand(request.adminId(), approvalId));
        return ResponseEntity.ok(new ApproveResponse(result.approvalId(), result.driverId(), result.status()));
    }

    @PostMapping("/{approvalId}/reject")
    public ResponseEntity<RejectResponse> reject(@PathVariable UUID approvalId, @RequestBody RejectRequest request) {
        var result = rejectUseCase.execute(new RejectDriverInputPort.RejectDriverCommand(
            request.adminId(),
            approvalId,
            request.rejectionReason(),
            request.permanentBan()
        ));
        return ResponseEntity.ok(new RejectResponse(result.approvalId(), result.driverId(), result.status(), result.rejectionReason()));
    }
}
