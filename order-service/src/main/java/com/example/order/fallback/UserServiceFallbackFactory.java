package com.example.order.fallback;

import com.example.common.dto.UserDto;
import com.example.order.client.UserServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * fallbackFactory vs fallback 的区别：
 *
 * fallback = UserServiceFallback.class       → 只捕获 Sentinel 熔断 + Feign HTTP 错误
 * fallbackFactory = UserServiceFallbackFactory.class → 捕获所有异常，包括：
 *   - LoadBalancer 找不到实例（No instances available）
 *   - 连接超时
 *   - HTTP 5xx/4xx
 *   - Sentinel 熔断打开
 *   - 任何其他 RuntimeException
 *
 * 实际生产中几乎都用 fallbackFactory，因为你需要知道"为什么降级了"，
 * 关键时刻能打印日志排查问题。
 */
@Component
public class UserServiceFallbackFactory implements FallbackFactory<UserServiceClient> {

    private static final Logger log = LoggerFactory.getLogger(UserServiceFallbackFactory.class);

    @Override
    public UserServiceClient create(Throwable cause) {
        // 记录降级原因，方便排查问题
        log.error("user-service 调用失败，触发降级。原因: {}", cause.getMessage());

        return new UserServiceClient() {
            @Override
            public UserDto getUserById(Long id) {
                UserDto fallbackUser = new UserDto();
                fallbackUser.setId(id);
                fallbackUser.setName("User Unavailable");
                fallbackUser.setEmail("N/A");
                return fallbackUser;
            }
        };
    }
}
