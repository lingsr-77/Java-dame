package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "梁鲲，你是癞疙包!";
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
