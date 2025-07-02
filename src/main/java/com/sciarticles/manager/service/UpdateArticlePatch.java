package com.sciarticles.manager.service;

import com.sciarticles.manager.dto.ArticleDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Service
public class UpdateArticlePatch {

    private final WebClient webClient;

    public UpdateArticlePatch(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<ArticleDto> updateArticle(UUID id, ArticleDto newData) {
        // Pobierz artykuł po id
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/articles")
                        .queryParam("id", "eq." + id.toString())
                        .build())
                .retrieve()
                .bodyToFlux(ArticleDto.class)
                .next()
                .flatMap(existing -> {
                    if (!"draft".equalsIgnoreCase(existing.getStatus())) {
                        return Mono.error(new IllegalStateException("Edycja możliwa tylko w statusie 'draft'"));
                    }

                    // Zaktualizuj dane artykułu
                    Map<String, Object> updateMap = Map.of(
                            "title", newData.getTitle(),
                            "authors", newData.getAuthors(),
                            "abstractText", newData.getAbstractText(),
                            "keywords", newData.getKeywords(),
                            "category", newData.getCategory(),
                            "fileUrl", newData.getFile_path()
                    );

                    return webClient.patch()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/articles")
                                    .queryParam("id", "eq." + id.toString())
                                    .build())
                            .bodyValue(updateMap)
                            .retrieve()
                            .bodyToMono(ArticleDto.class);
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Artykuł nie znaleziony")));
    }
}
