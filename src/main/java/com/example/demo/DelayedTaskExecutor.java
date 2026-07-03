package com.example.demo;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 延迟任务执行引擎
 *
 * 架构：
 *   DelayQueue（按到期时间排序）
 *     → 单消费者线程（take 阻塞取到期任务，极轻量）
 *     → 自定义线程池（并行执行业务逻辑）
 *
 * 背压策略：线程池有界队列 + CallerRunsPolicy，
 * 池满时消费者线程自己执行，不再从 DelayQueue 取新任务，自然降速。
 */
public class DelayedTaskExecutor {

    private final DelayQueue<TaskEntry<?>> delayQueue = new DelayQueue<>();
    private final ThreadPoolExecutor threadPool;
    private final Thread consumerThread;
    private final AtomicInteger workerCounter = new AtomicInteger(0);

    /**
     * @param corePoolSize 核心线程数
     * @param maxPoolSize  最大线程数
     * @param poolQueueSize 线程池等待队列容量（有界，防止无限堆积）
     */
    public DelayedTaskExecutor(int corePoolSize, int maxPoolSize, int poolQueueSize) {
        this.threadPool = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(poolQueueSize),           // 有界队列，限制内存占用
                r -> {
                    String name = "delay-worker-" + workerCounter.incrementAndGet();
                    return new Thread(r, name);
                },                                                 // 带编号的线程名，便于观察多线程并行效果
                new ThreadPoolExecutor.CallerRunsPolicy()           // 池满时消费者线程自己执行，形成背压
        );

        this.consumerThread = new Thread(this::consumeLoop, "delay-consumer");
        this.consumerThread.setDaemon(true);
        this.consumerThread.start();
    }

    /**
     * 提交延迟任务
     * @param task   延迟任务（携带到期时间和数据）
     * @param handler 到期后的业务处理逻辑
     */
    public <D> void submit(DealyTask<D> task, Consumer<D> handler) {
        delayQueue.put(new TaskEntry<>(task, handler));
    }

    /**
     * 消费者循环：阻塞等待到期任务 → 提交到线程池
     */
    private void consumeLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // 阻塞等待最早到期的任务
                TaskEntry<?> entry = delayQueue.take();
                // 提交到线程池执行（池满时 CallerRunsPolicy 让当前线程自己跑）
                threadPool.execute(entry);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * 优雅关闭：先停消费者，再等线程池中的任务跑完
     */
    public void shutdown() {
        consumerThread.interrupt();                    // 停止消费者循环
        threadPool.shutdown();                         // 不再接收新任务
        try {
            if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();              // 超时强制终止
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 内部包装类：将 DealyTask 的到期时间信息 + 业务 handler 绑定为一个 Entry
     * 同时实现 Delayed（委托给 DealyTask）和 Runnable（handler.accept）
     */
    private static class TaskEntry<D> implements Delayed, Runnable {
        private final DealyTask<D> task;
        private final Consumer<D> handler;

        TaskEntry(DealyTask<D> task, Consumer<D> handler) {
            this.task = task;
            this.handler = handler;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return task.getDelay(unit);
        }

        @Override
        public int compareTo(Delayed o) {
            return task.compareTo(o);
        }

        @Override
        public void run() {
            handler.accept(task.getDate());
        }
    }
}