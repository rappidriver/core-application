package com.rappidrive.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * End-to-End tests for Vehicle-Driver Assignment API.
 * 
 * This test class validates the complete flow of assigning vehicles to drivers,
 * activating vehicles, and retrieving vehicles by driver.
 * 
 * Prerequisites:
 *  - Docker containers must be running: docker-compose up -d
 *  - PostgreSQL 16 + PostGIS 3.4 available at localhost:5432
 *  - Application running on random port via @SpringBootTest
 * 
 * Scenarios tested:
 * 1. Happy path: assign vehicle to driver
 * 2. Happy path: activate vehicle after assignment
 * 3. Happy path: get vehicles by driver
 * 4. Happy path: get active vehicle by driver
 * 5. Validation errors: missing driver ID
 * 6. Business rules: vehicle/driver not found
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@DisplayName("Vehicle-Driver Assignment E2E Tests")
class VehicleDriverAssignmentE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID tenantId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        RestAssured.port = port;
        RestAssured.basePath = "";
        
        // Clean up any existing test data first
        jdbcTemplate.update("DELETE FROM vehicles");
        jdbcTemplate.update("DELETE FROM drivers");
        jdbcTemplate.update("DELETE FROM tenants");
        
        // Create tenant for the test
        jdbcTemplate.update(
            "INSERT INTO tenants (id, name, slug, active) VALUES (?, ?, ?, ?)",
            tenantId, "Test Tenant", "test-tenant-" + tenantId.toString().substring(0, 8), true
        );
    }

    @AfterEach
    void tearDown() {
        // Clean up test data after each test
        jdbcTemplate.update("DELETE FROM vehicles");
        jdbcTemplate.update("DELETE FROM drivers");
        jdbcTemplate.update("DELETE FROM tenants");
    }

    @Test
    @DisplayName("Happy Path: Assign vehicle to driver")
    void shouldAssignVehicleToDriver() {
        // Given: Create a driver
        String driverId = createDriver("assign.test@example.com", "52998224725");
        
        // And: Create a vehicle
        String vehicleId = createVehicle("ASN1A23");

        // When: Assign vehicle to driver
        var response = given()
            .contentType(ContentType.JSON)
            .body(new AssignVehiclePayload(driverId))
            .when()
            .put("/api/v1/vehicles/{id}/assign", vehicleId);
        
        if (response.statusCode() != 200) {
            System.out.println("ASSIGN - UNEXPECTED STATUS: " + response.statusCode());
            System.out.println("RESPONSE: " + response.body().asString());
        }
        
        // Then: Vehicle should be assigned to driver
        response.then()
            .statusCode(200)
            .body("id", equalTo(vehicleId))
            .body("driverId", equalTo(driverId))
            .body("status", equalTo("INACTIVE"));  // Still inactive until activated
    }

    @Test
    @DisplayName("Happy Path: Activate vehicle after assignment")
    void shouldActivateVehicleAfterAssignment() {
        // Given: Create a driver and vehicle
        String driverId = createDriver("activate.test@example.com", "52998224725");
        String vehicleId = createVehicle("ACT1B23");
        
        // And: Assign vehicle to driver
        given()
            .contentType(ContentType.JSON)
            .body(new AssignVehiclePayload(driverId))
            .when()
            .put("/api/v1/vehicles/{id}/assign", vehicleId)
            .then()
            .statusCode(200);

        // When: Activate the vehicle
        var response = given()
            .when()
            .put("/api/v1/vehicles/{id}/activate", vehicleId);
        
        if (response.statusCode() != 200) {
            System.out.println("ACTIVATE - UNEXPECTED STATUS: " + response.statusCode());
            System.out.println("RESPONSE: " + response.body().asString());
        }
        
        // Then: Vehicle should be active
        response.then()
            .statusCode(200)
            .body("id", equalTo(vehicleId))
            .body("driverId", equalTo(driverId))
            .body("status", equalTo("ACTIVE"));
    }

    @Test
    @DisplayName("Happy Path: Get vehicles by driver")
    void shouldGetVehiclesByDriver() {
        // Given: Create a driver with multiple vehicles
        String driverId = createDriver("multi.vehicle@example.com", "52998224725");
        String vehicleId1 = createVehicle("MUL1A23");
        String vehicleId2 = createVehicle("MUL2B34");
        
        // Assign both vehicles to driver
        given()
            .contentType(ContentType.JSON)
            .body(new AssignVehiclePayload(driverId))
            .when()
            .put("/api/v1/vehicles/{id}/assign", vehicleId1)
            .then()
            .statusCode(200);
            
        given()
            .contentType(ContentType.JSON)
            .body(new AssignVehiclePayload(driverId))
            .when()
            .put("/api/v1/vehicles/{id}/assign", vehicleId2)
            .then()
            .statusCode(200);

        // When: Get vehicles by driver
        var response = given()
            .when()
            .get("/api/v1/vehicles/driver/{driverId}", driverId);
        
        if (response.statusCode() != 200) {
            System.out.println("GET BY DRIVER - UNEXPECTED STATUS: " + response.statusCode());
            System.out.println("RESPONSE: " + response.body().asString());
        }
        
        // Then: Should return both vehicles
        response.then()
            .statusCode(200)
            .body("size()", equalTo(2))
            .body("id", hasItems(vehicleId1, vehicleId2));
    }

    @Test
    @DisplayName("Happy Path: Get active vehicle by driver")
    void shouldGetActiveVehicleByDriver() {
        // Given: Create a driver and vehicle, assign and activate
        String driverId = createDriver("active.vehicle@example.com", "52998224725");
        String vehicleId = createVehicle("ACV1C23");
        
        // Assign and activate
        given()
            .contentType(ContentType.JSON)
            .body(new AssignVehiclePayload(driverId))
            .when()
            .put("/api/v1/vehicles/{id}/assign", vehicleId)
            .then()
            .statusCode(200);
            
        given()
            .when()
            .put("/api/v1/vehicles/{id}/activate", vehicleId)
            .then()
            .statusCode(200);

        // When: Get active vehicle by driver
        var response = given()
            .when()
            .get("/api/v1/vehicles/driver/{driverId}/active", driverId);
        
        if (response.statusCode() != 200) {
            System.out.println("GET ACTIVE - UNEXPECTED STATUS: " + response.statusCode());
            System.out.println("RESPONSE: " + response.body().asString());
        }
        
        // Then: Should return the active vehicle
        response.then()
            .statusCode(200)
            .body("id", equalTo(vehicleId))
            .body("driverId", equalTo(driverId))
            .body("status", equalTo("ACTIVE"));
    }

    @Test
    @DisplayName("GET active vehicle: No active vehicle returns 404")
    void shouldReturn404WhenNoActiveVehicle() {
        // Given: Create a driver with no active vehicles
        String driverId = createDriver("no.active@example.com", "52998224725");
        String vehicleId = createVehicle("NAV1D23");
        
        // Assign but don't activate
        given()
            .contentType(ContentType.JSON)
            .body(new AssignVehiclePayload(driverId))
            .when()
            .put("/api/v1/vehicles/{id}/assign", vehicleId)
            .then()
            .statusCode(200);

        // When: Get active vehicle
        // Then: Should return 404
        given()
            .when()
            .get("/api/v1/vehicles/driver/{driverId}/active", driverId)
            .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Validation Error: Missing driver ID in assign request")
    void shouldRejectMissingDriverId() {
        // Given: Create a vehicle
        String vehicleId = createVehicle("VAL1E23");

        // When: Try to assign without driver ID
        String payload = "{}";
        
        // Then: Should return 400
        given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when()
            .put("/api/v1/vehicles/{id}/assign", vehicleId)
            .then()
            .statusCode(400)
            .body("validationErrors", notNullValue());
    }

    @Test
    @DisplayName("Business Rule: Assign to non-existent vehicle returns 404")
    void shouldReturn404WhenAssigningNonExistentVehicle() {
        // Given: Create a driver
        String driverId = createDriver("nonexistent.vehicle@example.com", "52998224725");

        // When: Try to assign non-existent vehicle
        var response = given()
            .contentType(ContentType.JSON)
            .body(new AssignVehiclePayload(driverId))
            .when()
            .put("/api/v1/vehicles/{id}/assign", UUID.randomUUID());
        
        if (response.statusCode() != 404) {
            System.out.println("NONEXISTENT VEHICLE - STATUS: " + response.statusCode());
            System.out.println("RESPONSE: " + response.body().asString());
        }
        
        // Then: Should return 404
        response.then().statusCode(404);
    }

    @Test
    @DisplayName("Business Rule: Activate non-existent vehicle returns 404")
    void shouldReturn404WhenActivatingNonExistentVehicle() {
        // When: Try to activate non-existent vehicle
        var response = given()
            .when()
            .put("/api/v1/vehicles/{id}/activate", UUID.randomUUID());
        
        if (response.statusCode() != 404) {
            System.out.println("ACTIVATE NONEXISTENT - STATUS: " + response.statusCode());
            System.out.println("RESPONSE: " + response.body().asString());
        }
        
        // Then: Should return 404
        response.then().statusCode(404);
    }

    @Test
    @DisplayName("Business Rule: Get vehicles for driver with no vehicles returns empty list")
    void shouldReturnEmptyListForDriverWithNoVehicles() {
        // Given: Create a driver with no vehicles
        String driverId = createDriver("no.vehicles@example.com", "52998224725");

        // When: Get vehicles by driver
        // Then: Should return empty list
        given()
            .when()
            .get("/api/v1/vehicles/driver/{driverId}", driverId)
            .then()
            .statusCode(200)
            .body("size()", equalTo(0));
    }

    // ==================== Helper Methods ====================
    
    private String createDriver(String email, String cpf) {
        var payload = new CreateDriverPayload(
            tenantId.toString(),
            "Test Driver",
            email,
            cpf,
            "+5511987654321",
            new DriverLicensePayload(
                "96580714537",
                "B",
                "2020-01-01",
                "2030-01-01",
                true
            )
        );

        var response = given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when()
            .post("/api/v1/drivers");
        
        if (response.statusCode() != 201) {
            System.out.println("CREATE DRIVER FAILED: " + response.statusCode());
            System.out.println("RESPONSE: " + response.body().asString());
        }
        
        return response.then()
            .statusCode(201)
            .extract()
            .path("id");
    }

    private String createVehicle(String licensePlate) {
        var payload = new CreateVehiclePayload(
            tenantId.toString(),
            licensePlate,
            "Toyota",
            "Corolla",
            2023,
            "Prata",
            "SEDAN",
            4,
            5
        );

        var response = given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when()
            .post("/api/v1/vehicles");
        
        if (response.statusCode() != 201) {
            System.out.println("CREATE VEHICLE FAILED: " + response.statusCode());
            System.out.println("RESPONSE: " + response.body().asString());
        }
        
        return response.then()
            .statusCode(201)
            .extract()
            .path("id");
    }

    // ==================== Helper DTOs ====================
    
    public record AssignVehiclePayload(String driverId) {}
    
    public record CreateDriverPayload(
        String tenantId,
        String fullName,
        String email,
        String cpf,
        String phone,
        DriverLicensePayload driverLicense
    ) {}

    public record DriverLicensePayload(
        String number,
        String category,
        String issueDate,
        String expiryDate,
        Boolean isDefinitive
    ) {}

    public record CreateVehiclePayload(
        String tenantId,
        String licensePlate,
        String brand,
        String model,
        Integer year,
        String color,
        String type,
        Integer numberOfDoors,
        Integer seats
    ) {}
}
