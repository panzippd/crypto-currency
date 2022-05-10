package com.crypto.currency.common.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Panzi
 * @Description
 * @date 2022/5/10 21:44
 */
public class ThreadPoolFactory {

    private ThreadPoolFactory() {
    }

    private static class Singleton {
        private static final ThreadPoolFactory INSTANCE = new ThreadPoolFactory();
    }

    /**
     * 创建线程
     *
     * @param threadNameFormat
     * @param minPool
     * @param maxPool
     * @param keepAliveTime
     * @return
     */
    public ExecutorService createThreadPool(String threadNameFormat, int minPool, int maxPool, long keepAliveTime,
        int capacity) {

        ThreadFactory threadFactory =
            new ThreadFactoryBuilder().setDaemon(false).setNameFormat(threadNameFormat).build();
        return new ThreadPoolExecutor(minPool, maxPool, keepAliveTime, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue(capacity), threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    public static ThreadPoolFactory getInstance() {
        return Singleton.INSTANCE;
    }

}
