package com.transportista.despacho.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/guias/*/descargar")
                        .access((authentication, context) -> {
                            var jwt = ((org.springframework.security.oauth2.jwt.Jwt) ((org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken) authentication
                                    .get()).getToken());
                            String rol = jwt.getClaimAsString("extension_RolConsulta");
                            return new org.springframework.security.authorization.AuthorizationDecision(
                                    "true".equals(rol));
                        })
                        .anyRequest()
                        .access((authentication, context) -> {
                            var jwt = ((org.springframework.security.oauth2.jwt.Jwt) ((org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken) authentication
                                    .get()).getToken());
                            String rol = jwt.getClaimAsString("extension_RolAdmin");
                            return new org.springframework.security.authorization.AuthorizationDecision(
                                    "true".equals(rol));
                        }))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {
                        }));
        return http.build();
    }
}