package com.rappidrive.infrastructure.security;

import com.rappidrive.application.ports.output.CurrentUserPort;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@Profile({"!test", "!e2e"})
public class SpringSecurityCurrentUserAdapter implements CurrentUserPort {

    @Override
    public Optional<CurrentUser> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        List<String> authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

        UUID userId = extractUserId(authentication).orElse(null);
        String username = authentication.getName();
        List<String> roles = filterAuthorities(authorities, "ROLE_");
        List<String> scopes = filterAuthorities(authorities, "SCOPE_");

        return Optional.of(new CurrentUser(userId, username, roles, scopes));
    }

    private Optional<UUID> extractUserId(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken token) {
            Object subject = token.getToken().getSubject();
            if (subject != null) {
                return parseUuid(subject.toString());
            }
            Object userIdClaim = token.getToken().getClaims().get("user_id");
            if (userIdClaim != null) {
                return parseUuid(userIdClaim.toString());
            }
        }
        return parseUuid(authentication.getName());
    }

    private Optional<UUID> parseUuid(String value) {
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private List<String> filterAuthorities(Collection<String> authorities, String prefix) {
        return authorities.stream()
            .filter(Objects::nonNull)
            .filter(auth -> auth.startsWith(prefix))
            .map(auth -> auth.substring(prefix.length()))
            .toList();
    }
}
