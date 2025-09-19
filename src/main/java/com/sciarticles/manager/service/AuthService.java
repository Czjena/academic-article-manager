package com.sciarticles.manager.service;

import com.sciarticles.manager.model.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final WebClient webClient;

    @Value("${auth.url}")
    private String supabaseUrl;
    @Value("${auth.url-login}")
    private String loginUrl;

    @Value("${supabase.api-key}")
    private String supabaseApiKey;

    /**
     * Rejestracja użytkownika przez Supabase Auth API.
     */
    public Mono<Void> register(UserRequest request) {
        return webClient.post()
                .uri(supabaseUrl)
                .header("apikey", supabaseApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class);
    }

    /**
     * Logowanie użytkownika przez Supabase Auth API - zwraca token JWT.
     */
    public Mono<String> login(UserRequest request) {
        return webClient.post()
                .uri(loginUrl)
                .header("apikey", supabaseApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(LoginResponse.class)
                .map(resp ->  resp.access_token);
    }

    /**
     * Klasa do deserializacji odpowiedzi z Supabase Auth po logowaniu.
     */
    private static class LoginResponse {
        public String access_token;
    }
}