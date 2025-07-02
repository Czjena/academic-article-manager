package com.sciarticles.manager.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ReviewDto {
    private UUID id;
    private UUID assignmentId;
    private UUID reviewerId;
    private UUID articleId;
    private Integer rating;
    private String comments;
}