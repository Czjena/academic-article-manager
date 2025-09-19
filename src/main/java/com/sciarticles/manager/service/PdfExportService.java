package com.sciarticles.manager.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;

@Service
public class PdfExportService {

    public Mono<byte[]> exportSummaryToPdf(String title, String abstractText) {
        return Mono.fromSupplier(() -> {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                PdfWriter writer = new PdfWriter(baos);
                PdfDocument pdfDoc = new PdfDocument(writer);
                Document document = new Document(pdfDoc);

                document.add(new Paragraph(title).setBold().setFontSize(16).setMarginBottom(10));
                document.add(new Paragraph(abstractText).setFontSize(12));

                document.close();
                return baos.toByteArray();
            } catch (Exception e) {
                throw new RuntimeException("Błąd podczas generowania PDF streszczenia", e);
            }
        });
    }
}
