package com.example.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * user-service 的启动入口。
 *
 * 三个核心注解的含义：
 *
 * @SpringBootApplication
 *   等价于 @Configuration + @EnableAutoConfiguration + @ComponentScan
 *   - 标记这是 Spring Boot 应用
 *   - 开启自动配置（比如自动配置 DataSource、JPA、Tomcat）
 *   - 扫描当前包及子包下的所有 @Component、@Service、@Controller
 *
 * @EnableDiscoveryClient
 *   告诉 Spring Cloud：启动后向注册中心报到。
 *   具体报到哪个注册中心？看 classpath —— 有 Nacos 就报到 Nacos。
 *   服务启动后，Nacos 控制台就能看到 "user-service" 了。
 *
 * 注意：这里没有 @EnableFeignClients，因为 user-service 是"被调用方"，
 * 它不主动调别人，所以不需要 Feign 能力。
 */
@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
