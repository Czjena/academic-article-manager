package com.sciarticles.manager.service;

import com.sciarticles.manager.dto.ArticleDto;

import com.sciarticles.manager.enums.ArticleStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class ArticleService {

    public WebClient webClient;

    public ArticleService(WebClient webClient) {
        this.webClient = webClient;
    }
    // Pobierz wszystkie artykuły
    public Mono<List<ArticleDto>> getAllArticles() {
        return webClient.get()
                .uri("/articles")
                .retrieve()
                .bodyToFlux(ArticleDto.class)
                .collectList()
                .doOnNext(list -> System.out.println("Ilość artykułów: " + list.size()));
    }

    // Pobierz artykuł po id
    public Mono<ArticleDto> getArticleById(UUID id) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/articles").queryParam("id", "eq." + id).build())
                .retrieve()
                .bodyToFlux(ArticleDto.class)
                .next(); // bo Supabase zwraca tablicę, a my chcemy pierwszy element
    }

    // Dodaj nowy artykuł
    public Mono<ArticleDto> createArticle(ArticleDto articleDto) {
        return webClient.post()
                .uri("/articles")
                .bodyValue(articleDto)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(ArticleDto.class);
                    } else {
                        return response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    // Logujemy błąd jeśli chcesz
                                    // logger.error("Supabase error: {}", errorBody);
                                    return Mono.error(new RuntimeException(
                                            "Supabase error " + response.statusCode() + ": " + errorBody));
                                });
                    }
                });
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

    private record StatusUpdateRequest(ArticleStatus status) {}
}

