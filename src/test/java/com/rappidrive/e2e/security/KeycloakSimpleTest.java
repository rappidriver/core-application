package com.rappidrive.e2e.security;

import com.rappidrive.infrastructure.security.KeycloakIntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste simples de integração com Keycloak.
 * 
 * Valida apenas a inicialização do container e obtenção de tokens,
 * sem testar endpoints da aplicação.
 */
class KeycloakSimpleTest extends KeycloakIntegrationTestBase {

    @Test
    void shouldObtainDriverTokenFromKeycloak() {
        String token = getDriverToken();
        
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // JWT format: header.payload.signature
        
        Map<String, Object> claims = decodeJwt(token);
        assertThat(claims).containsKey("sub");
        assertThat(claims).containsKey("email");
        assertThat(claims.get("email")).isEqualTo("driver@test.com");
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
}
