package com.rappidrive.application.ports.output;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port para obter informações do usuário autenticado no contexto atual.
 * 
 * Esta interface abstrai os detalhes de autenticação (Spring Security, JWT, Keycloak)
 * da camada de aplicação, permitindo que use cases obtenham informações do usuário
 * sem depender de frameworks específicos.
 * 
 * HIST-2026-014: Atualizado para incluir email do Keycloak
 */
public interface CurrentUserPort {

    Optional<CurrentUser> getCurrentUser();

    /**
     * Representa o usuário autenticado no contexto atual.
     * 
     * @param userId UUID do usuário (sub claim do Keycloak)
     * @param username Username preferido (preferred_username claim)
     * @param email Email do usuário (email claim do OIDC)
     * @param roles Roles sem prefixo (ex: ["ADMIN", "DRIVER"])
     * @param scopes Scopes OAuth2 sem prefixo (ex: ["openid", "profile", "email"])
     */
    record CurrentUser(
        UUID userId,
        String username,
        String email,
        List<String> roles,
        List<String> scopes
    ) {
        /**
         * Verifica se o usuário tem uma role específica.
         */
        public boolean hasRole(String role) {
            return roles != null && roles.contains(role.toUpperCase());
        }

        /**
         * Verifica se o usuário tem pelo menos uma das roles especificadas.
         */
        public boolean hasAnyRole(String... roles) {
            if (this.roles == null) {
                return false;
            }
            for (String role : roles) {
                if (this.roles.contains(role.toUpperCase())) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Verifica se o usuário tem um scope específico.
         */
        public boolean hasScope(String scope) {
            return scopes != null && scopes.contains(scope.toLowerCase());
        }
    }
}
