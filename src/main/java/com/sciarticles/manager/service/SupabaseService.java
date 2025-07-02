package com.sciarticles.manager.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class SupabaseService {

    private final WebClient webClient;

    public SupabaseService(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.api-key}") String supabaseKey
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(supabaseUrl)
                .defaultHeader("apikey", supabaseKey)
                .defaultHeader("Authorization", "Bearer " + supabaseKey)
                .build();
    }

    public String testConnection() {
        // Przykładowe zapytanie do tabeli 'articles'
        return webClient.get()
                .uri("/articles?select=*") // zmień 'articles' na swoją nazwę tabeli
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}