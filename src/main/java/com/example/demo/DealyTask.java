package com.example.demo; // 声明该类所属的包路径

import lombok.Data; // 引入 Lombok 的 @Data 注解，自动生成 getter/setter/toString/equals/hashCode 等方法

import java.time.Duration;
import java.util.concurrent.Delayed; // 引入 Delayed 接口，用于标记可延迟执行的任务
import java.util.concurrent.TimeUnit; // 引入 TimeUnit 枚举，用于时间单位转换

/**
 * 延迟任务类
 * 实现 Delayed 接口，可放入 DelayQueue 中按延迟时间排序取出
 * @param <D> 泛型，表示任务携带的数据类型
 */
@Data // Lombok 注解：编译时自动生成所有字段的 getter/setter，以及 toString、equals、hashCode 方法
public class DealyTask<D> implements Delayed { // 实现 Delayed 接口，使该任务可在 DelayQueue 中使用

    private D date; // 泛型字段，存储任务关联的业务数据

    private long deadlineNanos; // 任务的到期时间，以纳秒为单位（相对于 System.nanoTime() 的绝对时间点）

    /**
     * 构造方法
     * @param date 任务携带的业务数据
     * @param dealyTime 相对延迟时长（Duration 类型），构造时自动转换为绝对到期时间戳
     */
    public DealyTask(D date, Duration dealyTime) {
        this.date = date; // 保存业务数据
        // 调用方只需传入"多久后到期"，由构造方法内部换算为绝对纳秒时间戳
        this.deadlineNanos = System.nanoTime() + dealyTime.toNanos();
    }

    /**
     * 获取剩余延迟时间
     * DelayQueue 通过此方法判断任务是否到期（返回值 <= 0 表示已到期）
     * @param unit 期望返回的时间单位
     * @return 剩余延迟时间，已转换为指定单位；如果已到期则返回 0
     */
    @Override
    public long getDelay(TimeUnit unit) {
        // deadlineNanos - System.nanoTime() 计算剩余纳秒数
        // Math.max(0, ...) 确保已到期的任务不会返回负数，避免影响队列行为
        // unit.convert(..., TimeUnit.NANOSECONDS) 将纳秒转换为调用者指定的时间单位
        return unit.convert(Math.max(0, deadlineNanos - System.nanoTime()), TimeUnit.NANOSECONDS);
    }

    /**
     * 比较两个延迟任务的优先级
     * DelayQueue 内部使用二叉堆（PriorityQueue）排序，需要此方法确定任务顺序
     * @param o 另一个 Delayed 任务
     * @return 正数表示当前任务延迟更长，负数表示更短，0 表示相等
     */
    @Override
    public int compareTo(Delayed o) {
        // 以纳秒为单位计算两个任务的剩余延迟差值
        long l = getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);

        // 三元嵌套运算符：将 long 差值映射为 int 的比较结果
        // l > 0 → 返回 1（当前任务延迟更长，排在后面）
        // l < 0 → 返回 -1（当前任务延迟更短，排在前面）
        // 否则 l == 0 → 返回 0（两个任务同时到期）
        return l > 0 ? 1 : l < 0 ? -1 : 0;
    }
}