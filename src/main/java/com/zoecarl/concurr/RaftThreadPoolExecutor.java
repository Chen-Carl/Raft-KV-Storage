package com.zoecarl.concurr;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class RaftThreadPoolExecutor extends ThreadPoolExecutor {
    private static final Logger logger = LogManager.getLogger(RaftThreadPoolExecutor.class);
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
        logger.debug("Thread {} start", t.getName());
    }

    @Override
    public void afterExecute(Runnable r, Throwable t) {
        long start = startTime.get();
        long end = System.currentTimeMillis();
        logger.debug("Runnable {} end, consumed time = {}", r.getClass(), end - start);
        startTime.remove();
    }

    @Override
    public void terminated() {
        logger.debug("active count : {}, queueSize : {}, poolSize : {}", getActiveCount(), getQueue().size(), getPoolSize());
    }
}
