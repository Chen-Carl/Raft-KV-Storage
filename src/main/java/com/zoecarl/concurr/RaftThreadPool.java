package com.zoecarl.concurr;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;

public class RaftThreadPool {
    private static int core = Runtime.getRuntime().availableProcessors();
    private static int maximumPoolSize = core * 2;
    private static final int queueSize = 1024;
    private static final int keepAliveTime = 60000;
    private static TimeUnit keepTimeUnit = TimeUnit.MILLISECONDS;
    private static ThreadPoolExecutor executor = getThreadPoolExecutor();

    private static ThreadPoolExecutor getThreadPoolExecutor() {
        return new RaftThreadPoolExecutor(core, maximumPoolSize, keepAliveTime, keepTimeUnit, new LinkedBlockingQueue<>(queueSize));
    }

    public static <T> Future<T> submit(Callable<T> r) {
        return executor.submit(r);
    }
}
