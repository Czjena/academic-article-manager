package com.sciarticles.manager.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.sciarticles.manager.dto.ArticleDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ArticleExportService {

    public Mono<byte[]> exportArticlesToPdf(List<ArticleDto> articles) {
        return Mono.fromSupplier(() -> {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                PdfWriter writer = new PdfWriter(baos);
                PdfDocument pdfDoc = new PdfDocument(writer);
                Document document = new Document(pdfDoc);

                document.add(new Paragraph("Lista artykułów").setBold().setFontSize(16).setMarginBottom(15));

                // Tabela z 5 kolumnami o domyślnych szerokościach
                Table table = new Table(5);

                // Nagłówki tabeli
                table.addHeaderCell("ID");
                table.addHeaderCell("Tytuł");
                table.addHeaderCell("Autorzy");
                table.addHeaderCell("Kategoria");
                table.addHeaderCell("Status");

                // Wypełnienie tabeli danymi
                for (ArticleDto article : articles) {
                    table.addCell(article.getId() != null ? article.getId().toString() : "");
                    table.addCell(article.getTitle() != null ? article.getTitle() : "");
                    table.addCell(article.getAuthors() != null ? article.getAuthors() : "");
                    table.addCell(article.getCategory() != null ? article.getCategory() : "");
                    table.addCell(article.getStatus() != null ? article.getStatus() : "");
                }

                document.add(table);
                document.close();

                return baos.toByteArray();

            } catch (Exception e) {
                throw new RuntimeException("Błąd podczas generowania PDF", e);
            }
        });
    }
}
