package com.example.demo;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * DealyTask 延迟队列测试类
 * 模拟 5 个任务，每个任务随机延迟时间，通过 DelayQueue 按到期时间排序执行
 */
class DealyTaskTest {

    @Test
    void testDelayQueue() {
        // DelayQueue：无界阻塞队列，元素按到期时间排序，只有到期后才能取出
        DelayQueue<DealyTask<String>> queue = new DelayQueue<>();

        // ThreadLocalRandom：线程安全的随机数生成器，避免多线程竞争
        ThreadLocalRandom random = ThreadLocalRandom.current();

        System.out.println("=== 提交任务（每个任务随机延迟 100~600ms）===");

        // 提交 5 个任务，每个设置随机延迟
        for (int i = 1; i <= 5; i++) {
            // 随机生成 100~600ms 的延迟时长
            long delayMs = random.nextLong(100, 601);
            // Duration.ofMillis() 将毫秒数转为 Duration 对象，传入构造方法
            queue.put(new DealyTask<>("任务" + i, Duration.ofMillis(delayMs)));
            System.out.println("任务" + i + " 已提交，延迟 " + delayMs + " ms");
        }

        System.out.println("\n=== 执行结果（按到期时间排序，延迟短的先执行）===");

        // 循环取出 5 个已到期的任务
        for (int i = 0; i < 5; i++) {
            try {
                // take()：阻塞等待，直到队列中有任务到期才返回
                // 返回的是队列中到期时间最早的任务（二叉堆堆顶）
                DealyTask<String> task = queue.take();
                System.out.println(task.getDate() + " 已执行");
            } catch (InterruptedException e) {
                // 如果当前线程被中断，恢复中断状态并退出
                Thread.currentThread().interrupt();
                break;
            }
        }

        // 说明：DelayQueue 内部使用 PriorityQueue（二叉堆），
        // compareTo() 决定了排序规则 — 剩余延迟越短越靠前。
        // 因此延迟最短的任务最先被 take() 取出来执行。
        // 示例输出可能是：任务3 → 任务5 → 任务1 → 任务4 → 任务2
        // （顺序取决于随机生成的延迟长短）
    }

    @Test
    void testSimple() throws InterruptedException {
        // 创建延迟队列
        DelayQueue<DealyTask<String>> queue = new DelayQueue<>();

        // 直接创建 3 个任务，分别传入不同的延迟时长
        DealyTask<String> task1 = new DealyTask<>("任务A", Duration.ofMillis(300));
        DealyTask<String> task2 = new DealyTask<>("任务B", Duration.ofMillis(100));
        DealyTask<String> task3 = new DealyTask<>("任务C", Duration.ofMillis(200));

        // 放入队列（顺序无关，DelayQueue 会按到期时间自动排序）
        queue.put(task1);
        queue.put(task2);
        queue.put(task3);

        System.out.println("已提交：任务A(300ms)  任务B(100ms)  任务C(200ms)");
        System.out.println("执行顺序：");

        // 按到期时间依次取出执行 — 延迟短的先出队
        System.out.println(queue.take().getDate()); // 100ms 最先到期
        System.out.println(queue.take().getDate()); // 200ms
        System.out.println(queue.take().getDate()); // 300ms 最后到期
    }

    /**
     * 多线程并发执行测试：模拟高并发下大量任务同时到期，验证线程池并行处理能力
     *
     * 与 testSimple 的区别：
     *   testSimple 是单线程阻塞取出 → 顺序执行
     *   本测试是消费者线程快速取出 → 线程池多 worker 并行执行业务逻辑
     */
    @Test
    void testConcurrentExecution() throws InterruptedException {
        // 核心4线程 / 最大8线程 / 线程池队列容量50（有界，防止 OOM）
        DelayedTaskExecutor executor = new DelayedTaskExecutor(4, 8, 50);

        int taskCount = 20;
        System.out.println("=== 提交 " + taskCount + " 个任务（延迟 50~150ms）===");

        // 提交大量任务，模拟高并发场景
        for (int i = 1; i <= taskCount; i++) {
            // 所有任务在 50~150ms 内密集到期，模拟"瞬间大量到期"
            long delayMs = ThreadLocalRandom.current().nextLong(50, 151);
            DealyTask<String> task = new DealyTask<>("任务" + i, Duration.ofMillis(delayMs));
            executor.submit(task, data -> {
                // 模拟业务处理耗时（如 Redis 查询 + 磁盘写入 ~10ms）
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
                // 打印执行线程名，展示多线程并行效果
                System.out.println(data + " 已执行 [线程: " + Thread.currentThread().getName() + "]");
            });
        }

        System.out.println("任务全部提交完成，等待到期...\n");

        // 等待所有任务执行完毕
        Thread.sleep(2000);

        // 优雅关闭：消费者线程 + 线程池
        executor.shutdown();
        System.out.println("\n=== 执行完毕 ===");

        // 观察输出中的 [线程: delay-worker-X]，X 会出现在 1~8 之间，
        // 说明多个线程在并行处理不同任务，而非单线程顺序执行。
    }
}