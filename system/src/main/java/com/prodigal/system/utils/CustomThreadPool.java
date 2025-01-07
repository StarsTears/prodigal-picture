package com.prodigal.system.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @program: prodigal-picture
 * @author: Lang
 * @description: 自定义线程池
 **/
@Slf4j
public class CustomThreadPool {

    public static ExecutorService createCustomThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit) {
        // 工作队列，用于存放待执行任务
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();

        // 创建自定义线程池
        ExecutorService threadPool = new ThreadPoolExecutor(
                corePoolSize, // 核心线程数
                maximumPoolSize, // 最大线程数
                keepAliveTime, // 非核心线程空闲存活时间
                unit, // 时间单位
                workQueue, // 工作队列
                new CustomThreadFactory(), // 线程工厂，用于创建线程
                new CustomRejectedExecutionHandler() // 拒绝策略，当任务太多，无法被线程池及时处理时的策略
        );

        return threadPool;
    }

    // 自定义线程工厂，用于创建线程
    static class CustomThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        CustomThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "CustomThreadPool-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY) t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    // 自定义拒绝策略
    static class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            // 处理任务拒绝情况，例如打印日志、抛出异常等
            log.error("Thread pool is exhausted, task: " + r.toString() + " will be rejected.");
            throw new RejectedExecutionException("Thread pool is exhausted, task: " + r.toString() + " will be rejected.");
        }
    }

    // 使用示例
    public static void main(String[] args) {
        // 创建自定义线程池
        ExecutorService threadPool = createCustomThreadPool(10, 50, 120, TimeUnit.SECONDS);

        // 提交任务到线程池
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            threadPool.execute(() -> {
                System.out.println("Executing task " + finalI + " in thread " + Thread.currentThread().getName());
            });
        }

        // 关闭线程池
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
        }
    }
}
