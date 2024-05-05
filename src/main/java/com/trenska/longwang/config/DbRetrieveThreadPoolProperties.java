package com.trenska.longwang.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**线程池参数
 * <p>
 * corePoolSize: the number of threads to keep in the pool, even if they are idle,
 * unless {@code allowCoreThreadTimeOut} is set
 * <p>
 * 线程池中最小线程数量，即使这些线程处于空闲状态也不会被回收，除非allowCoreThreadTimeOut被设置为true。
 * <p>
 * maximumPoolSize: the maximum number of threads to allow in the pool 线程池中允许的最多线程数。
 * <p>
 * keepAliveTime: when the number of threads is greater than the core,
 * this is the maximum time that excess(多余的) idle threads will wait for new tasks before terminating.
 * 当线程的数量大于内核时，多余的空闲线程在终止之前等待新任务的最大时间。
 * <p>
 * TimeUnit unit: 多余线程存活时间
 * <p>
 * BlockingQueue<Runnable> workQueue: the queue to use for holding tasks before they are executed.This
 * queue will hold only the Runnable tasks submitted by the execute() method
 * <p>
 * 存储等待执行的任务的队列。这个队列只会存储execute()方法提交的任务。
 * <p>
 * ThreadFactory threadFactory: the factory to use when the executor creates a new thread.
 * <p>
 * 线程执行器创建一个线程时使用的线程工厂
 * <p>
 * RejectedExecutionHandler handler: A handler for tasks that cannot be executed by a
 * ThreadPoolExecutor.当线程池不能执行一个任务时的处理器
 */
@Data
@Component
@ConfigurationProperties(prefix = "thread.pool.db.retrieve")
public class DbRetrieveThreadPoolProperties {
    @Value("${corePoolSize:3}")
    private int corePoolSize;
    @Value("${maximumPoolSize:5}")
    private int maximumPoolSize;
    @Value("${keepAliveTime:1}")
    private long keepAliveTime;
    @Value("${blockingQueueCapacity:3}")
    private int blockingQueueCapacity;
    @Value("${threadNamePrefix:DB-RETRIEVE-}")
    private String threadNamePrefix;
}