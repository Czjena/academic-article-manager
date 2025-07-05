package com.sciarticles.manager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssignReviewerService {

    private final WebClient webClient;

    public Mono<Void> assignReviewer(UUID articleId, UUID reviewerId, String serviceRoleKey) {
        // Payload do przypisania
        Map<String, Object> body = Map.of(
                "article_id", articleId.toString(),
                "reviewer_id", reviewerId.toString(),
                "status", "assigned"
        );

        return webClient.post()
                .uri("review_assignments")
                .header("apikey", serviceRoleKey)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
