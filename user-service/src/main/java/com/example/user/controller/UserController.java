package com.example.user.controller;

import com.example.common.dto.UserDto;
import com.example.user.entity.User;
import com.example.user.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

/**
 * 用户服务对外暴露的 REST 接口。
 *
 * 这个 Controller 的 /api/users/{id} 端点，
 * 和 common 模块中 UserServiceApi 接口定义的方法是一一对应的：
 *   Feign 接口：@GetMapping("/{id}") UserDto getUserById(@PathVariable("id") Long id)
 *   实际实现：@GetMapping("/{id}") UserDto getUserById(@PathVariable Long id)
 *
 * 方法的路径、参数、返回值类型必须一致，否则 Feign 调用会报错！
 * 这就是为什么我们把 Feign 接口放在 common 里 —— 一份契约，双方遵守。
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    /**
     * 构造器注入（推荐方式，比 @Autowired 字段注入更好测试）。
     * Spring 自动从容器中找到 UserRepository 的实例并传入。
     */
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 根据 ID 查询用户。
     *
     * 访问方式：GET http://user-service:8082/api/users/1
     * 或者通过网关：GET http://gateway:8080/api/users/1
     *
     * 注意：这里返回的是 UserDto，不是 User 实体。
     * 如果把实体直接返回给外部，当数据库加了敏感字段（如 password）时，
     * 就会造成信息泄露。DTO 是"对外窗口"，只暴露允许外部看到的数据。
     */
    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        // 手动把 Entity 转为 DTO，这是数据安全的关键一步
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }

    /**
     * 健康检查/简单信息端点。
     * GET /api/users/count → "Total users: 5"
     */
    @GetMapping("/count")
    public String count() {
        return "Total users: " + userRepository.count();
    }
}
