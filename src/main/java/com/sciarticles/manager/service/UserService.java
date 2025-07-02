package com.sciarticles.manager.service;

import com.sciarticles.manager.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final WebClient supabaseWebClient;
    private final WebClient webClient;

    public Mono<String> getUserRoleFromSupabase(String userId) {
        return supabaseWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users")
                        .queryParam("id", "eq." + userId)
                        .build())
                .retrieve()
                .bodyToFlux(UserDto.class)
                .next()
                .map(UserDto::getRole);
    }
    public Mono<UUID> getUserIdByEmail(String email) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users")
                        .queryParam("email", "eq." + email)
                        .build())
                .retrieve()
                .bodyToFlux(UserDto.class)
                .next()
                .map(user -> UUID.fromString(user.getId()));
    }
}
