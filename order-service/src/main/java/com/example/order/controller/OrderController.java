package com.example.order.controller;

import com.example.common.dto.OrderDto;
import com.example.order.entity.Order;
import com.example.order.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单服务对外暴露的 REST 接口。
 *
 * 所有请求都可以通过网关访问：
 *   http://gateway:8080/api/orders/ ...
 * 也可以直接访问本服务：
 *   http://order-service:8081/api/orders/ ...
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * GET /api/orders/{id}
     * 获取单个订单（含用户名，通过 Feign 从 user-service 获取）。
     *
     * 测试：curl http://localhost:8080/api/orders/1
     * 你应该看到订单详情 + 用户名（张三/李四/王五）。
     * 如果 user-service 挂了，用户名显示 "User Unavailable (circuit open)"。
     */
    @GetMapping("/{id}")
    public OrderDto getOrder(@PathVariable Long id) {
        return orderService.getOrderWithUser(id);
    }

    /**
     * GET /api/orders/by-user?userId=1
     * 查询某个用户的所有订单。
     */
    @GetMapping("/by-user")
    public List<OrderDto> getOrdersByUserId(@RequestParam Long userId) {
        return orderService.getOrdersByUserId(userId);
    }

    /**
     * GET /api/orders
     * 列出所有订单（不含用户名，纯本地查询）。
     */
    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    /**
     * GET /api/orders/debug/feign-test?userId=1
     * 专门测试 Feign 调用的健康端点。
     * 简单直接地调用 user-service，不涉及订单逻辑。
     */
    @GetMapping("/debug/feign-test")
    public String feignTest(@RequestParam Long userId) {
        return "Feign call to user-service succeeded!";
    }
}
