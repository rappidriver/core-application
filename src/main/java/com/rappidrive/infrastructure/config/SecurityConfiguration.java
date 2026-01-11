package com.rappidrive.infrastructure.config;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@Profile({"!test", "!e2e"})
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
    prefix = "rappidrive.security",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class SecurityConfiguration {

    private static final String ADMIN_SCOPE = "SCOPE_admin";
    private static final String DRIVER_SCOPE = "SCOPE_driver";
    private static final String PASSENGER_SCOPE = "SCOPE_passenger";

    @Value("${security.jwt.secret:change-me-please-change-me-please-change-me-please}")
    private String jwtSecret;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    new AntPathRequestMatcher("/auth/login"),
                    new AntPathRequestMatcher("/swagger-ui/**"),
                    new AntPathRequestMatcher("/v3/api-docs/**"),
                    new AntPathRequestMatcher("/actuator/health")
                ).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/admin/**")).hasAuthority(ADMIN_SCOPE)
                .requestMatchers(new AntPathRequestMatcher("/api/v1/drivers/**"))
                .hasAnyAuthority(DRIVER_SCOPE, ADMIN_SCOPE)
                .requestMatchers(new AntPathRequestMatcher("/api/v1/passengers/**"))
                .hasAnyAuthority(PASSENGER_SCOPE, ADMIN_SCOPE)
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey secretKey = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }
}
