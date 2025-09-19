package com.sciarticles.manager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.api-key}")
    private String apiKey;

    @Value("${service.role-key}")
    private String serviceRoleToken;

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .baseUrl(supabaseUrl)
                .defaultHeader("apikey", apiKey)
                .defaultHeader("Authorization", "Bearer " + serviceRoleToken)
                .build();
    }
}