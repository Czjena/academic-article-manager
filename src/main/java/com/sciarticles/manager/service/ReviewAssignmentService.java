package com.sciarticles.manager.service;

import com.sciarticles.manager.dto.ReviewAssignmentDto;
import com.sciarticles.manager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewAssignmentService {

    private final UserService userService;
    private final WebClient supabaseWebClient;

    public Mono<ReviewAssignmentDto> assignReviewer(UUID articleId, UUID reviewerId, String currentUserId) {
        return userService.getUserRoleFromSupabase(currentUserId)
                .flatMap(role -> {
                    if (!"editor".equalsIgnoreCase(role)) {
                        return Mono.error(new IllegalAccessException("Brak uprawnień do przypisania recenzenta"));
                    }

                    Map<String, Object> data = Map.of(
                            "article_id", articleId,
                            "reviewer_id", reviewerId,
                            "status", "pending",
                            "assigned_at", LocalDateTime.now().toString()
                    );

                    return supabaseWebClient.post()
                            .uri("/review_assignments")
                            .bodyValue(data)
                            .retrieve()
                            .bodyToMono(ReviewAssignmentDto.class);
                });
    }
}
