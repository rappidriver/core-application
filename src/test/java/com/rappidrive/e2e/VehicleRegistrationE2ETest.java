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
 * End-to-End tests for Vehicle Registration API.
 * 
 * This test class validates the complete vehicle registration flow from HTTP request
 * to database persistence using RestAssured for API testing.
 * 
 * Prerequisites:
 *  - Docker containers must be running: docker-compose up -d
 *  - PostgreSQL 16 + PostGIS 3.4 available at localhost:5432
 *  - Application running on random port via @SpringBootTest
 * 
 * Scenarios tested:
 * 1. Happy path: successful vehicle creation
 * 2. Validation errors: invalid input data (license plate, year, etc.)
 * 3. Business rules: duplicate license plate handling
 * 4. GET operations: retrieve by ID, by driver, 404 cases
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@DisplayName("Vehicle Registration E2E Tests")
class VehicleRegistrationE2ETest {

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
    @DisplayName("Happy Path: Create vehicle with valid data")
    void shouldCreateVehicleSuccessfully() {
        // Given: Valid vehicle creation payload
        var createVehiclePayload = new CreateVehiclePayload(
            tenantId.toString(),
            "ABC1D23",      // Valid Mercosul plate format
            "Toyota",
            "Corolla",
            2023,
            "Prata",
            "SEDAN",
            4,
            5
        );

        // When & Then: POST request should return 201 with vehicle data
        var response = given()
            .contentType(ContentType.JSON)
            .body(createVehiclePayload)
            .when()
            .post("/api/v1/vehicles");
        
        if (response.statusCode() != 201) {
            System.out.println("UNEXPECTED STATUS: " + response.statusCode());
            System.out.println("RESPONSE BODY: " + response.body().asString());
        }
        
        response.then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("licensePlate", equalTo("ABC1D23"))
            .body("brand", equalTo("Toyota"))
            .body("model", equalTo("Corolla"))
            .body("year", equalTo(2023))
            .body("color", equalTo("Prata"))
            .body("type", equalTo("SEDAN"))
            .body("numberOfDoors", equalTo(4))
            .body("seats", equalTo(5))
            .body("status", equalTo("INACTIVE"));  // Vehicles start as INACTIVE
    }

    @Test
    @DisplayName("Happy Path: Create vehicle with old plate format")
    void shouldCreateVehicleWithOldPlateFormat() {
        // Given: Valid vehicle with old Brazilian plate format
        var createVehiclePayload = new CreateVehiclePayload(
            tenantId.toString(),
            "ABC-1234",     // Old Brazilian plate format
            "Honda",
            "Civic",
            2022,
            "Preto",
            "SEDAN",
            4,
            5
        );

        // When & Then: POST request should return 201
        var response = given()
            .contentType(ContentType.JSON)
            .body(createVehiclePayload)
            .when()
            .post("/api/v1/vehicles");
        
        if (response.statusCode() != 201) {
            System.out.println("OLD PLATE - UNEXPECTED STATUS: " + response.statusCode());
            System.out.println("RESPONSE BODY: " + response.body().asString());
        }
        
        response.then()
            .statusCode(201)
            .body("licensePlate", equalTo("ABC-1234"));
    }

    @Test
    @DisplayName("Happy Path: Create SUV vehicle")
    void shouldCreateSUVVehicle() {
        // Given: Valid SUV vehicle
        var createVehiclePayload = new CreateVehiclePayload(
            tenantId.toString(),
            "XYZ5E67",
            "Jeep",
            "Compass",
            2024,
            "Branco",
            "SUV",
            4,
            7
        );

        // When & Then: POST request should return 201 with SUV type
        given()
            .contentType(ContentType.JSON)
            .body(createVehiclePayload)
            .when()
            .post("/api/v1/vehicles")
            .then()
            .statusCode(201)
            .body("type", equalTo("SUV"))
            .body("seats", equalTo(7));
    }

    @Test
    @DisplayName("Validation Error: Invalid license plate format")
    void shouldRejectInvalidLicensePlate() {
        // Given: Payload with invalid license plate
        var createVehiclePayload = new CreateVehiclePayload(
            tenantId.toString(),
            "INVALID",      // Invalid: not matching pattern
            "Toyota",
            "Corolla",
            2023,
            "Prata",
            "SEDAN",
            4,
            5
        );

        // When & Then: POST request should return 400
        given()
            .contentType(ContentType.JSON)
            .body(createVehiclePayload)
            .when()
            .post("/api/v1/vehicles")
            .then()
            .statusCode(400)
            .body("validationErrors", notNullValue());
    }

    @Test
    @DisplayName("Validation Error: Vehicle too old")
    void shouldRejectVehicleTooOld() {
        // Given: Payload with year older than 10 years
        var createVehiclePayload = new CreateVehiclePayload(
            tenantId.toString(),
            "ABC1D23",
            "Toyota",
            "Corolla",
            2010,           // Too old (min is 2015)
            "Prata",
            "SEDAN",
            4,
            5
        );

        // When & Then: POST request should return 400
        given()
            .contentType(ContentType.JSON)
            .body(createVehiclePayload)
            .when()
            .post("/api/v1/vehicles")
            .then()
            .statusCode(400)
            .body("validationErrors", notNullValue());
    }

