package com.example.springboot_app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @GetMapping("/")
    public String hello() {
        return "Hello from Spring Boot! Environment: " + activeProfile;
    }

    @GetMapping("/health-check")
    public String health() {
        return "UP";
    }
}