package com.trenska.longwang.config;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * 数据库查询线程池
 */
@Slf4j
public class DbRetrieveThreadPool {
    private ThreadPoolExecutor threadPoolExecutor;

    public DbRetrieveThreadPool(DbRetrieveThreadPoolProperties properties) {
        this.threadPoolExecutor = new ThreadPoolExecutor(properties.getCorePoolSize(),
                properties.getMaximumPoolSize(), properties.getKeepAliveTime(), TimeUnit.SECONDS
                , new ArrayBlockingQueue<>(properties.getBlockingQueueCapacity()), runnable -> {
            Thread thread = new Thread(runnable,
                    properties.getThreadNamePrefix().concat(Thread.currentThread().getName()));
            log.info("Created thread name: {}", thread.getName());
            return thread;
        },
                (runnable, executor) -> {
                    throw new RejectedExecutionException("Task " + runnable.toString() + " rejected from " + executor.toString());
                });
    }

    /**
     * 执行一个没有返回值的异步任务
     *
     * @param runnable 异步任务
     * @return CompletableFuture对象
     */
    public CompletableFuture<Void> runAsync(Runnable runnable) {
        log.info("{} processing the business progress>>>", Thread.currentThread().getName());
        return CompletableFuture.runAsync(runnable, threadPoolExecutor);
    }

    /**
     * 执行一个返回值类型为U的异步任务
     *
     * @param supplier 异步任务
     * @param <U>      返回值类型
     * @return 携带返回值的Completable对象
     */
    public <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
        return CompletableFuture.supplyAsync(supplier, threadPoolExecutor);
    }
}