package com.sciarticles.manager.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ReviewerDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String affiliation;
    private String field; // dziedzina naukowa
}