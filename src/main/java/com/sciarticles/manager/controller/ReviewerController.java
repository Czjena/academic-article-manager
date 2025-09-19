package com.sciarticles.manager.controller;


import com.sciarticles.manager.security.VerifyRole;
import com.sciarticles.manager.service.AssignReviewerService;
import com.sciarticles.manager.service.ReviewerService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/reviewers")
@RequiredArgsConstructor
public class ReviewerController {

    private final ReviewerService reviewerService;
    private final AssignReviewerService assignReviewerService;
    private final VerifyRole verifyRole;

    @Value("${supabase.api-key}")
    private String serviceRoleKey;


    @PatchMapping("/{userId}/role")
    public Mono<ResponseEntity<Void>> addReviewerRole(@PathVariable UUID userId) {
        ResponseEntity<Void> noContentResponse = ResponseEntity.status(HttpStatus.NO_CONTENT).build();

        return reviewerService.addReviewerRoleToUser(userId)
                .thenReturn(noContentResponse)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }


    @PostMapping("/assign/{articleId}/{reviewerId}")
    public Mono<ResponseEntity<Object>> assignReviewer(
            @PathVariable UUID articleId,
            @PathVariable UUID reviewerId,
            @AuthenticationPrincipal Jwt jwt)
    {
        return verifyRole.checkRoles(jwt,  "admin")
                .flatMap(authError -> Mono.just(authError))
                .switchIfEmpty(
                        assignReviewerService.assignReviewer(articleId, reviewerId, serviceRoleKey)
                                .map(v -> ResponseEntity.ok().build())
                                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).build()))
                );
    }
    @PostMapping
    public Mono<ResponseEntity<Object>> addReview(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal Jwt jwt) {

        String articleIdStr = body.get("articleId");
        String content = body.get("content");

        if (articleIdStr == null || content == null || jwt == null) {
            return Mono.just(ResponseEntity.badRequest()
                    .body((Object) Map.of("error", "Brak danych lub nieautoryzowany")));
        }

        UUID articleId = UUID.fromString(articleIdStr);
        String reviewerEmail = jwt.getClaimAsString("email");

        return reviewerService.assignReview(articleId, reviewerEmail, content)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED)
                        .body((Object) Map.of("message", "Recenzja dodana")))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body((Object) Map.of("error", e.getMessage()))
                ));
    }

}
