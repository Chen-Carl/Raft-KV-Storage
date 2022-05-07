package com.zoecarl.concurr;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class RaftThreadPool {
    static class NameThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new RaftThread(r, "Raft thread");
            t.setDaemon(true);
            t.setPriority(5);
            return t;
        }
    }

    private static int core = Runtime.getRuntime().availableProcessors();
    private static int maximumPoolSize = core * 2;
    private static final int queueSize = 1024;
    private static final int keepAliveTime = 10000;
    private static TimeUnit keepTimeUnit = TimeUnit.MILLISECONDS;
    private static ThreadPoolExecutor executor = getThreadPoolExecutor();
    private static ScheduledExecutorService ss = getScheduled();

    public static RaftThreadPoolExecutor getThreadPoolExecutor() {
        return new RaftThreadPoolExecutor(core, maximumPoolSize, keepAliveTime, keepTimeUnit,
                new LinkedBlockingQueue<>(queueSize));
    }

    public static <T> Future<T> submit(Callable<T> r) {
        return executor.submit(r);
    }

    public static void shutdown() {
        executor.shutdown();
    }

    public static void execute(Runnable r, boolean sync) {
        if (sync) {
            r.run();
        } else {
            executor.execute(r);
        }
    }

    private static ScheduledExecutorService getScheduled() {
        return new ScheduledThreadPoolExecutor(core, new NameThreadFactory());
    }

    public static void scheduleWithFixedDelay(Runnable r, long delay) {
        ss.scheduleWithFixedDelay(r, 0, delay, TimeUnit.MILLISECONDS);
    }

    public static void scheduleAtFixedRate(Runnable r, long initDelay, long delay) {
        ss.scheduleAtFixedRate(r, initDelay, delay, TimeUnit.MILLISECONDS);
    }
}
