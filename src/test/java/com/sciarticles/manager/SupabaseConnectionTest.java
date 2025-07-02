package com.sciarticles.manager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@SpringBootTest
public class SupabaseConnectionTest {

    @Autowired
    private WebClient webClient;

    @Test
    void testSupabaseGetArticles() {
        Mono<String> responseMono = webClient.get()
                .uri("/articles?select=*")
                .retrieve()
                .onStatus(
                        status -> status.value() == 403,
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(body -> {
                                    System.err.println("ERROR 403 Forbidden from Supabase:");
                                    System.err.println(body);
                                    return Mono.error(new RuntimeException("Forbidden: " + body));
                                })
                )
                .bodyToMono(String.class);

        String response = responseMono.block(); // blokujemy, aby wynik się pojawił w teście
        System.out.println("Response from Supabase:");
        System.out.println(response);
    }
}
