package com.sciarticles.manager.service;

import com.sciarticles.manager.dto.ReviewAssignmentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssignReviewer {

    private final WebClient supabaseWebClient;

    public Mono<ReviewAssignmentDto> assign(UUID articleId, UUID reviewerId, String jwtToken) {
        Map<String, Object> data = Map.of(
                "article_id", articleId,
                "reviewer_id", reviewerId,
                "status", "pending",
                "assigned_at", LocalDateTime.now().toString()
        );

        return supabaseWebClient.post()
                .uri("/review_assignments")
                .header("Authorization", "Bearer " + jwtToken)   // dodaj nagłówek z tokenem
                .bodyValue(data)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(ReviewAssignmentDto.class);
                    } else {
                        return response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException(
                                        "Supabase error " + response.statusCode() + ": " + body)));
                    }
                });
    }
}
