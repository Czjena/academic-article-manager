package com.sciarticles.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateArticleDto {
    private String title;
    private String authors;
    @JsonProperty("abstract")
    private String abstractText;
    private String keywords;
    private String category;
    private String status;
    private String file_path;
}
