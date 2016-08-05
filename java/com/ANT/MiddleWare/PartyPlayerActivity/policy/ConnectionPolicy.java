package com.ANT.MiddleWare.PartyPlayerActivity.policy;

/**
 * Created by zxc on 2016/8/4.
 */
public interface ConnectionPolicy {
    //server,establish connection
    void establish();

    //client ,connect to others
    void connect();

    //view video pause
    void pause();

    void resume();

    void die();
}
