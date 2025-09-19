package com.sciarticles.manager.dto;


import lombok.Data;

import java.util.UUID;

@Data
public class ReviewerDto {

    private UUID id;
    private String first_name;
    private String last_name;
    private String email;
    private String affiliation;
    private String field; // dziedzina naukowa
    private String message;


    public ReviewerDto(UUID id, String message) {
        this.id = id;
                this.message = message;
    }
}