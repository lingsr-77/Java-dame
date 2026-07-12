package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TroubleshootController {

    // 死循环：访问 CPU 飙到 100%
    @GetMapping("/dead-loop")
    public String deadLoop() {
        long sum = 0;
        while (true) {
            sum += 1;
        }
    }

    // 死锁：两个线程互相等对方的锁
    private final Object lockA = new Object();
    private final Object lockB = new Object();

    @GetMapping("/dead-lock")
    public String deadLock() {
        new Thread(() -> {
            synchronized (lockA) {
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                synchronized (lockB) {
                    System.out.println("Thread-1 got both locks");
                }
            }
        }, "DeadlockThread-1").start();

        new Thread(() -> {
            synchronized (lockB) {
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                synchronized (lockA) {
                    System.out.println("Thread-2 got both locks");
                }
            }
        }, "DeadlockThread-2").start();

        return "Deadlock created. Run: jstack -l <PID>";
    }
}
