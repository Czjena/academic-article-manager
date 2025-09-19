package com.sciarticles.manager.controller;

import com.sciarticles.manager.service.ArticleService;
import com.sciarticles.manager.service.FileTextExtractorService;
import com.sciarticles.manager.service.ReviewerService;
import com.sciarticles.manager.service.UserService;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/files")
public class FileUploadController {

    private final FileTextExtractorService extractorService;
    private final ArticleService articleService;
    private final WebClient webClient;
    private final UserService userService;

    public FileUploadController(FileTextExtractorService extractorService, ArticleService articleService, WebClient webClient, UserService userService) {
        this.extractorService = extractorService;
        this.articleService = articleService;
        this.webClient = webClient;
        this.userService = userService;
    }

    @PostMapping("/{articleId}/upload-file")
    public Mono<ResponseEntity<?>> uploadAndExtract(
            @PathVariable UUID articleId,
            @RequestPart("file") Mono<FilePart> filePartMono,
            @AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized")));
        }

        String email = jwt.getClaimAsString("email");

        return userService.getUserIdByEmail(email)
                .flatMap(userId -> filePartMono.flatMap(filePart -> {
                    String filename = filePart.filename().toLowerCase();

                    return DataBufferUtils.join(filePart.content())
                            .flatMap(dataBuffer -> {
                                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(bytes);
                                DataBufferUtils.release(dataBuffer);

                                MediaType contentType = filePart.headers().getContentType();
                                if (contentType == null) {
                                    contentType = MediaType.APPLICATION_OCTET_STREAM;
                                }

                                String originalFileName = filePart.filename();
                                String uploadUrl = String.format("https://bgbnastkfzfpvfdkfjsc.supabase.co/storage/v1/object/pdf/%s", originalFileName);

                                return webClient.post()
                                        .uri(uploadUrl)
                                        .contentType(contentType)
                                        .bodyValue(bytes)
                                        .retrieve()
                                        .onStatus(status -> status.isError(), response ->
                                                response.bodyToMono(String.class).flatMap(errorBody ->
                                                        Mono.error(new RuntimeException("Supabase error " + response.statusCode() + ": " + errorBody))
                                                )
                                        )
                                        .bodyToMono(Void.class)
                                        .thenReturn(bytes)
                                        .flatMap(uploadedBytes -> {
                                            InputStream is = new ByteArrayInputStream(uploadedBytes);

                                            return Mono.fromCallable(() -> {
                                                        String text;
                                                        if (filename.endsWith(".docx")) {
                                                            text = extractorService.extractTextFromDocx(is);
                                                        } else if (filename.endsWith(".pdf")) {
                                                            text = extractorService.extractTextFromPdf(is);
                                                        } else if (filename.endsWith(".tex")) {
                                                            text = extractorService.extractTextFromLatex(is);
                                                        } else {
                                                            throw new IllegalArgumentException("Nieobsługiwany format pliku");
                                                        }
                                                        return text;
                                                    })
                                                    .flatMap(text -> articleService.updateArticleText(articleId, text))
                                                    .thenReturn(ResponseEntity.ok(Map.of(
                                                            "message", "Plik załadowany i tekst wyekstrahowany",
                                                            "uploadedByUserId", userId.toString(),
                                                            "filename", filename
                                                    )))
                                                    .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(
                                                            Map.of("error", "Błąd podczas ekstrakcji: " + e.getMessage())
                                                    )));
                                        });
                            });
                }));
    }
}

