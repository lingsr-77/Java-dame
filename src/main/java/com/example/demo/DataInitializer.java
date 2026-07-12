package com.example.demo;

import com.example.demo.entity.Order;
import com.example.demo.repository.OrderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DataInitializer implements CommandLineRunner {

    private final OrderRepository orderRepository;

    public DataInitializer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(String... args) {
        if (orderRepository.count() > 0) {
            System.out.println("数据已存在，跳过初始化。当前订单数：" + orderRepository.count());
            return;
        }

        System.out.println("开始插入 10 万条测试数据...");
        Random random = new Random();
        String[] statuses = {"PENDING", "CONFIRMED", "SHIPPED", "CANCELLED"};
        List<Order> batch = new ArrayList<>(1000);

        for (int i = 0; i < 100_000; i++) {
            batch.add(new Order(
                (long) random.nextInt(1000),
                statuses[random.nextInt(statuses.length)],
                LocalDateTime.now().minusDays(random.nextInt(365))
            ));

            if (batch.size() >= 1000) {
                orderRepository.saveAll(batch);
                batch.clear();
                if (i % 10000 == 0) System.out.println("已插入: " + i);
            }
        }

        if (!batch.isEmpty()) {
            orderRepository.saveAll(batch);
        }
        System.out.println("插入完成。订单总数：" + orderRepository.count());
    }
}
