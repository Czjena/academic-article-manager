package com.sciarticles.manager.controller;

import com.sciarticles.manager.dto.ArticleDto;
import com.sciarticles.manager.dto.ReviewAssignmentDto;
import com.sciarticles.manager.service.*;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    private final UpdateArticlePatch updateArticlePatch;
    private final AssignReviewer assignReviewer;
    private final ReviewAssignmentService reviewAssignmentService;
    private final UserService userService;

    @PatchMapping("/{id}")
    public Mono<ResponseEntity<ArticleDto>> UpdateArticlePatch(
            @PathVariable UUID id,
            @RequestBody ArticleDto articleDto
    ) {
        return updateArticlePatch.updateArticle(id, articleDto)
                .map(updated -> ResponseEntity.ok(updated))
                .onErrorResume(e -> {
                    if (e instanceof IllegalStateException) {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
                    } else if (e instanceof IllegalArgumentException) {
                        return Mono.just(ResponseEntity.notFound().build());
                    } else {
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                    }
                });
    }

    @GetMapping
    public ResponseEntity<List<ArticleDto>> getAllArticles() {
        List<ArticleDto> articles;
        try {
            articles = articleService.getAllArticles().block();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        if (articles == null || articles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleDto> getArticleById(@PathVariable UUID id) {
        ArticleDto article;
        try {
            article = articleService.getArticleById(id).block();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        if (article == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(article);
    }

    @PostMapping("/{articleId}/assign-reviewer/{reviewerId}")
    public Mono<ResponseEntity<ReviewAssignmentDto>> assignReviewer(
            @PathVariable UUID articleId,
            @PathVariable UUID reviewerId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String currentUserJwt = (String) ((org.springframework.security.oauth2.jwt.Jwt) jwt).getTokenValue();

        return assignReviewer.assign(articleId, reviewerId, currentUserJwt)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    if (e instanceof IllegalAccessException) {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @PostMapping
    public ResponseEntity<?> createArticle(@RequestBody ArticleDto articleDto,
                                           Authentication authentication) {
        try {

            String email = (String) authentication.getPrincipal();

            UUID userId = userService.getUserIdByEmail(email).block();

            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "User with email " + email + " not found"));
            }

            articleDto.setSubmittedBy(userId);
            ArticleDto created = articleService.createArticle(articleDto).block();

            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable UUID id) {
        try {
            articleService.deleteArticle(id).block();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.noContent().build();
    }

    @Value("${service.role-key}")
    String serviceKey = "${service.role-key}";

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, Authentication authentication) {
        try {
            String email = (String) authentication.getPrincipal();
            UUID userId = userService.getUserIdByEmail(email).block();

            if (userId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "User with email " + email + " not found"));
            }

            System.out.println(serviceKey);
            String originalFileName = file.getOriginalFilename();
            String uploadUrl = String.format("https://bgbnastkfzfpvfdkfjsc.supabase.co/storage/v1/object/pdf/%s", originalFileName);


            WebClient webClient = WebClient.builder()
                    .baseUrl(uploadUrl)
                    .defaultHeader(HttpHeaders.AUTHORIZATION,   serviceKey)  // bez "Bearer "
                    .build();

            webClient.post()
                    .contentType(MediaType.parseMediaType(file.getContentType()))
                    .bodyValue(file.getBytes())
                    .retrieve()
                    .onStatus(status -> status.isError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> Mono.error(new RuntimeException("Supabase error "
                                            + clientResponse.statusCode() + ": " + errorBody))))
                    .bodyToMono(Void.class)
                    .block();

            String publicUrl = String.format("https://bgbnastkfzfpvfdkfjsc.supabase.co/storage/v1/object/public/pdf/%s", originalFileName);

            return ResponseEntity.ok(Map.of(
                    "message", "File uploaded successfully",
                    "fileUrl", publicUrl,
                    "uploadedByUserId", userId.toString()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }


}

