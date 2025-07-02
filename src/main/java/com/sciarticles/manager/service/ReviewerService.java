package com.sciarticles.manager.service;

import com.sciarticles.manager.dto.ReviewerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewerService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.api-key}")
    private String apiKey;

    private WebClient getClient() {
        return WebClient.builder()
                .baseUrl(supabaseUrl + "/rest/v1/reviewers")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader("apikey", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public Mono<ReviewerDto> addReviewer(ReviewerDto dto) {
        return getClient().post()
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(ReviewerDto[].class)
                .map(response -> response[0]);
    }

    public Mono<List<ReviewerDto>> getAllReviewers() {
        return getClient().get()
                .retrieve()
                .bodyToMono(ReviewerDto[].class)
                .map(Arrays::asList);
    }

    public Mono<Void> deleteReviewer(UUID id) {
        return getClient().delete()
                .uri(uriBuilder -> uriBuilder.queryParam("id", "eq." + id).build())
                .retrieve()
                .bodyToMono(Void.class);
    }
}
