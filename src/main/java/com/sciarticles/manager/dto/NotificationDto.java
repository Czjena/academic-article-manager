package com.sciarticles.manager.dto;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private UUID id;
    private UUID userId;
    private UUID articleId;
    private String message;
    private Boolean isRead;
    private Instant createdAt;
}
