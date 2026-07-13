package com.example.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 订单数据传输对象。
 *
 * 注意 userName 字段：订单表里只有 userId（外键），没有 userName。
 * 但给前端展示时需要显示用户名，所以这里多了一个 userName 字段，
 * 它是由后端通过 Feign 调用 user-service 填充的。
 * 这就是微服务中 "数据聚合/编排" 的典型场景。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private Long userId;
    private String userName;   // 来自 user-service，不是 order-service 自己的数据
    private String status;
    private LocalDateTime createTime;
}
