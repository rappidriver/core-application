package com.rappidrive.infrastructure.security.keycloak;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Conversor customizado para extrair roles do Keycloak JWT e transformá-las em authorities do Spring Security.
 * 
 * Keycloak retorna roles em dois lugares:
 * 1. realm_access.roles - Roles globais do realm
 * 2. resource_access.{client-id}.roles - Roles específicas do client
 * 
 * Este conversor extrai ambas, adiciona o prefixo ROLE_ e as converte para SimpleGrantedAuthority.
 */
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String RESOURCE_ACCESS_CLAIM = "resource_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String ROLE_PREFIX = "ROLE_";
    
    private final JwtGrantedAuthoritiesConverter defaultGrantedAuthoritiesConverter;
    private final String resourceId;

    /**
     * Cria o conversor com o resourceId (client-id) do Keycloak.
     * 
     * @param resourceId o client-id configurado no Keycloak (ex: "rappidrive-api")
     */
    public KeycloakJwtAuthenticationConverter(String resourceId) {
        this.resourceId = resourceId;
        this.defaultGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = Stream.concat(
            // Extrai scopes padrão (SCOPE_openid, SCOPE_profile, etc.)
            defaultGrantedAuthoritiesConverter.convert(jwt).stream(),
            // Extrai roles do Keycloak (realm + resource)
            extractKeycloakRoles(jwt).stream()
        ).collect(Collectors.toSet());

        return new JwtAuthenticationToken(jwt, authorities);
    }

    /**
     * Extrai roles do Keycloak de realm_access e resource_access.
     */
    private Collection<GrantedAuthority> extractKeycloakRoles(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        authorities.addAll(extractRealmRoles(jwt));
        
        authorities.addAll(extractResourceRoles(jwt));
        
        return authorities;
    }

    /**
     * Extrai roles de realm_access.roles
     */
    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim(REALM_ACCESS_CLAIM);
        if (realmAccess == null || !realmAccess.containsKey(ROLES_CLAIM)) {
            return Collections.emptyList();
        }
        
        Object rolesObj = realmAccess.get(ROLES_CLAIM);
        if (!(rolesObj instanceof Collection)) {
            return Collections.emptyList();
        }
        
        Collection<String> roles = (Collection<String>) rolesObj;
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role.toUpperCase()))
            .collect(Collectors.toList());
    }

    /**
     * Extrai roles de resource_access.{resourceId}.roles
     */
    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim(RESOURCE_ACCESS_CLAIM);
        if (resourceAccess == null || !resourceAccess.containsKey(resourceId)) {
            return Collections.emptyList();
        }
        
        Object clientObj = resourceAccess.get(resourceId);
        if (!(clientObj instanceof Map)) {
            return Collections.emptyList();
        }
        
        Map<String, Object> client = (Map<String, Object>) clientObj;
        if (!client.containsKey(ROLES_CLAIM)) {
            return Collections.emptyList();
        }
        
        Object rolesObj = client.get(ROLES_CLAIM);
        if (!(rolesObj instanceof Collection)) {
            return Collections.emptyList();
        }
        
        Collection<String> roles = (Collection<String>) rolesObj;
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role.toUpperCase()))
            .collect(Collectors.toList());
    }
}
