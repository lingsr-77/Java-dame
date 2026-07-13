package com.example.order.service;

import com.example.common.dto.OrderDto;
import com.example.common.dto.UserDto;
import com.example.order.client.UserServiceClient;
import com.example.order.entity.Order;
import com.example.order.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单业务逻辑层。
 *
 * 核心演示了微服务的"服务编排/数据聚合"模式：
 *   订单数据在 order-service 自己的数据库里，
 *   但展示订单时需要用户名，用户名在 user-service 里。
 *   所以 getOrderWithUser() 先查自己的数据库，再远程调 user-service。
 *
 * 这是微服务 vs 单体应用最大的区别之一：
 *   单体：SELECT o.*, u.name FROM orders o JOIN users u ON o.user_id = u.id
 *   微服务：先查 order-service 的 orders 表，再调 user-service 接口
 *
 * trade-off：微服务失去了数据库 JOIN 的便利，但换来了独立部署和独立扩容的能力。
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserServiceClient userServiceClient;

    public OrderService(OrderRepository orderRepository,
                        UserServiceClient userServiceClient) {
        this.orderRepository = orderRepository;
        this.userServiceClient = userServiceClient;
    }

    /**
     * 获取订单详情（含用户名）。
     *
     * 流程：
     * 1. 从自己数据库查订单
     * 2. 远程调用 user-service 获取用户名
     * 3. 组装后返回给前端
     *
     * 如果第 2 步 user-service 挂了：
     *   不会抛异常！Sentinel 会调用 UserServiceFallback，
     *   用户名显示 "User Unavailable"，但订单数据本身不受影响。
     *   这就是"优雅降级"——部分功能不可用时，整体仍然可用。
     */
    public OrderDto getOrderWithUser(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // ★ 关键：这是远程调用，不是本地方法调用！
        // 底层是 HTTP GET http://user-service/api/users/{userId}
        // 如果 user-service 挂了，Sentinel 走 UserServiceFallback
        UserDto user = userServiceClient.getUserById(order.getUserId());

        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setUserName(user.getName());   // 来自 user-service
        dto.setStatus(order.getStatus());
        dto.setCreateTime(order.getCreateTime());
        return dto;
    }

    /**
     * 查询某个用户的所有订单（含用户名）。
     */
    public List<OrderDto> getOrdersByUserId(Long userId) {
        // 先拿到用户名（一次远程调用）
        UserDto user = userServiceClient.getUserById(userId);

        // 查该用户的所有订单
        List<Order> orders = orderRepository.findByUserId(userId);

        // 组装 DTO
        return orders.stream().map(order -> {
            OrderDto dto = new OrderDto();
            dto.setId(order.getId());
            dto.setUserId(order.getUserId());
            dto.setUserName(user.getName());
            dto.setStatus(order.getStatus());
            dto.setCreateTime(order.getCreateTime());
            return dto;
        }).toList();
    }

    /**
     * 简单查询 — 不走远程调用，纯本地查询。
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
