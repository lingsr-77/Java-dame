package com.example.order.client;

import com.example.common.api.UserServiceApi;
import com.example.order.fallback.UserServiceFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Feign 客户端 —— order-service 调用 user-service 的"遥控器"。
 *
 * fallbackFactory（推荐）vs fallback 的区别：
 *   fallback：只捕获 HTTP 调用失败
 *   fallbackFactory：捕获所有异常（包括找不到实例、超时、HTTP 错误等）
 *
 * 当 user-service 完全宕机、Nacos 里没有健康实例时，
 * LoadBalancer 抛出异常 → fallbackFactory 捕获 → 返回降级数据。
 */
@FeignClient(
        name = "user-service",
        path = "/api/users",
        fallbackFactory = UserServiceFallbackFactory.class
)
public interface UserServiceClient extends UserServiceApi {
}
