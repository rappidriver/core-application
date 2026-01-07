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
 * End-to-End tests for Passenger Registration API.
 * 
 * This test class validates the complete passenger registration flow from HTTP request
 * to database persistence using RestAssured for API testing.
 * 
 * Prerequisites:
 *  - Docker containers must be running: docker-compose up -d
 *  - PostgreSQL 16 + PostGIS 3.4 available at localhost:5432
 *  - Application running on random port via @SpringBootTest
 * 
 * Scenarios tested:
 * 1. Happy path: successful passenger creation
 * 2. Validation errors: invalid input data (email, phone, name)
 * 3. Business rules: duplicate email handling
 * 4. GET operations: retrieve by ID, 404 cases
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@DisplayName("Passenger Registration E2E Tests")
class PassengerRegistrationE2ETest {

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
        jdbcTemplate.update("DELETE FROM passengers");
        jdbcTemplate.update("DELETE FROM tenants");
    }

    @Test
    @DisplayName("Happy Path: Create passenger with valid data")
    void shouldCreatePassengerSuccessfully() {
        // Given: Valid passenger creation payload
        var createPassengerPayload = new CreatePassengerPayload(
            tenantId.toString(),
            "Maria Santos da Silva",
            "maria.santos@example.com",
            "+5511987654321"
        );

        // When & Then: POST request should return 201 with passenger data
        var response = given()
            .contentType(ContentType.JSON)
            .body(createPassengerPayload)
            .when()
            .post("/api/v1/passengers");
        
        if (response.statusCode() != 201) {
            System.out.println("UNEXPECTED STATUS: " + response.statusCode());
            System.out.println("RESPONSE BODY: " + response.body().asString());
        }
        
        response.then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("fullName", equalTo("Maria Santos da Silva"))
            .body("email", equalTo("maria.santos@example.com"))
            .body("phone", notNullValue())
            .body("status", equalTo("ACTIVE"));
    }

    @Test
    @DisplayName("Validation Error: Invalid email format")
    void shouldRejectInvalidEmail() {
        // Given: Payload with invalid email
        var createPassengerPayload = new CreatePassengerPayload(
            tenantId.toString(),
            "Maria Santos",
            "invalid-email",  // Invalid: not an email
            "+5511987654321"
        );

        // When & Then: POST request should return 400
        given()
            .contentType(ContentType.JSON)
            .body(createPassengerPayload)
            .when()
            .post("/api/v1/passengers")
            .then()
            .statusCode(400)
            .body("validationErrors", notNullValue());
    }

    @Test
    @DisplayName("Validation Error: Invalid phone format")
    void shouldRejectInvalidPhone() {
        // Given: Payload with invalid phone (not E.164 format)
        var createPassengerPayload = new CreatePassengerPayload(
            tenantId.toString(),
            "Maria Santos",
            "maria@example.com",
            "abc-invalid"  // Invalid: not numeric, not E.164 format
        );

        // When & Then: POST request should return 400
        var response = given()
            .contentType(ContentType.JSON)
            .body(createPassengerPayload)
            .when()
            .post("/api/v1/passengers");
        
        if (response.statusCode() != 400) {
            System.out.println("PHONE VALIDATION - UNEXPECTED STATUS: " + response.statusCode());
            System.out.println("RESPONSE: " + response.body().asString());
        }
        
        response.then()
            .statusCode(400)
            .body("validationErrors", notNullValue());
    }

    @Test
    @DisplayName("Validation Error: Missing required name field")
    void shouldRejectMissingName() {
        // Given: Payload without fullName
        String payload = """
            {
                "tenantId": "%s",
                "email": "test@example.com",
                "phone": "+5511987654321"
            }
            """.formatted(tenantId);

        // When & Then: POST request should return 400
        given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when()
            .post("/api/v1/passengers")
            .then()
            .statusCode(400)
            .body("validationErrors", notNullValue());
    }

    @Test
    @DisplayName("Validation Error: Name too short")
    void shouldRejectNameTooShort() {
        // Given: Payload with name less than 3 characters
        var createPassengerPayload = new CreatePassengerPayload(
            tenantId.toString(),
            "Ma",  // Too short (min 3)
            "maria@example.com",
            "+5511987654321"
        );

        // When & Then: POST request should return 400
        given()
            .contentType(ContentType.JSON)
            .body(createPassengerPayload)
            .when()
            .post("/api/v1/passengers")
            .then()
            .statusCode(400)
            .body("validationErrors", notNullValue());
    }

    @Test
    @DisplayName("Business Rule: Duplicate email rejection")
    void shouldRejectDuplicateEmail() {
        String sharedEmail = "duplicate@example.com";

        // Given: First passenger successfully created
        var firstPayload = new CreatePassengerPayload(
            tenantId.toString(),
            "Passenger One",
            sharedEmail,
            "+5511987654321"
        );

        var firstResponse = given()
            .contentType(ContentType.JSON)
            .body(firstPayload)
            .when()
            .post("/api/v1/passengers");
        
        if (firstResponse.statusCode() != 201) {
            System.out.println("DUPLICATE EMAIL - FIRST PASSENGER FAILED: " + firstResponse.statusCode());
            System.out.println("RESPONSE: " + firstResponse.body().asString());
        }
        
        firstResponse.then().statusCode(201);

        // When: Try to create second passenger with same email
        var secondPayload = new CreatePassengerPayload(
            tenantId.toString(),
            "Passenger Two",
            sharedEmail,  // Same email
            "+5511987654322"
        );

        // Then: Should return HTTP 400 (duplicate detected)
        var secondResponse = given()
            .contentType(ContentType.JSON)
            .body(secondPayload)
            .when()
            .post("/api/v1/passengers");
        
        if (secondResponse.statusCode() != 400 && secondResponse.statusCode() != 409) {
            System.out.println("DUPLICATE EMAIL - SECOND PASSENGER STATUS: " + secondResponse.statusCode());
            System.out.println("RESPONSE: " + secondResponse.body().asString());
        }
        
        secondResponse.then()
            .statusCode(anyOf(equalTo(400), equalTo(409)))
            .body("message", containsString("already exists"));
    }

    @Test
    @DisplayName("GET endpoint: Retrieve created passenger by ID")
    void shouldRetrievePassengerById() {
        // Given: Create a passenger first
        var createPayload = new CreatePassengerPayload(
            tenantId.toString(),
            "Retrieve Test Passenger",
            "retrieve@example.com",
            "+5511987654321"
        );

        var createResponse = given()
            .contentType(ContentType.JSON)
            .body(createPayload)
            .when()
            .post("/api/v1/passengers");
        
        if (createResponse.statusCode() != 201) {
            System.out.println("GET BY ID - CREATE FAILED: " + createResponse.statusCode());
            System.out.println("RESPONSE: " + createResponse.body().asString());
        }
        
        String passengerId = createResponse.then()
            .statusCode(201)
            .extract()
            .path("id");

        // When: GET passenger by ID
        // Then: Should return 200 with passenger data
        given()
            .when()
            .get("/api/v1/passengers/{id}", passengerId)
            .then()
            .statusCode(200)
            .body("id", equalTo(passengerId))
            .body("fullName", equalTo("Retrieve Test Passenger"))
            .body("email", equalTo("retrieve@example.com"))
            .body("status", equalTo("ACTIVE"));
    }

    @Test
    @DisplayName("GET endpoint: Non-existent passenger returns 404")
    void shouldReturn404ForNonExistentPassenger() {
        // When: GET non-existent passenger
        var response = given()
            .when()
            .get("/api/v1/passengers/{id}", UUID.randomUUID());
        
        if (response.statusCode() != 404) {
            System.out.println("404 TEST - UNEXPECTED STATUS: " + response.statusCode());
            System.out.println("RESPONSE: " + response.body().asString());
        }
        
        // Then: Should return 404
        response.then().statusCode(404);
    }

    // Helper DTO for JSON serialization
    public record CreatePassengerPayload(
        String tenantId,
        String fullName,
        String email,
        String phone
    ) {}
}
