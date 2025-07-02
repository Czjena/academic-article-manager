package com.sciarticles.manager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciarticles.manager.dto.UserDto;
import com.sciarticles.manager.model.UserRequest;
import com.sciarticles.manager.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final WebClient webClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;


    public Mono<Void> register(UserRequest request) {
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        UserDto newUser = new UserDto(request.getEmail(), hashedPassword, request.getRole());

        return webClient.post()
                .uri("/users")
                .bodyValue(newUser)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return Mono.just("User registered successfully");
                    } else {
                        // Odczytaj ciało błędu z Supabase
                        return response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    // Możesz też zalogować błąd:
                                    System.err.println("Supabase error: " + errorBody);

                                    // Rzuć wyjątek z tekstem błędu (lub zwróć w inny sposób)
                                    return Mono.error(new RuntimeException("Supabase error: " + errorBody));
                                });
                    }
                }).then();
    }
    public Mono<String> login(UserRequest request) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users")
                        .queryParam("email", "eq." + request.getEmail())
                        .build())
                .retrieve()
                .bodyToFlux(UserDto.class)
                .next()
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
                .flatMap(user -> {
                    if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        return Mono.fromCallable(() -> jwtUtil.generateToken(user.getEmail()))
                                .subscribeOn(Schedulers.boundedElastic());
                    } else {
                        return Mono.error(new RuntimeException("Invalid credentials"));
                    }
                });
    }
}
