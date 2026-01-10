package com.rappidrive.application.exceptions;

import java.util.UUID;

public class ApprovalNotFoundException extends ApplicationException {
    public ApprovalNotFoundException(UUID approvalId) {
        super("Approval request not found: " + approvalId);
    }
}
