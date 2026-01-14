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
 * End-to-End tests for Trip Management API.
 * 
 * This test class validates the complete trip lifecycle from request to completion,
 * including driver assignment, trip start, and completion.
 * 
 * Prerequisites:
 *  - Docker containers must be running: docker-compose up -d
 *  - PostgreSQL 16 + PostGIS 3.4 available at localhost:5432
 *  - Application running on random port via @SpringBootTest
 * 
 * Trip Lifecycle:
 *  REQUESTED -> DRIVER_ASSIGNED -> IN_PROGRESS -> COMPLETED
 * 
 * Scenarios tested:
 * 1. Happy path: complete trip lifecycle
 * 2. Create trip with valid data
 * 3. Assign driver to trip
 * 4. Start trip
 * 5. Complete trip
 * 6. Validation errors
 * 7. Business rules: invalid state transitions
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@DisplayName("Trip Management E2E Tests")
class TripE2ETest {

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
        
        // Clean up any existing test data first (order matters due to FK constraints)
        jdbcTemplate.update("DELETE FROM trips");
        jdbcTemplate.update("DELETE FROM vehicles");
        jdbcTemplate.update("DELETE FROM drivers");
        jdbcTemplate.update("DELETE FROM passengers");
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
        jdbcTemplate.update("DELETE FROM trips");
        jdbcTemplate.update("DELETE FROM vehicles");
        jdbcTemplate.update("DELETE FROM drivers");
        jdbcTemplate.update("DELETE FROM passengers");
        jdbcTemplate.update("DELETE FROM tenants");
    }

    @Test
    @DisplayName("Happy Path: Complete trip lifecycle - Request -> Assign -> Start -> Complete")
    void shouldCompleteFullTripLifecycle() {
        // Given: Create a passenger and a driver with active vehicle
        String passengerId = createPassenger("lifecycle@example.com");
        String driverId = createDriverWithActiveVehicle("driver.lifecycle@example.com", "LFC1A23");

        String tripId = given()
            .contentType(ContentType.JSON)
            .body(createTripPayload(passengerId))
            .when()
            .post("/api/v1/trips")
            .then()
            .statusCode(201)
            .body("status", equalTo("REQUESTED"))
            .extract()
            .path("id");

        given()
            .contentType(ContentType.JSON)
            .body(new AssignDriverPayload(driverId))
            .when()
            .put("/api/v1/trips/{id}/assign-driver", tripId)
            .then()
            .statusCode(200)
            .body("status", equalTo("DRIVER_ASSIGNED"))
            .body("driverId", equalTo(driverId));

        given()
            .when()
            .put("/api/v1/trips/{id}/start", tripId)
            .then()
            .statusCode(200)
            .body("status", equalTo("IN_PROGRESS"));

        given()
            .when()
            .put("/api/v1/trips/{id}/complete", tripId)
            .then()
            .statusCode(200)
            .body("status", equalTo("COMPLETED"));
    }

    @Test
    @DisplayName("Happy Path: Create trip with valid data")
    void shouldCreateTripSuccessfully() {
        // Given: Create a passenger
        String passengerId = createPassenger("trip.create@example.com");

        // When & Then: POST request should return 201 with trip data
        var response = given()
            .contentType(ContentType.JSON)
            .body(createTripPayload(passengerId))
            .when()
            .post("/api/v1/trips");
        
        if (response.statusCode() != 201) {
            System.out.println("CREATE TRIP - UNEXPECTED STATUS: " + response.statusCode());
            System.out.println("RESPONSE: " + response.body().asString());
        }
        
        response.then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("passengerId", equalTo(passengerId))
            .body("status", equalTo("REQUESTED"))
            .body("origin.latitude", equalTo(-23.55052f))
            .body("origin.longitude", equalTo(-46.633308f))
            .body("destination.latitude", equalTo(-23.561414f))
            .body("destination.longitude", equalTo(-46.656056f));
    }

    @Test
    @DisplayName("GET endpoint: Retrieve trip by ID")
    void shouldRetrieveTripById() {
        // Given: Create a passenger and trip
        String passengerId = createPassenger("get.trip@example.com");
        
        String tripId = given()
            .contentType(ContentType.JSON)
            .body(createTripPayload(passengerId))
            .when()
            .post("/api/v1/trips")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // When: GET trip by ID
        // Then: Should return 200 with trip data
        given()
            .when()
            .get("/api/v1/trips/{id}", tripId)
            .then()
            .statusCode(200)
            .body("id", equalTo(tripId))
            .body("passengerId", equalTo(passengerId))
            .body("status", equalTo("REQUESTED"));
    }

    @Test
    @DisplayName("GET endpoint: Non-existent trip returns 404")
    void shouldReturn404ForNonExistentTrip() {
        var response = given()
            .when()
            .get("/api/v1/trips/{id}", UUID.randomUUID());
        
        if (response.statusCode() != 404) {
            System.out.println("404 TEST - UNEXPECTED STATUS: " + response.statusCode());
            System.out.println("RESPONSE: " + response.body().asString());
        }
        
        response.then().statusCode(404);
    }

    @Test
    @DisplayName("Validation Error: Missing passenger ID")
    void shouldRejectMissingPassengerId() {
        String payload = """
            {
                "tenantId": "%s",
                "origin": {"latitude": -23.55052, "longitude": -46.633308},
                "destination": {"latitude": -23.561414, "longitude": -46.656056}
            }
            """.formatted(tenantId);

        given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when()
            .post("/api/v1/trips")
            .then()
            .statusCode(400)
            .body("validationErrors", notNullValue());
    }

    @Test
    @DisplayName("Validation Error: Invalid latitude (out of range)")
    void shouldRejectInvalidLatitude() {
        String passengerId = createPassenger("invalid.lat@example.com");
        
        String payload = """
            {
                "tenantId": "%s",
                "passengerId": "%s",
                "origin": {"latitude": -100.0, "longitude": -46.633308},
                "destination": {"latitude": -23.561414, "longitude": -46.656056}
            }
            """.formatted(tenantId, passengerId);

        given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when()
            .post("/api/v1/trips")
            .then()
            .statusCode(400)
            .body("validationErrors", notNullValue());
    }

    @Test
    @DisplayName("Business Rule: Cannot start trip without driver assigned")
    void shouldRejectStartWithoutDriverAssigned() {
        // Given: Create a trip without assigning driver
        String passengerId = createPassenger("no.driver@example.com");
        
        String tripId = given()
            .contentType(ContentType.JSON)
            .body(createTripPayload(passengerId))
            .when()
            .post("/api/v1/trips")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // When: Try to start trip without driver
        var response = given()
            .when()
            .put("/api/v1/trips/{id}/start", tripId);
        
        if (response.statusCode() != 400) {
            System.out.println("START WITHOUT DRIVER - STATUS: " + response.statusCode());
            System.out.println("RESPONSE: " + response.body().asString());
        }
        
        // Then: Should return 400
        response.then().statusCode(400);
    }

    @Test
    @DisplayName("Business Rule: Cannot complete trip not in progress")
    void shouldRejectCompleteWhenNotInProgress() {
        // Given: Create a trip and assign driver (but don't start)
        String passengerId = createPassenger("not.started@example.com");
        String driverId = createDriverWithActiveVehicle("driver.notstarted@example.com", "NST1A23");
        
        String tripId = given()
            .contentType(ContentType.JSON)
            .body(createTripPayload(passengerId))
            .when()
            .post("/api/v1/trips")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        given()
            .contentType(ContentType.JSON)
            .body(new AssignDriverPayload(driverId))
            .when()
            .put("/api/v1/trips/{id}/assign-driver", tripId)
            .then()
            .statusCode(200);

        // When: Try to complete trip without starting
        var response = given()
            .when()
            .put("/api/v1/trips/{id}/complete", tripId);
        
        if (response.statusCode() != 400) {
            System.out.println("COMPLETE NOT STARTED - STATUS: " + response.statusCode());
            System.out.println("RESPONSE: " + response.body().asString());
        }
        
        // Then: Should return 400
        response.then().statusCode(400);
    }

    @Test
    @DisplayName("Business Rule: Assign driver to non-existent trip returns 404")
    void shouldReturn404WhenAssigningToNonExistentTrip() {
        String driverId = createDriverWithActiveVehicle("driver.notrip@example.com", "NTR1A23");

        var response = given()
            .contentType(ContentType.JSON)
            .body(new AssignDriverPayload(driverId))
            .when()
            .put("/api/v1/trips/{id}/assign-driver", UUID.randomUUID());
        
        if (response.statusCode() != 404) {
            System.out.println("ASSIGN NONEXISTENT - STATUS: " + response.statusCode());
            System.out.println("RESPONSE: " + response.body().asString());
        }
        
        response.then().statusCode(404);
    }

    // ==================== Helper Methods ====================

    private String createPassenger(String email) {
        var payload = new CreatePassengerPayload(
            tenantId.toString(),
            "Test Passenger",
            email,
            "+5511987654321"
        );

        var response = given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when()
            .post("/api/v1/passengers");
        
        if (response.statusCode() != 201) {
            System.out.println("CREATE PASSENGER FAILED: " + response.statusCode());
            System.out.println("RESPONSE: " + response.body().asString());
        }
        
        return response.then()
            .statusCode(201)
            .extract()
            .path("id");
    }

    private String createDriverWithActiveVehicle(String email, String licensePlate) {
        // Create driver
        var driverPayload = new CreateDriverPayload(
            tenantId.toString(),
            "Test Driver",
            email,
            "52998224725",  // Valid CPF
            "+5511987654322",
            new DriverLicensePayload("96580714537", "B", "2020-01-01", "2030-01-01", true)
        );

        var driverResponse = given()
            .contentType(ContentType.JSON)
            .body(driverPayload)
            .when()
            .post("/api/v1/drivers");
        
        if (driverResponse.statusCode() != 201) {
            System.out.println("CREATE DRIVER FAILED: " + driverResponse.statusCode());
            System.out.println("RESPONSE: " + driverResponse.body().asString());
        }
        
        String driverId = driverResponse.then()
            .statusCode(201)
            .extract()
            .path("id");

        // Create vehicle
        var vehiclePayload = new CreateVehiclePayload(
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

        var vehicleResponse = given()
            .contentType(ContentType.JSON)
            .body(vehiclePayload)
            .when()
            .post("/api/v1/vehicles");
        
        if (vehicleResponse.statusCode() != 201) {
            System.out.println("CREATE VEHICLE FAILED: " + vehicleResponse.statusCode());
            System.out.println("RESPONSE: " + vehicleResponse.body().asString());
        }
        
        String vehicleId = vehicleResponse.then()
            .statusCode(201)
            .extract()
            .path("id");

        // Assign vehicle to driver
        given()
            .contentType(ContentType.JSON)
            .body(new AssignVehiclePayload(driverId))
            .when()
            .put("/api/v1/vehicles/{id}/assign", vehicleId)
            .then()
            .statusCode(200);

        // Activate vehicle
        given()
            .when()
            .put("/api/v1/vehicles/{id}/activate", vehicleId)
            .then()
            .statusCode(200);

        // Activate driver (changes status from PENDING_APPROVAL to ACTIVE)
        var activateResponse = given()
            .when()
            .put("/api/v1/drivers/{id}/activate", driverId);
        
        activateResponse.then().statusCode(200);

        // Update driver location (required to be available for rides)
        var locationPayload = new UpdateLocationPayload(new LocationPayload(-23.55052, -46.633308));
        var locationResponse = given()
            .contentType(ContentType.JSON)
            .body(locationPayload)
            .when()
            .put("/api/v1/drivers/{id}/location", driverId);
        
        locationResponse.then().statusCode(200);

        return driverId;
    }

    private CreateTripPayload createTripPayload(String passengerId) {
        return new CreateTripPayload(
            tenantId.toString(),
            passengerId,
            new LocationPayload(-23.55052, -46.633308),   // São Paulo - Praça da Sé
            new LocationPayload(-23.561414, -46.656056)   // São Paulo - Av Paulista
        );
    }

    // ==================== Helper DTOs ====================

    public record CreateTripPayload(
        String tenantId,
        String passengerId,
        LocationPayload origin,
        LocationPayload destination
    ) {}

    public record LocationPayload(Double latitude, Double longitude) {}
    
    public record AssignDriverPayload(String driverId) {}
    
    public record AssignVehiclePayload(String driverId) {}

    public record CreatePassengerPayload(
        String tenantId,
        String fullName,
        String email,
        String phone
    ) {}

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

    public record UpdateLocationPayload(LocationPayload location) {}
}
