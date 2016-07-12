package com.ANT.MiddleWare.PartyPlayerActivity.bean;

import android.util.Log;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by zxc on 2016/7/8.
 */
public class Statistics {
    private float totalSize;
    private static int startTime;
    private static Queue<Float> dSize;
    private static Queue<Long> dTime;
    private float currentSpeed;
    public Statistics() {
        startTime = (int) System.currentTimeMillis();
        dSize = new ArrayBlockingQueue<Float>(2);
        dTime = new ArrayBlockingQueue<Long>(2);
    }
    public static synchronized Statistics getInstance() {
        return new Statistics();
    }
    //add 传进来的是byte数
    public synchronized void add(int size) {
        totalSize += size;
//        dSize.add(totalSize);
//        dTime.add( System.currentTimeMillis());
//        if (dSize.size() == 2) {
//            currentSpeed = computeCurrentSpeed();
//        }
//        Log.d("TAG", String.valueOf(dSize.size()));
    }

    public synchronized float getSize() {
        return totalSize;
    }

    public synchronized float getSpeed() {
        int totalTime = (int) (System.currentTimeMillis() - startTime);
        return  totalSize / totalTime * 1000 * 8;
    }

    private float computeCurrentSpeed() {
        long smallT = dTime.poll();
        long bigT = dTime.poll();
        float smallSize = dSize.poll();
        float bigSize = dSize.poll();
        if(bigT == smallT) return 0;
//        Log.d("TAG", "small t/s" + String.valueOf(smallT) + " " + String.valueOf(smallSize) + " big t/s"
//                + String.valueOf(bigT) + " " + String.valueOf(bigSize));
//        Log.d("TAG", "time minus:" + String.valueOf(bigT - smallT));
        return (bigSize - smallSize) / (bigT - smallT)*1000;
    }

    //瞬时速度，仍未完成，后面再说
    public float getCurrentSpeed() {
        return Math.abs(currentSpeed);
    }
}
