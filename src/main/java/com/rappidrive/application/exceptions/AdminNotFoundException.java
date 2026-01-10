package com.rappidrive.application.exceptions;

import java.util.UUID;

/**
 * Thrown when an admin is not found during approval operations.
 */
public class AdminNotFoundException extends ApplicationException {
    public AdminNotFoundException(UUID adminId) {
        super("Admin not found with ID: " + adminId);
    }
}
