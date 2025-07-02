package com.sciarticles.manager.controller;

import com.sciarticles.manager.dto.ReviewerDto;
import com.sciarticles.manager.service.AssignReviewerService;
import com.sciarticles.manager.service.ReviewerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/reviewers")
@RequiredArgsConstructor
public class ReviewerController {

    private final ReviewerService reviewerService;
    private final AssignReviewerService assignReviewerService;

    @PostMapping
    public Mono<ResponseEntity<ReviewerDto>> addReviewer(@RequestBody ReviewerDto dto) {
        return reviewerService.addReviewer(dto)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public Mono<ResponseEntity<List<ReviewerDto>>> getAllReviewers() {
        return reviewerService.getAllReviewers()
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteReviewer(@PathVariable UUID id) {
        return reviewerService.deleteReviewer(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @Value("${supabase.api-key}")
    private String serviceRoleKey;

    @PostMapping("/assign/{articleId}/{reviewerId}")
    public Mono<ResponseEntity<Object>> assignReviewer(
            @PathVariable UUID articleId,
            @PathVariable UUID reviewerId,
            @AuthenticationPrincipal Jwt jwt  // aktualny zalogowany user
    ) {
        // Pobierz service role key z konfiguracji albo wstrzyknij przez konstruktor (tu uproszczenie)

        return assignReviewerService.assignReviewer(articleId, reviewerId, serviceRoleKey)
                .map(v -> ResponseEntity.ok().build())
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).build()));
    }
}
