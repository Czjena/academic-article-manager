package com.sciarticles.manager.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;


import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleDto {
    private String title;
    private String authors;
    @JsonProperty("abstract")
    private String abstractText;
    private String keywords;
    private String category;
    private String status;
    private UUID submitted_by;
    private String file_path;

    public void setSubmittedBy(UUID uuid) {
        this.submitted_by = uuid;
    }
}
