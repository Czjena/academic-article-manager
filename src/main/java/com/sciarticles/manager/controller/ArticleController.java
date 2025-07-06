package com.sciarticles.manager.controller;

import com.sciarticles.manager.dto.ArticleDto;
import com.sciarticles.manager.dto.CreateArticleDto;
import com.sciarticles.manager.enums.ArticleStatus;
import com.sciarticles.manager.security.VerifyRole;
import com.sciarticles.manager.service.*;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
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
    private final UserService userService;
    private final VerifyRole verifyRole;
    private final ArticleExportService exportService;


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
    @PutMapping("/articles/{id}")
    public Mono<ResponseEntity<Void>> updateArticleText(
            @PathVariable UUID id,
            @RequestBody Map<String, String> updates) {
        String newText = updates.get("text");
        return articleService.updateArticleTextInDb(id, newText)
                .thenReturn(ResponseEntity.noContent().build());
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
    @GetMapping("/filter")
    public Mono<ResponseEntity<List<ArticleDto>>> getFilteredArticles(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String authors,
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category
    ) {
        return articleService.getFilteredArticles(title, authors, keywords, status, category)
                .collectList()
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).build()));
    }
    @GetMapping("/articles/accepted")
    public Flux<ArticleDto> getAcceptedArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return articleService.findAcceptedArticlesPagedSorted(page, size, sortBy, sortDir);
    }

    @PostMapping("/articles/{id}/reject")
    public Mono<ResponseEntity<Object>> rejectArticle(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        return verifyRole.checkRoles(jwt, "supervisor")
                .flatMap(authError -> Mono.just(authError))
                .switchIfEmpty(
                        articleService.rejectArticle(id)
                                .map(a -> ResponseEntity.<Object>ok(Map.of("message", "Artykuł odrzucony")))
                                .defaultIfEmpty(ResponseEntity.notFound().build())
                                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).build()))
                );
    }


    @PostMapping("/articles/{id}/approve")
    public Mono<ResponseEntity<Object>> approveArticle(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        return verifyRole.checkRoles(jwt, "supervisor")
                .flatMap(authError -> Mono.just(authError))
                .switchIfEmpty(
                        articleService.approveArticle(id)
                                .map(resultString -> ResponseEntity.<Object>ok(Map.of("message", "Artykuł zatwierdzony", "result", resultString)))
                                .defaultIfEmpty(ResponseEntity.notFound().build())
                                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).build()))
                );
    }

    @GetMapping("/export/pdf")
    public Mono<ResponseEntity<byte[]>> exportArticlesPdf() {
        return articleService.getAllArticles() // przyjmuję, że masz taki serwis zwracający Mono<List<ArticleDto>>
                .flatMap(exportService::exportArticlesToPdf)
                .map(pdfBytes -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=articles.pdf")
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdfBytes));
    }

    @PostMapping
    public Mono<ResponseEntity<Map<String, String>>> createArticle(
            @RequestBody CreateArticleDto dto,
            @AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized")));
        }

        String email = jwt.getClaimAsString("email");

        return userService.getUserIdByEmail(email)
                .flatMap(userId -> {
                    ArticleDto fullDto = ArticleDto.builder()
                            .title(dto.getTitle())
                            .authors(dto.getAuthors())
                            .abstractText(dto.getAbstractText())
                            .keywords(dto.getKeywords())
                            .category(dto.getCategory())
                            .submitted_by(userId) // 👈 tutaj przypisujesz użytkownika
                            .build();

                    return articleService.createArticle(fullDto)
                            .map(created -> ResponseEntity.status(HttpStatus.CREATED)
                                    .body(Map.of("id", created.getId().toString())));
                });
    }

    @GetMapping("/statistics")
    public Mono<ResponseEntity<Object>> getStatistics() {
        return articleService.getArticleStatistics()
                .map(stats -> ResponseEntity.ok((Object) stats))
                .defaultIfEmpty(ResponseEntity.noContent().build())
                .onErrorResume(e -> {
                    Map<String, String> error = Map.of("error", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
                });
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


    @PatchMapping("/{articleId}/status")
    public Mono<ResponseEntity<Void>> updateStatus(@PathVariable UUID articleId,
                                                   @RequestParam ArticleStatus status) {
        return articleService.changeArticleStatus(articleId, status)
                .then(Mono.defer(() -> Mono.just(ResponseEntity.noContent().<Void>build())))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }
}

