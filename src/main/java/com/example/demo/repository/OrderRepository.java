package com.example.demo.repository;

import com.example.demo.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 故意不加索引的查询：按 status 和 user_id 查
    List<Order> findByStatus(String status);

    List<Order> findByUserId(Long userId);
}
