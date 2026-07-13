package com.example.order.service;

import com.example.order.entity.Order;
import com.example.order.repository.OrderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 启动时自动插入测试订单数据。
 *
 * 和之前的 DataInitializer 同理，确保数据库有测试数据。
 * 这里插 20 条，够测试用了（不用像之前 10 万条那样等）。
 */
@Component
public class OrderDataInitializer implements CommandLineRunner {

    private final OrderRepository orderRepository;

    public OrderDataInitializer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(String... args) {
        if (orderRepository.count() > 0) {
            System.out.println(">>> order-service: 订单数据已存在，当前共 " + orderRepository.count() + " 条");
            return;
        }

        System.out.println(">>> order-service: 插入测试订单数据...");
        String[] statuses = {"PENDING", "CONFIRMED", "SHIPPED", "CANCELLED"};
        Random random = new Random();
        List<Order> batch = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            batch.add(new Order(
                    (long) (random.nextInt(3) + 1),   // userId: 1, 2, 3（对应张三/李四/王五）
                    statuses[random.nextInt(statuses.length)],
                    LocalDateTime.now().minusDays(random.nextInt(30))
            ));
        }
        orderRepository.saveAll(batch);
        System.out.println(">>> order-service: 已插入 " + batch.size() + " 条测试订单");
    }
}
