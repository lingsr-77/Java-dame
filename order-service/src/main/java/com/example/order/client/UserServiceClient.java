package com.example.order.client;

import com.example.common.api.UserServiceApi;
import com.example.common.dto.UserDto;
import com.example.order.fallback.UserServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Feign 客户端 —— order-service 调用 user-service 的"遥控器"。
 *
 * 你不需要写任何 HTTP 请求代码，只需声明一个接口，
 * Feign 会自动：
 *   1. 向 Nacos 查询 "user-service" 有哪些健康实例
 *   2. 通过 LoadBalancer 选一个实例
 *   3. 构建 HTTP 请求（URL、Header、Body）
 *   4. 发送请求并解析响应 JSON → UserDto
 *
 * fallback = UserServiceFallback.class 是 Sentinel 熔断的关键：
 *   当 user-service 挂了、超时了、或者返回异常过多时，
 *   Sentinel 会"打开断路器"，不再发请求过去，
 *   而是直接调用 UserServiceFallback 里的方法，返回一个降级结果。
 *
 * 这就好比：快递站点坏了，不再傻等，直接给你发个"暂时无法送达"的通知。
 * 如果不做降级，调用方会一直阻塞等待，最终线程耗尽，整个系统崩溃。
 */
@FeignClient(
        name = "user-service",          // Nacos 里的服务名
        path = "/api/users",            // 路径前缀
        fallback = UserServiceFallback.class  // 熔断后的降级处理
)
public interface UserServiceClient extends UserServiceApi {
    // 继承了 UserServiceApi 的 getUserById 方法。
    // 所以这里不需要再写方法，只是加上了 fallback 配置。
    // 为什么分开？因为 common 模块里不能依赖 Sentinel（那是具体实现选型），
    // 而这里可以。
}
