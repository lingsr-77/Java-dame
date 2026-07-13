package com.example.common.api;

import com.example.common.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign 客户端接口 —— 服务之间调用的"契约"。
 *
 * 这就像一个"合同"，定义了 user-service 对外提供什么能力：
 *   "你给我一个用户 ID，我返回这个用户的信息"
 *
 * 为什么放在 common 模块？
 *   调用方（order-service）和被调用方（user-service）都依赖 common，
 *   共享同一份接口定义，不会出现 "你改了我不知道" 的问题。
 *   类比：双方签同一份合同，各持一份副本。
 *
 * @FeignClient 的几个参数：
 *   name = "user-service"  → 去 Nacos 查"谁叫 user-service"，拿到 IP:端口
 *   path = "/api/users"    → 所有方法路径的前缀
 */
@FeignClient(name = "user-service", path = "/api/users")
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
