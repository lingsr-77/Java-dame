package com.example.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 用户实体 —— 对应数据库中的 users 表。
 *
 * @Entity：告诉 JPA "这是一个数据库实体，需要映射到表"
 * @Table(name = "users")：指定表名（不写则默认类名小写 = user）
 * @Data：Lombok 自动生成 getter/setter/toString/equals/hashCode
 * @NoArgsConstructor：JPA 要求实体必须有无参构造器（框架反射用）
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * @Id：主键
     * @GeneratedValue(strategy = IDENTITY)：主键自增（AUTO_INCREMENT）
     *   相当于你在 SQL 里写的 `id BIGINT AUTO_INCREMENT PRIMARY KEY`
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @Column(nullable = false)：NOT NULL 约束
     * 不加 @Column 的话，JPA 会用默认规则（字段名下划线转换，可为空）
     */
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;
}
