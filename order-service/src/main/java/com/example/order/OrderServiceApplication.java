package com.example.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * order-service 的启动入口。
 *
 * 和 user-service 相比，这里多了一个关键注解：
 *
 * @EnableFeignClients —— 扫描项目中的 @FeignClient 接口，生成 HTTP 代理。
 *   没有这个注解，Feign 不会工作，调用 user-service 会失败。
 *   basePackages 指定扫描哪个包，精确指定可以加快启动速度。
 *
 * @EnableDiscoveryClient —— 向 Nacos 注册自己 + 从 Nacos 发现别人。
 *   order-service 需要发现 user-service 在哪，所以这个注解必须加。
 *
 * @SpringBootApplication —— 标准 Spring Boot 启动注解。
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.order.client")
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
