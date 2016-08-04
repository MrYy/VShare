package com.ANT.MiddleWare.PartyPlayerActivity.policy;

/**
 * Created by zxc on 2016/8/4.
 */
public interface ConnectionPolicy {
    //server,establish connection
    public void establish();
    //client ,connect to others
    public void connect();
    //view video pause
    public void pause();
    public void resume();
}
