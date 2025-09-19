package com.sciarticles.manager.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewerService {

    private final WebClient webClient;


    // 1. Nadaj rolę reviewer użytkownikowi po jego ID
    public Mono<Void> addReviewerRoleToUser(UUID userId) {
        return webClient.patch()
                .uri(uriBuilder -> uriBuilder.path("/users").queryParam("id", "eq." + userId.toString()).build())
                .bodyValue(Map.of("role", "reviewer"))
                .retrieve()
                .bodyToMono(Void.class);
    }

    // 2. Przypisz reviewera do artykułu
    public Mono<Void> assignReviewerToArticle(UUID articleId, UUID reviewerId) {
        Map<String, Object> body = Map.of(
                "article_id", articleId.toString(),
                "reviewer_id", reviewerId.toString(),
                "status", "assigned"
        );

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/review_assignments")
                        .build())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class);
    }
    // Metoda do dodawania recenzji
    public Mono<Void> assignReview(UUID articleId, String reviewerEmail, String content) {
        Map<String, Object> body = Map.of(
                "article_id", articleId.toString(),
                "reviewer_email", reviewerEmail,
                "content", content,
                "status", "submitted"
        );

        return webClient.post()
                .uri("/reviews") // endpoint w Supabase lub własnej tabeli recenzji
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class);
    }
}

