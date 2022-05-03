package com.zoecarl.concurr;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RaftThreadPoolExecutor extends ThreadPoolExecutor {
    private static final Logger logger = LoggerFactory.getLogger(RaftThreadPoolExecutor.class);
    private static final ThreadLocal<Long> startTime = ThreadLocal.withInitial(System::currentTimeMillis);

    public RaftThreadPoolExecutor(int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit unit,
            BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    @Override
    public void beforeExecute(Thread t, Runnable r) {
        long start = startTime.get();
        logger.info("Thread {} start at {}", t.getName(), start);
    }

    @Override
    public void afterExecute(Runnable r, Throwable t) {
        long start = startTime.get();
        long end = System.currentTimeMillis();
        logger.info("Thread {} end, consumed time = {}", t.getMessage(), end - start);
        startTime.remove();
    }

    @Override
    public void terminated() {
        logger.info("active count : {}, queueSize : {}, poolSize : {}", getActiveCount(), getQueue().size(), getPoolSize());
    }
}
