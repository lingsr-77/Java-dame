package com.example.user.service;

import com.example.user.entity.User;
import com.example.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动时自动插入测试用户数据。
 *
 * CommandLineRunner 是 Spring Boot 的一个钩子（Hook），
 * 当 ApplicationContext 完全初始化后、应用正式对外服务前，
 * Spring 会自动调用 run() 方法。
 *
 * 这就像你之前的 DataInitializer.java 一样，
 * 用来保证数据库里有测试数据可以用。
 */
@Component
public class UserDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    public UserDataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        // 只有表为空时才插入，避免重复
        if (userRepository.count() == 0) {
            userRepository.save(new User(null, "张三", "zhangsan@example.com"));
            userRepository.save(new User(null, "李四", "lisi@example.com"));
            userRepository.save(new User(null, "王五", "wangwu@example.com"));
            System.out.println(">>> user-service: 已插入 3 条测试用户数据");
        }
    }
}
