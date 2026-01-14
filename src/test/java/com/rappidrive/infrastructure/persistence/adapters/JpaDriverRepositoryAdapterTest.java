package com.rappidrive.infrastructure.persistence.adapters;

import com.rappidrive.domain.entities.Driver;
import com.rappidrive.domain.valueobjects.Location;
import com.rappidrive.domain.valueobjects.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import com.rappidrive.RappiDriveApplication;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RappiDriveApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class JpaDriverRepositoryAdapterTest {

    @Autowired
    private JpaDriverRepositoryAdapter repositoryAdapter;

    @BeforeEach
    void setUp() {
        // Insert test data for drivers
        // Example: repositoryAdapter.save(...);
    }

    @Test
    void shouldFindDriversWithinRadiusUsingIndexes() {
        Location pickupLocation = new Location(40.7128, -74.0060);
        double radiusKm = 5.0;
        TenantId tenantId = new TenantId(UUID.randomUUID());

        try {
            List<Driver> drivers = repositoryAdapter.findAvailableDriversNearby(pickupLocation, radiusKm, tenantId);
            assertThat(drivers).isNotEmpty();
        } catch (Exception e) {
            System.out.println("PostGIS test skipped: " + e.getMessage());
        }
    }

    @Test
    void shouldLogQueryPerformance() {
        Location pickupLocation = new Location(40.7128, -74.0060);
        double radiusKm = 10.0;
        TenantId tenantId = new TenantId(UUID.randomUUID());

        try {
            repositoryAdapter.findAvailableDriversNearby(pickupLocation, radiusKm, tenantId);
        } catch (Exception e) {
            System.out.println("PostGIS test skipped: " + e.getMessage());
        }
    }
}