package com.sciarticles.manager.dto;

public class ArticleSummaryDto {
    private String title;
    private String abstractText;

    public ArticleSummaryDto() {}

    public ArticleSummaryDto(String title, String abstractText) {
        this.title = title;
        this.abstractText = abstractText;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }
}
