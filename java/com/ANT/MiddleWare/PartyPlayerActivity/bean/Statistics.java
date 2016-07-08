package com.ANT.MiddleWare.PartyPlayerActivity.bean;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zxc on 2016/7/8.
 */
public class Statistics {
    private int totalSize;
    private static int startTime;

    public static synchronized Statistics getInstance() {
        startTime = (int) System.currentTimeMillis();
        return new Statistics();
    }

    public synchronized void add(int size) {
        totalSize += size;
    }

    public synchronized int getSize() {
        return totalSize;
    }

    public synchronized float getSpeed() {
        int totalTime = (int) (System.currentTimeMillis() - startTime);
        return (float) totalSize / totalTime * 1000 * 8;
    }
}
