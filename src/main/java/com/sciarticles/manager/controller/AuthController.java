package com.sciarticles.manager.controller;

import com.sciarticles.manager.model.UserRequest;
import com.sciarticles.manager.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Mono<ResponseEntity<String>> register(@RequestBody UserRequest request) {
        return authService.register(request)
                .thenReturn(ResponseEntity.ok("Registered successfully"))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body("Error: " + e.getMessage())));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<String>> login(@RequestBody UserRequest request) {
        return authService.login(request)
                .map(token -> ResponseEntity.ok("Bearer " + token))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed")));
    }
}