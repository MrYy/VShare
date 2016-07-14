package com.ANT.MiddleWare.PartyPlayerActivity.bean;

import android.util.Log;

/**
 * Created by zxc on 2016/7/8.
 */
public class StatisticsFactory {
    private static Statistics instanceGR;
    private static Statistics instanceWS;
    private static Statistics instanceWR;
    public static enum Type{
        gReceive,wifiSend,wifiReceive
    }

    public static  Statistics getInstance(Type type) {
        Statistics instance = null;
        switch (type) {
            case gReceive:
                if(instanceGR ==null) instanceGR = new Statistics();
                instance = instanceGR;
                break;
            case wifiSend:
                if(instanceWS == null) instanceWS = new Statistics();
                instance = instanceWS;
                break;
            case wifiReceive:
                if(instanceWR == null) instanceWR = new Statistics();
                instance = instanceWR;
                break;
        }
        return instance;
    }

    public static void startStatistic() {
        for (Type t : Type.values()) {
            getInstance(t).start();
        }
    }
}
