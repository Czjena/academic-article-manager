package com.sciarticles.manager.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ReviewAssignmentDto {
    private UUID id;
    private UUID reviewerId;
    private UUID articleId;
}