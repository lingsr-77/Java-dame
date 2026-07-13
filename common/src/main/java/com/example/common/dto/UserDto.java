package com.example.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户数据传输对象（DTO）。
 *
 * 为什么需要 DTO，而不是直接把数据库实体传出去？
 * 1. 安全：实体可能包含密码字段，DTO 只暴露安全的字段
 * 2. 解耦：数据库表结构变了，只需要改 Entity → DTO 的映射，不影响调用方
 * 3. 跨服务：微服务之间传的是 JSON，DTO 就是 "JSON 的 Java 表达"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String email;
}
