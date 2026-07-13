package com.example.order.repository;

import com.example.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 按用户 ID 查询该用户的所有订单。
     *
     * Spring Data JPA 会解析方法名 findByUserId，
     * 自动生成 SQL：SELECT * FROM orders WHERE user_id = ?
     *
     * 命名规则：find + By + 字段名（驼峰转下划线）
     * 这就是你之前做慢查询分析时看到的那个 SQL。
     */
    List<Order> findByUserId(Long userId);
}
