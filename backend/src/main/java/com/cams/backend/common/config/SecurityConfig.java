package com.cams.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Phase 1 security foundation.
 *
 * <p>Deny-by-default: only the public liveness endpoint ({@code /health},
 * API_ARCHITECTURE.md §4.12) is permitted without authentication. No login,
 * token issuance, or user store is implemented yet — that is the Auth/User
 * module's job per ADR-0006 (authentication) and ADR-0012 (authorization),
 * neither of which is implemented in this Phase 1 skeleton.
 *
 * <p>CSRF is disabled because CAMS is a stateless, token-authenticated JSON
 * API with no browser/cookie-session client (SECURITY_ARCHITECTURE.md) — CSRF
 * protection is a browser/cookie concern and does not apply here.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health").permitAll()
                        .anyRequest().denyAll());
        return http.build();
    }
}
