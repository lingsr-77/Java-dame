package com.example.order.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 订单实体 —— 对应数据库中的 orders 表（和之前的一样）。
 *
 * 注意：订单表里存的是 userId（谁下的单），但不存在 userName。
 * 要显示"订单属于哪个用户"，就必须远程调用 user-service 来获取用户名。
 * 这就是微服务中"数据分散在各服务中"的体现。
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "status")
    private String status;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    public Order(Long userId, String status, LocalDateTime createTime) {
        this.userId = userId;
        this.status = status;
        this.createTime = createTime;
    }
}
