package com.sciarticles.manager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciarticles.manager.dto.ArticleDto;

import com.sciarticles.manager.enums.ArticleStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ArticleService {


    public WebClient webClient;

    public ArticleService(WebClient webClient) {
        this.webClient = webClient;


    }
    // Pobierz wszystkie artykuły jako DTO
    public Mono<List<ArticleDto>> getAllArticles() {
        return webClient.get()
                .uri("/articles")
                .retrieve()
                .bodyToFlux(ArticleDto.class)
                .collectList()
                .doOnNext(list -> System.out.println("Ilość artykułów: " + list.size()));
    }


    // Pobierz surowe artykuły jako JSON String
    public Mono<String> getRawArticles() {
        return webClient.get()
                .uri("/articles")
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(json -> System.out.println("Otrzymany JSON: " + json))
                .doOnError(err -> System.err.println("Błąd: " + err.getMessage()));
    }


    public Mono<ArticleDto> getArticleById(UUID id) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/articles")
                        .queryParam("id", "eq." + id.toString())
                        .queryParam("select", "*")
                        .build())
                .retrieve()
                .bodyToFlux(ArticleDto.class)
                .next()
                .switchIfEmpty(Mono.error(new RuntimeException("Artykuł nie znaleziony")));
    }

    // Dodaj nowy artykuł
    public Mono<ArticleDto> createArticle(ArticleDto createArticleDto) {
        ObjectMapper mapper = new ObjectMapper();

        return webClient.post()
                .uri("/articles")
                .header("Prefer", "return=representation")
                .bodyValue(createArticleDto)
                .exchangeToMono(response -> response.bodyToMono(String.class)
                        .flatMap(body -> {
                            if (response.statusCode().is2xxSuccessful()) {
                                try {
                                    List<ArticleDto> articles = mapper.readValue(
                                            body,
                                            new TypeReference<List<ArticleDto>>() {}
                                    );
                                    if (articles != null && !articles.isEmpty()) {
                                        return Mono.just(articles.get(0));
                                    } else {
                                        return Mono.error(new RuntimeException("No data in Supabase response"));
                                    }
                                } catch (Exception parseError) {
                                    return Mono.error(parseError);
                                }
                            } else {
                                return Mono.error(new RuntimeException("Supabase error: " + response.statusCode() + " - " + body));
                            }
                        }));
    }
    @Value("${supabase.url}")
    private String supabaseUrl;

    public Flux<ArticleDto> getFilteredArticles(String title, String authors, String keywords, String status, String category) {
        UriBuilderFactory factory = new DefaultUriBuilderFactory(supabaseUrl + "/articles");

        UriBuilder uriBuilder = factory.builder();
        uriBuilder.queryParam("select", "*");

        if (title != null) uriBuilder.queryParam("title", "ilike.*" + title + "*");
        if (authors != null) uriBuilder.queryParam("authors", "ilike.*" + authors + "*");
        if (keywords != null) uriBuilder.queryParam("keywords", "ilike.*" + keywords + "*");
        if (status != null) uriBuilder.queryParam("status", "eq." + status);
        if (category != null) uriBuilder.queryParam("category", "eq." + category);

        String url = uriBuilder.build().toString();

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(ArticleDto.class);
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SupabaseResponse<T> {
        private List<T> data;
        private Object error;

        public List<T> getData() { return data; }
        public void setData(List<T> data) { this.data = data; }

        public Object getError() { return error; }
        public void setError(Object error) { this.error = error; }
    }

    // Aktualizuj artykuł
    public Mono<Void> updateArticle(UUID id, ArticleDto articleDto) {
        return webClient.patch()
                .uri(uriBuilder -> uriBuilder.path("/articles").queryParam("id", "eq." + id).build())
                .bodyValue(articleDto)
                .retrieve()
                .bodyToMono(Void.class);
    }

    // Usuń artykuł
    public Mono<Void> deleteArticle(UUID id) {
        return webClient.delete()
                .uri(uriBuilder -> uriBuilder.path("/articles").queryParam("id", "eq." + id).build())
                .retrieve()
                .bodyToMono(Void.class);
    }
    // Zmien status
    public Mono<Void> changeArticleStatus(UUID articleId, ArticleStatus newStatus) {
        return webClient.patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/articles")
                        .queryParam("id", "eq." + articleId)
                        .build())
                .bodyValue(new StatusUpdateRequest(newStatus))
                .retrieve()
                .bodyToMono(Void.class);
    }
    public Mono<Void> updateArticleTextInDb(UUID articleId, String newText) {
        // Tutaj logika aktualizacji artykułu w bazie lub przez API
        // Przykład przez REST API (WebClient):
        return webClient.patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/articles")
                        .queryParam("id","eq"+articleId.toString())
                        .build())
                .bodyValue(Map.of("text", newText))
                .retrieve()
                .bodyToMono(Void.class);
    }

    private record StatusUpdateRequest(ArticleStatus status) {}

    public Mono<Void> approveArticle(UUID articleId) {
        return webClient.post()
                .uri("/articles/{id}/approve", articleId)
                .retrieve()
                .bodyToMono(Void.class);
    }
    public Flux<ArticleDto> findAcceptedArticlesPagedSorted(int page, int size, String sortBy, String sortDir) {
        int offset = page * size;
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/articles")
                        .queryParam("status", "ACCEPTED")
                        .queryParam("limit", size)
                        .queryParam("offset", offset)
                        .queryParam("sortBy", sortBy)
                        .queryParam("sortDir", sortDir)
                        .build())
                .retrieve()
                .bodyToFlux(ArticleDto.class);
    }
    public Mono<Void> updateArticleText(UUID articleId, String newText) {

        return webClient.patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/articles")
                        .queryParam("id","eq."+articleId)
                        .build())
                .bodyValue(Map.of("text", newText))
                .retrieve()
                .bodyToMono(Void.class);
    }
    public Mono<Map<String, Long>> getArticleStatistics() {
        return getAllArticles()
                .map(articles -> {
                    Map<String, Long> stats = new HashMap<>();
                    stats.put("submitted", articles.stream()
                            .filter(a -> "SUBMITTED".equalsIgnoreCase(a.getStatus()))
                            .count());
                    stats.put("reviewed", articles.stream()
                            .filter(a -> "REVIEWED".equalsIgnoreCase(a.getStatus()))
                            .count());
                    stats.put("published", articles.stream()
                            .filter(a -> "PUBLISHED".equalsIgnoreCase(a.getStatus()))
                            .count());
                    return stats;
                });
    }


    public Mono<Void> rejectArticle(UUID articleId) {
        return webClient.post()
                .uri("/articles/{id}/reject", articleId)
                .retrieve()
                .bodyToMono(Void.class);
    }
    }

