package com.example.user.repository;

import com.example.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 用户数据访问层。
 *
 * 为什么这是一个接口而不是类？
 * Spring Data JPA 会在运行时自动生成实现类（代理），
 * 你只需要声明方法签名，框架帮你生成 SQL。
 *
 * JpaRepository<User, Long>：
 *   第一个参数 User  → 操作的实体类型
 *   第二个参数 Long  → 主键类型
 *
 * 继承后自动获得的能力（不用写一行代码）：
 *   findById(id)   → SELECT * FROM users WHERE id = ?
 *   findAll()      → SELECT * FROM users
 *   save(user)     → INSERT INTO users ... 或 UPDATE
 *   deleteById(id) → DELETE FROM users WHERE id = ?
 *   count()        → SELECT COUNT(*) FROM users
 *
 * 你之前写过 OrderRepository，原理完全一样。
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 不需要写任何方法，基础的 CRUD JpaRepository 都提供了
}
