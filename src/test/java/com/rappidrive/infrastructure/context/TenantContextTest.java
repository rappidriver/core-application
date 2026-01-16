package com.rappidrive.infrastructure.context;

import com.rappidrive.domain.valueobjects.TenantId;
import com.rappidrive.infrastructure.context.exceptions.TenantNotSetException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for TenantContext.
 * Validates thread-local tenant storage and retrieval.
 */
class TenantContextTest {
    
    @AfterEach
    void cleanup() {
        // Always clear after each test to prevent leaks
        TenantContext.clear();
    }
    
    @Test
    void shouldSetAndGetTenant() {
        TenantId tenantId = TenantId.generate();
        
        TenantContext.setTenant(tenantId);
        
        TenantId retrieved = TenantContext.getTenant();
        assertThat(retrieved).isEqualTo(tenantId);
    }
    
    @Test
    void shouldThrowExceptionWhenSettingNullTenant() {
        assertThatThrownBy(() -> TenantContext.setTenant(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("TenantId cannot be null");
    }
    
    @Test
    void shouldThrowExceptionWhenGettingTenantBeforeSet() {
        assertThatThrownBy(() -> TenantContext.getTenant())
            .isInstanceOf(TenantNotSetException.class)
            .hasMessageContaining("No tenant set in current context");
    }
    
    @Test
    void shouldReturnEmptyOptionalWhenTenantNotSet() {
        Optional<TenantId> tenant = TenantContext.getTenantIfPresent();
        
        assertThat(tenant).isEmpty();
    }
    
    @Test
    void shouldReturnPresentOptionalWhenTenantIsSet() {
        TenantId tenantId = TenantId.generate();
        TenantContext.setTenant(tenantId);
        
        Optional<TenantId> tenant = TenantContext.getTenantIfPresent();
        
        assertThat(tenant).isPresent();
        assertThat(tenant.get()).isEqualTo(tenantId);
    }
    
    @Test
    void shouldClearTenantSuccessfully() {
        TenantId tenantId = TenantId.generate();
        TenantContext.setTenant(tenantId);
        
        TenantContext.clear();
        
        assertThatThrownBy(() -> TenantContext.getTenant())
            .isInstanceOf(TenantNotSetException.class);
    }
    
    @Test
    void shouldIsolateTenantsBetweenThreads() throws InterruptedException {
        TenantId mainThreadTenant = TenantId.generate();
        TenantContext.setTenant(mainThreadTenant);
        
        // Create a new thread and verify it doesn't see the main thread's tenant
        Thread otherThread = new Thread(() -> {
            // Should not see main thread's tenant
            assertThatThrownBy(() -> TenantContext.getTenant())
                .isInstanceOf(TenantNotSetException.class);
            
            // Set its own tenant
            TenantId otherTenant = TenantId.generate();
            TenantContext.setTenant(otherTenant);
            
            // Verify it sees its own tenant
            assertThat(TenantContext.getTenant()).isEqualTo(otherTenant);
        });
        
        otherThread.start();
        otherThread.join();
        
        // Main thread should still see its own tenant
        assertThat(TenantContext.getTenant()).isEqualTo(mainThreadTenant);
    }
    
    @Test
    void shouldAllowSettingDifferentTenantAfterClear() {
        TenantId firstTenant = TenantId.generate();
        TenantContext.setTenant(firstTenant);
        assertThat(TenantContext.getTenant()).isEqualTo(firstTenant);
        
        TenantContext.clear();
        
        TenantId secondTenant = TenantId.generate();
        TenantContext.setTenant(secondTenant);
        assertThat(TenantContext.getTenant()).isEqualTo(secondTenant);
    }
}
