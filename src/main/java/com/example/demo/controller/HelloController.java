package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return String.format(
                "<html><body style=\"display:flex;justify-content:center;align-items:center;height:100vh;margin:0;font-size:48px;font-weight:bold;\">%s</body></html>",
                "梁鲲，你是癞疙包!");
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
