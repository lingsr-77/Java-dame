package com.example.demo.controller;

import com.example.demo.entity.Order;
import com.example.demo.repository.OrderRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // 按 status 查 → 没索引，慢
    @GetMapping("/by-status")
    public List<Order> byStatus(@RequestParam String status) {
        return orderRepository.findByStatus(status);
    }

    // 按 user_id 查 → 也没索引
    @GetMapping("/by-user")
    public List<Order> byUser(@RequestParam Long userId) {
        return orderRepository.findByUserId(userId);
    }

    // 查总数
    @GetMapping("/count")
    public String count() {
        return "总订单数：" + orderRepository.count();
    }
}
