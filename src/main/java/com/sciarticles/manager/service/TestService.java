package com.sciarticles.manager.service;

import com.sciarticles.manager.dto.TestDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class TestService {

    private final WebClient webClient;

    public TestService(WebClient webClient) {
        this.webClient = webClient;
    }

    // Pobierz wszystkie testy
    public Mono<List<TestDto>> getAllTests() {
        return webClient.get()
                .uri("/testowa")
                .retrieve()
                .bodyToFlux(TestDto.class)
                .collectList()
                .doOnNext(list -> System.out.println("Ilość testów: " + list.size()));
    }
}
