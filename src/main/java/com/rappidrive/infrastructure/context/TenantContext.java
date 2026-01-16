package com.rappidrive.infrastructure.context;

import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.infrastructure.context.exceptions.TenantNotSetException;

import java.util.Optional;

/**
 * Thread-local context for storing the current TenantId during request lifecycle.
 * Provides thread-safe access to the current tenant in a multi-threaded environment.
 * 
 * <p>Usage:
 * <pre>
 * // In filter or interceptor
 * TenantContext.setTenant(tenantId);
 * 
 * // In controllers or use cases
 * TenantId currentTenant = TenantContext.getTenant();
 * 
 * // Clean up (in filter finally block)
 * TenantContext.clear();
 * </pre>
 * 
 * <p><b>CRITICAL:</b> Always call {@link #clear()} in a finally block to prevent memory leaks
 * when using thread pools or virtual threads.
 */
public final class TenantContext {
    
    private static final ThreadLocal<TenantId> CURRENT_TENANT = new ThreadLocal<>();
    
    private TenantContext() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Sets the current tenant for the thread.
     * 
     * @param tenantId the tenant identifier to set (must not be null)
     * @throws IllegalArgumentException if tenantId is null
     */
    public static void setTenant(TenantId tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("TenantId cannot be null");
        }
        CURRENT_TENANT.set(tenantId);
    }
    
    /**
     * Returns the current tenant for the thread.
     * 
     * @return the current TenantId
     * @throws TenantNotSetException if no tenant has been set in the current context
     */
    public static TenantId getTenant() {
        TenantId tenantId = CURRENT_TENANT.get();
        if (tenantId == null) {
            throw new TenantNotSetException("No tenant set in current context. " +
                "Ensure TenantResolverFilter is configured and X-Tenant-ID header is present.");
        }
        return tenantId;
    }
    
    /**
     * Returns the current tenant as an Optional.
     * Useful for public endpoints where tenant is optional.
     * 
     * @return Optional containing the current TenantId, or empty if not set
     */
    public static Optional<TenantId> getTenantIfPresent() {
        return Optional.ofNullable(CURRENT_TENANT.get());
    }
    
    /**
     * Clears the current tenant from the thread-local context.
     * 
     * <p><b>IMPORTANT:</b> This MUST be called in a finally block to prevent memory leaks,
     * especially when using thread pools or virtual threads where threads are reused.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