    @Test
    @DisplayName("Validation Error: Missing required brand")
    void shouldRejectMissingBrand() {
        // Given: Payload without brand
        String payload = """
            {
                "tenantId": "%s",
                "licensePlate": "ABC1D23",
                "model": "Corolla",
                "year": 2023,
                "color": "Prata",
                "type": "SEDAN",
                "numberOfDoors": 4,
                "seats": 5
            }
            """.formatted(tenantId);

        // When & Then: POST request should return 400
        given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when()
            .post("/api/v1/vehicles")
            .then()
            .statusCode(400)
            .body("validationErrors", notNullValue());
    }

    @Test
    @DisplayName("Validation Error: Invalid number of doors")
    void shouldRejectInvalidNumberOfDoors() {
        // Given: Payload with 2 doors (must be exactly 4)
        var createVehiclePayload = new CreateVehiclePayload(
            tenantId.toString(),
            "ABC1D23",
            "Toyota",
            "Corolla",
            2023,
            "Prata",
            "SEDAN",
            2,              // Invalid: must be exactly 4
            5
        );

        // When & Then: POST request should return 400
        given()
            .contentType(ContentType.JSON)
            .body(createVehiclePayload)
            .when()
            .post("/api/v1/vehicles")
            .then()
            .statusCode(400)
            .body("validationErrors", notNullValue());
    }

    @Test
    @DisplayName("Business Rule: Duplicate license plate rejection")
    void shouldRejectDuplicateLicensePlate() {
        String sharedPlate = "DUP1E23";

        // Given: First vehicle successfully created
        var firstPayload = new CreateVehiclePayload(
            tenantId.toString(),
            sharedPlate,
            "Toyota",
            "Corolla",
            2023,
            "Prata",
            "SEDAN",
            4,
            5
        );

        var firstResponse = given()
            .contentType(ContentType.JSON)
            .body(firstPayload)
            .when()
            .post("/api/v1/vehicles");
        
        if (firstResponse.statusCode() != 201) {
            System.out.println("DUPLICATE PLATE - FIRST VEHICLE FAILED: " + firstResponse.statusCode());
            System.out.println("RESPONSE: " + firstResponse.body().asString());
        }
        
        firstResponse.then().statusCode(201);

        // When: Try to create second vehicle with same license plate
        var secondPayload = new CreateVehiclePayload(
            tenantId.toString(),
            sharedPlate,    // Same license plate
            "Honda",
            "Civic",
            2024,
            "Preto",
            "SEDAN",
            4,
            5
        );

        // Then: Should return HTTP 400 (duplicate detected)
        var secondResponse = given()
            .contentType(ContentType.JSON)
            .body(secondPayload)
            .when()
            .post("/api/v1/vehicles");
        
        if (secondResponse.statusCode() != 400 && secondResponse.statusCode() != 409) {
            System.out.println("DUPLICATE PLATE - SECOND VEHICLE STATUS: " + secondResponse.statusCode());
            System.out.println("RESPONSE: " + secondResponse.body().asString());
        }
        
        secondResponse.then()
            .statusCode(anyOf(equalTo(400), equalTo(409)))
            .body("message", containsString("already exists"));
    }

    @Test
    @DisplayName("GET endpoint: Retrieve created vehicle by ID")
    void shouldRetrieveVehicleById() {
        // Given: Create a vehicle first
        var createPayload = new CreateVehiclePayload(
            tenantId.toString(),
            "GET1A23",
            "Volkswagen",
            "Golf",
            2023,
            "Cinza",
            "HATCHBACK",
            4,
            5
        );

        var createResponse = given()
            .contentType(ContentType.JSON)
            .body(createPayload)
            .when()
            .post("/api/v1/vehicles");
        
        if (createResponse.statusCode() != 201) {
            System.out.println("GET BY ID - CREATE FAILED: " + createResponse.statusCode());
            System.out.println("RESPONSE: " + createResponse.body().asString());
        }
        
        String vehicleId = createResponse.then()
            .statusCode(201)
            .extract()
            .path("id");

        // When: GET vehicle by ID
        // Then: Should return 200 with vehicle data
        given()
            .when()
            .get("/api/v1/vehicles/{id}", vehicleId)
            .then()
            .statusCode(200)
            .body("id", equalTo(vehicleId))
            .body("licensePlate", equalTo("GET1A23"))
            .body("brand", equalTo("Volkswagen"))
            .body("model", equalTo("Golf"))
            .body("type", equalTo("HATCHBACK"))
            .body("status", equalTo("INACTIVE"));
    }

    @Test
    @DisplayName("GET endpoint: Non-existent vehicle returns 404")
    void shouldReturn404ForNonExistentVehicle() {
        // When: GET non-existent vehicle
        var response = given()
            .when()
            .get("/api/v1/vehicles/{id}", UUID.randomUUID());
        
        if (response.statusCode() != 404) {
            System.out.println("404 TEST - UNEXPECTED STATUS: " + response.statusCode());
            System.out.println("RESPONSE: " + response.body().asString());
        }
        
        // Then: Should return 404
        response.then().statusCode(404);
    }

    // Helper DTO for JSON serialization
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
