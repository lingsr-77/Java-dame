package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from ClaudeJavaDemo!";
    }

    @GetMapping("/version")
    public String version() {
        return "v1.0.0";
    }

    @GetMapping("/time")
    public String time() {
        return java.time.LocalDateTime.now().toString();
    }
}
