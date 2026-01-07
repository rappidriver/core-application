package com.rappidrive.domain.valueobjects;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TenantIdTest {

    @Test
    void shouldCreateTenantIdFromUUID() {
        UUID uuid = UUID.randomUUID();
        TenantId tenantId = new TenantId(uuid);
        
        assertEquals(uuid, tenantId.getValue());
    }

    @Test
    void shouldCreateTenantIdFromString() {
        String uuidString = "123e4567-e89b-12d3-a456-426614174000";
        TenantId tenantId = TenantId.fromString(uuidString);
        
        assertEquals(uuidString, tenantId.asString());
    }

    @Test
    void shouldGenerateRandomTenantId() {
        TenantId tenantId1 = TenantId.generate();
        TenantId tenantId2 = TenantId.generate();
        
        assertNotNull(tenantId1.getValue());
        assertNotNull(tenantId2.getValue());
        assertNotEquals(tenantId1, tenantId2); // Random UUIDs should be different
    }

    @Test
    void shouldReturnUUIDAsString() {
        String uuidString = "123e4567-e89b-12d3-a456-426614174000";
        TenantId tenantId = TenantId.fromString(uuidString);
        
        assertEquals(uuidString, tenantId.asString());
    }

    @Test
    void shouldRejectNullUUID() {
        assertThrows(IllegalArgumentException.class, 
            () -> new TenantId(null));
    }

    @Test
    void shouldRejectNullString() {
        assertThrows(IllegalArgumentException.class, 
            () -> TenantId.fromString(null));
    }

    @Test
    void shouldRejectEmptyString() {
        assertThrows(IllegalArgumentException.class, 
            () -> TenantId.fromString(""));
    }

    @Test
    void shouldRejectBlankString() {
        assertThrows(IllegalArgumentException.class, 
            () -> TenantId.fromString("   "));
    }

    @Test
    void shouldRejectInvalidUUIDFormat() {
        assertThrows(IllegalArgumentException.class, 
            () -> TenantId.fromString("not-a-valid-uuid"));
    }

    @Test
    void shouldRejectInvalidUUIDFormatWithPartialUUID() {
        assertThrows(IllegalArgumentException.class, 
            () -> TenantId.fromString("123e4567-e89b"));
    }

    @Test
    void shouldHaveEqualityBasedOnUUID() {
        UUID uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        TenantId tenantId1 = new TenantId(uuid);
        TenantId tenantId2 = new TenantId(uuid);
        TenantId tenantId3 = TenantId.fromString("123e4567-e89b-12d3-a456-426614174001");
        
        assertEquals(tenantId1, tenantId2);
        assertNotEquals(tenantId1, tenantId3);
    }

    @Test
    void shouldHaveSameHashCodeForEqualTenantIds() {
        UUID uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        TenantId tenantId1 = new TenantId(uuid);
        TenantId tenantId2 = new TenantId(uuid);
        
        assertEquals(tenantId1.hashCode(), tenantId2.hashCode());
    }

    @Test
    void shouldReturnUUIDStringInToString() {
        String uuidString = "123e4567-e89b-12d3-a456-426614174000";
        TenantId tenantId = TenantId.fromString(uuidString);
        
        assertEquals(uuidString, tenantId.toString());
    }

    @Test
    void shouldAcceptLowercaseUUID() {
        String lowercase = "123e4567-e89b-12d3-a456-426614174000";
        TenantId tenantId = TenantId.fromString(lowercase);
        
        assertNotNull(tenantId);
        assertEquals(lowercase, tenantId.asString());
    }

    @Test
    void shouldAcceptUppercaseUUID() {
        String uppercase = "123E4567-E89B-12D3-A456-426614174000";
        TenantId tenantId = TenantId.fromString(uppercase);
        
        assertNotNull(tenantId);
        // UUID.fromString normalizes to lowercase
        assertEquals(uppercase.toLowerCase(), tenantId.asString());
    }
}
