package com.trenska.longwang.cron;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.ehcache.impl.internal.util.ThreadFactoryUtil;

import java.util.concurrent.*;

/**
 * 2019/11/23
 * 创建人:Owen
 */
public class ThreadPoolUtil {


	/**
	 * int corePoolSize,
	 * int maximumPoolSize,
	 * long keepAliveTime,
	 * TimeUnit unit,
	 * BlockingQueue<Runnable> workQueue,
	 * ThreadFactory threadFactory,
	 * RejectedExecutionHandler handler
	 */
//	public static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor()

	public static final BlockingQueue<Runnable> WORK_QUEUE = new LinkedBlockingDeque(100);


}
