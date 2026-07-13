package com.example.common.api;

import com.example.common.dto.UserDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 服务调用契约接口 —— 只定义"合同"，不加 @FeignClient。
 *
 * 为什么不在这里加 @FeignClient？
 *   common 模块只定义合约（方法签名 + 路径映射），
 *   具体的 Feign 客户端由 order-service 里的 UserServiceClient 来声明，
 *   因为熔断降级（fallback）是调用方自己的事，不应该写死在公共模块里。
 *
 * 这就像一个"合同模板"：
 *   - user-service 照着它实现 @RestController
 *   - order-service 继承它，加上 @FeignClient 和 fallback
 */
public interface UserServiceApi {

    /**
     * 根据用户 ID 查询用户信息。
     *
     * @GetMapping("/{id}") 等价于 @RequestMapping(method = GET, path = "/{id}")
     * {id} 是一个路径变量，用 @PathVariable 绑定到方法参数上。
     *
     * 当调用 userServiceApi.getUserById(1L) 时，
     * Feign 实际发送的是：GET http://user-service的IP:端口/api/users/1
     */
    @GetMapping("/{id}")
    UserDto getUserById(@PathVariable("id") Long id);
}
