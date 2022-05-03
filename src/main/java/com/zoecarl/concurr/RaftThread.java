package com.zoecarl.concurr;

public class RaftThread extends Thread {
    private static final UncaughtExceptionHandler exceptHandler = new UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            
        }
    };

    public RaftThread(Runnable cb, String threadName) {
        super(cb, threadName);
        setUncaughtExceptionHandler(exceptHandler);
    }
}
