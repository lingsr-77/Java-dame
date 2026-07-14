package com.example.order.task;

import com.example.order.alert.WeComAlertService;
import com.example.order.entity.Order;
import com.example.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * 订单对账定时任务 —— 模拟生产环境中"定时任务异常 → 重试 → 人工告警"的完整链路。
 *
 * 流程：
 * 1. @Scheduled 每 30 秒触发一次
 * 2. 通过自注入的代理对象调用 @Retryable 方法
 * 3. 如果失败 → 自动重试（最多 3 次，间隔 2 秒）
 * 4. 全部重试耗尽 → @Recover 发送企业微信告警
 *
 * 为什么要自注入（self）？
 *   @Retryable 基于 AOP 代理实现。同类内部调用（this.doReconciliation()）
 *   绕过了代理，重试不会生效。注入自身的代理对象（self.doReconciliation()）
 *   才能触发 Spring Retry 拦截器。
 */
@Component
public class OrderReconciliationTask {

    private static final Logger log = LoggerFactory.getLogger(OrderReconciliationTask.class);

    private final OrderRepository orderRepository;
    private final WeComAlertService weComAlertService;
    private final Random random = new Random();

    /**
     * 自注入：注入自己的 AOP 代理对象。
     * @Lazy 避免循环依赖：Spring 先创建原始 Bean，再注入代理。
     */
    @Lazy
    @Autowired
    private OrderReconciliationTask self;

    /**
     * 模拟故障率：0.0=永不失败，1.0=每次都失败。
     */
    @Value("${task.failure.rate:0.3}")
    private double failureRate;

    public OrderReconciliationTask(OrderRepository orderRepository,
                                   WeComAlertService weComAlertService) {
        this.orderRepository = orderRepository;
        this.weComAlertService = weComAlertService;
    }

    /**
     * 定时触发入口。
     * fixedDelay = 30 秒，表示上一次执行完成后等 30 秒再执行下一次。
     * initialDelay = 5 秒，启动后 5 秒就开始第一次执行，方便快速验证。
     */
    @Scheduled(fixedDelay = 30_000, initialDelay = 5_000)
    public void scheduleReconciliation() {
        log.info("===== 定时对账任务触发 =====");
        try {
            // ★ 关键：必须通过 self（代理对象）调用，@Retryable 才会生效
            self.doReconciliation();
            log.info("===== 定时对账任务完成 =====");
        } catch (Exception e) {
            log.error("对账任务最终失败，已发送告警: {}", e.getMessage());
        }
    }

    /**
     * 对账业务逻辑 —— 带自动重试。
     *
     * @Retryable 参数说明：
     *   retryFor = Exception.class    → 任何异常都重试（生产通常只重试特定异常）
     *   maxAttempts = 3               → 最多执行 3 次（1 次初始 + 2 次重试）
     *   backoff delay = 2000          → 两次重试之间等 2 秒（给下游恢复时间）
     *   label = "orderReconciliation" → 给这个重试器一个名字，方便监控和调试
     */
    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000),
            label = "orderReconciliation"
    )
    public String doReconciliation() {
        log.info("执行订单对账逻辑... (当前线程: {})", Thread.currentThread().getName());

        // ---- 真实业务：统计 PENDING 状态超过 1 小时的订单 ----
        List<Order> pendingOrders = orderRepository.findByStatus("PENDING");
        long stuckCount = pendingOrders.stream()
                .filter(o -> o.getCreateTime() != null
                        && o.getCreateTime().isBefore(LocalDateTime.now().minusHours(1)))
                .count();

        log.info("对账结果: PENDING 订单总数={}, 卡住超过1小时={}", pendingOrders.size(), stuckCount);

        // ---- 模拟故障：用于演示重试 + 告警 ----
        if (random.nextDouble() < failureRate) {
            throw new RuntimeException(
                    String.format("对账过程异常！(模拟故障, failureRate=%.0f%%)", failureRate * 100));
        }

        return "对账完成: 卡住订单=" + stuckCount;
    }

    /**
     * 所有重试耗尽后的降级处理 —— 发送企业微信告警。
     *
     * @Recover 方法的要求：
     *   1. 和 @Retryable 方法在同一个类中
     *   2. 返回值类型必须相同（这里是 String）
     *   3. 第一个参数是异常类型（所有重试耗尽时抛出的最终异常）
     *   4. 后续参数和 @Retryable 方法一致（这里没有参数）
     *
     * 生产环境中，这一步通常：
     *   1. 发企业微信/钉钉/飞书通知值班人员
     *   2. 写一条告警记录到数据库
     *   3. 触发 PagerDuty/OnCall 等 escalation 流程
     */
    @Recover
    public String handleReconciliationFailure(Exception e) {
        log.error("对账任务重试 3 次全部失败，触发人工告警！异常: {}", e.getMessage(), e);

        weComAlertService.sendAlert(
                "【告警】订单对账任务失败",
                "任务：订单对账定时任务\n" +
                        "异常：" + e.getMessage() + "\n" +
                        "重试次数：3 次已耗尽\n" +
                        "处理建议：检查数据库连接和订单服务状态，必要时手动执行对账"
        );

        // 告警已发送，但任务仍然失败，抛出异常让上层感知
        throw new RuntimeException("对账任务失败，已告警", e);
    }
}
