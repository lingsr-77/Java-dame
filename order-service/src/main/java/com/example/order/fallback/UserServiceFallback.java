package com.example.order.fallback;

import com.example.common.dto.UserDto;
import com.example.order.client.UserServiceClient;
import org.springframework.stereotype.Component;

/**
 * Sentinel 降级处理 —— 当 user-service 不可用时的"应急预案"。
 *
 * 这个类实现了与 Feign 客户端相同的接口，
 * 当 Feign 调用失败（超时、异常、服务不可用），
 * Sentinel 就会执行这里的方法，而不是把异常抛给用户。
 *
 * 降级策略三选一：
 *   1. 返回默认值/缓存数据（本示例的做法）
 *   2. 返回错误提示，让前端展示友好的错误信息
 *   3. 记录日志后重试（配合重试框架）
 *
 * 核心思想：宁可返回不完整的数据，也不要什么都不返回（崩溃）。
 * "优雅降级" 是微服务稳定性的基石。
 */
@Component
public class UserServiceFallback implements UserServiceClient {

    @Override
    public UserDto getUserById(Long id) {
        // 构造一个降级响应，用户名标记为"不可用"
        UserDto fallbackUser = new UserDto();
        fallbackUser.setId(id);
        fallbackUser.setName("User Unavailable (circuit open)");
        fallbackUser.setEmail("N/A");
        return fallbackUser;
    }
}
