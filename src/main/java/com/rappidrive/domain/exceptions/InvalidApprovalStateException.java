package com.rappidrive.domain.exceptions;

/**
 * Exception thrown when an invalid state transition is attempted on an approval request.
 * 
 * For example: trying to approve an already rejected request.
 */
public class InvalidApprovalStateException extends DomainException {
    
    public InvalidApprovalStateException(String message) {
        super(message);
    }
    
    public InvalidApprovalStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
