package com.example.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * order-service 的启动入口。
 *
 * 注解说明：
 *
 * @EnableFeignClients —— 扫描 @FeignClient 接口，生成 HTTP 代理。
 * @EnableDiscoveryClient —— 向 Nacos 注册自己 + 从 Nacos 发现别人。
 * @EnableScheduling —— 开启定时任务（@Scheduled）。
 * @EnableRetry —— 开启 Spring Retry（@Retryable / @Recover）。
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.order.client")
@EnableScheduling
@EnableRetry
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
