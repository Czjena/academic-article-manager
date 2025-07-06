package com.sciarticles.manager.dto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;


import java.time.OffsetDateTime;
import java.util.UUID;
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleDto {
    @Getter
    @Setter
    @JsonProperty("id")
    private UUID id;
    private String title;
    private String authors;
    @JsonProperty("abstract")
    private String abstractText;
    private String keywords;
    private String category;
    private String status;
    private String text;
    private UUID submitted_by;
    private String submitted_at;
    private String file_path;

    public void setSubmittedBy(UUID uuid) {
        this.submitted_by = uuid;

    }

}
