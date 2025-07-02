package com.sciarticles.manager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@SpringBootTest
public class ServiceWebClientTest {

    @Autowired
    private WebClient webClient;

    @Test
    public void testSupabaseHeaders() {
        Mono<String> responseMono = webClient.get()
                .uri("/users?limit=1")
                .headers(headers -> {
                    System.out.println("Wysyłane nagłówki:");
                    headers.forEach((k, v) -> System.out.println(k + ": " + v));
                })
                .retrieve()
                .bodyToMono(String.class);

        String response = responseMono.block();  // blokujemy na wynik, bo to test

        System.out.println("Odpowiedź z Supabase:");
        System.out.println(response);
    }
}