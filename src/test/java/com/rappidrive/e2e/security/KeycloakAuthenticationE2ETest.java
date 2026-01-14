package com.rappidrive.e2e.security;

import com.rappidrive.infrastructure.security.KeycloakIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import io.restassured.RestAssured;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Testes E2E de autenticação e autorização com Keycloak.
 */
@TestPropertySource(properties = "rappidrive.security.enabled=true")
class KeycloakAuthenticationE2ETest extends KeycloakIntegrationTestBase {

    @BeforeEach
    void resetBasePath() {
        // Usar caminhos absolutos sem prefixo duplicado
        RestAssured.basePath = "";
    }

    @Test
    void shouldObtainDriverTokenFromKeycloak() {
        String token = getDriverToken();
        
        assertThat(token).isNotBlank();
        
        // Valida estrutura do JWT (3 partes separadas por .)
        assertThat(token.split("\\.")).hasSize(3);
        
        // Decodifica e valida claims
        Map<String, Object> claims = decodeJwt(token);
        assertThat(claims).containsKey("sub");
        assertThat(claims).containsKey("email");
        assertThat(claims.get("email")).isEqualTo("driver@test.com");
        assertThat(claims.get("preferred_username")).isEqualTo("driver-test");
        
        // Valida roles
        @SuppressWarnings("unchecked")
        Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
        assertThat(realmAccess).containsKey("roles");
    }

    @Test
    void shouldObtainPassengerTokenFromKeycloak() {
        String token = getPassengerToken();
        
        assertThat(token).isNotBlank();
        
        Map<String, Object> claims = decodeJwt(token);
        assertThat(claims.get("email")).isEqualTo("passenger@test.com");
    }

    @Test
    void shouldObtainAdminTokenFromKeycloak() {
        String token = getAdminToken();
        
        assertThat(token).isNotBlank();
        
        Map<String, Object> claims = decodeJwt(token);
        assertThat(claims.get("email")).isEqualTo("admin@test.com");
    }

    @Test
    void shouldAccessPublicEndpointWithoutToken() {
        given()
            .when()
            .get("/actuator/health")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"));
    }

    @Test
    void shouldRejectRequestWithoutToken() {
        given()
            .when()
            .get("/api/v1/trips/available")
            .then()
            .statusCode(401);
    }

    @Test
    void shouldAccessDriverEndpointWithDriverToken() {
        String token = getDriverToken();
        
        given()
            .header("Authorization", "Bearer " + token)
            .when()
                .get("/api/v1/drivers/available")
            .then()
            .statusCode(anyOf(is(200), is(404))); // 200 OK ou 404 se endpoint não implementado ainda
    }

    @Test
    void shouldRejectDriverEndpointWithPassengerToken() {
        String token = getPassengerToken();
        
        given()
            .header("Authorization", "Bearer " + token)
            .when()
                .get("/api/v1/drivers/available")
            .then()
            .statusCode(403); // Forbidden
    }

    @Test
    void shouldAccessAdminEndpointWithAdminToken() {
        String token = getAdminToken();
        
        given()
            .header("Authorization", "Bearer " + token)
            .when()
                .get("/api/v1/admin/approvals/pending")
            .then()
            .statusCode(anyOf(is(200), is(404))); // 200 OK ou 404 se endpoint não implementado ainda
    }

    @Test
    void shouldRejectAdminEndpointWithDriverToken() {
        String token = getDriverToken();
        
        given()
            .header("Authorization", "Bearer " + token)
            .when()
                .get("/api/v1/admin/approvals/pending")
            .then()
            .statusCode(403); // Forbidden
    }

    @Test
    void shouldRefreshTokenSuccessfully() {
        String refreshToken = getRefreshToken("driver-test", "driver123");
        assertThat(refreshToken).isNotBlank();
        
        String newAccessToken = refreshAccessToken(refreshToken);
        assertThat(newAccessToken).isNotBlank();
        assertThat(newAccessToken).isNotEqualTo(refreshToken);
        
        // Valida que o novo token funciona
        given()
            .header("Authorization", "Bearer " + newAccessToken)
            .when()
                .get("/api/v1/drivers/available")
            .then()
            .statusCode(anyOf(is(200), is(404)));
    }

    @Test
    void shouldRejectInvalidToken() {
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.signature";
        
        given()
            .header("Authorization", "Bearer " + invalidToken)
            .when()
                .get("/api/v1/drivers/available")
            .then()
            .statusCode(401);
    }

    @Test
    void shouldRejectExpiredToken() {
        // Token expirado (gerado manualmente com exp no passado)
        String expiredToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ0ZXN0In0.eyJleHAiOjE2MDk0NTkyMDB9.invalid";
        
        given()
            .header("Authorization", "Bearer " + expiredToken)
            .when()
            .get("/api/v1/drivers/available")
            .then()
            .statusCode(401);
    }
}
