package com.ANT.MiddleWare.PartyPlayerActivity.bean;

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
                if(instanceGR ==null) instanceGR = Statistics.getInstance();
                instance = instanceGR;
                break;
            case wifiSend:
                if(instanceWS == null) instanceWS = Statistics.getInstance();
                instance = instanceWS;
                break;
            case wifiReceive:
                if(instanceWR == null) instanceWR = Statistics.getInstance();
                instance = instanceWR;
                break;
        }
        return instance;
    }
}
