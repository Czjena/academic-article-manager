package com.sciarticles.manager.controller;

import com.sciarticles.manager.dto.TestDto;
import com.sciarticles.manager.service.TestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping("/tests")
    public Mono<List<TestDto>> getAllTests() {
        return testService.getAllTests();
    }
}
