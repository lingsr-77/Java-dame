package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API 网关启动入口。
 *
 * 注意：这里没有 @EnableFeignClients！
 * 网关不通过 Feign 调用服务，而是"代理转发"。
 * 客户端的 HTTP 请求到达网关后，网关根据路由规则，
 * 把请求原样转发给后端服务，再原样返回响应。
 *
 * 网关本质是一个"反向代理"：
 *   浏览器 → 网关（8080）→ order-service（8081）
 *   浏览器 ← 网关（8080）← order-service（8081）
 *
 * 没有 @SpringBootApplication 中的 Web MVC（Tomcat），
 * 因为 spring-cloud-starter-gateway 自带 WebFlux（Netty）。
 * 这是响应式编程模型，更适合高并发的网关场景。
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
