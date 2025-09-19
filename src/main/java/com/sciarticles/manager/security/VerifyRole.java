package com.sciarticles.manager.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Arrays;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Component
@Service
public class VerifyRole {

    public Mono<ResponseEntity<Object>> checkRoles(Jwt jwt, String... allowedRoles) {
        if (jwt == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized")));
        }

        String role = jwt.getClaimAsString("role");

        if (role == null || Arrays.stream(allowedRoles).noneMatch(r -> r.equalsIgnoreCase(role))) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Forbidden: insufficient permissions")));
        }

        return Mono.empty(); // brak błędów
    }

}
