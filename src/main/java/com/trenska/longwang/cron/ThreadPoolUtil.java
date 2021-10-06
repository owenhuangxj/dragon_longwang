package com.trenska.longwang.cron;

import java.util.concurrent.*;

/**
 * 2019/11/23
 * 创建人:Owen
 */
public class ThreadPoolUtil {
	/**
	 * 存放线程池暂时不能执行的任务的队列
	 */
	private static final BlockingQueue<Runnable> WORK_QUEUE = new LinkedBlockingDeque(100);

	/**
	 * 创建新线程时使用的线程工厂
	 */
	private static final ThreadFactory THREAD_FACTORY = Executors.privilegedThreadFactory();
	/**
	 * corePoolSize: the number of threads to keep in the pool, even if they are idle, unless {@code
	 * allowCoreThreadTimeOut} is set 线程池中最小线程数量，即使这些线程处于空闲状态也不会被回收，除非 allowCoreThreadTimeOut 被设置为true。
	 * maximumPoolSize: the maximum number of threads to allow in the pool 线程池中允许的最多线程数。
	 * long keepAliveTime: when the number of threads is greater than the core,
	 * this is the maximum time that excess(多余的) idle threads will wait for new tasks before terminating.
	 * 当线程的数量大于内核时，多余的空闲线程在终止之前等待新任务的最大时间。
	 * TimeUnit unit: 多余线程存活时间
	 * BlockingQueue<Runnable> workQueue: the queue to use for holding tasks before they are executed.This
	 * queue will hold only the Runnable tasks submitted by the execute() method
	 * .存储等待执行的任务的队列。这个队列只会存储execute()方法提交的任务。
	 * ThreadFactory threadFactory: the factory to use when the executor creates a new thread.
	 * 线程执行器创建一个线程时使用的线程工厂
	 * RejectedExecutionHandler handler: A handler for tasks that cannot be executed by a
	 * ThreadPoolExecutor.当线程池不能执行一个任务时的处理器
	 */
	public static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(5, 20, 120,
			TimeUnit.SECONDS, WORK_QUEUE, Executors.defaultThreadFactory(), new RejectedExecutionHandler() {
		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

		}
	});

}