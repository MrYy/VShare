package com.ANT.MiddleWare.PartyPlayerActivity.policy;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;

/**
 * Created by zxc on 2016/8/4.
 */
public class WifiDirectConnection implements ConnectionPolicy {
    private final WifiP2pManager wifiManager;
    private final WifiP2pManager.Channel wifichannel;
    private WiFiServerBroadcastReceiver wifiServerReceiver;
    private IntentFilter wifiServerReceiverIntentFilter;
    private Context context;

    public WifiDirectConnection(Context context) {
        this.context = context;
        wifiManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        wifichannel = wifiManager.initialize(context, Looper.getMainLooper(), null);
    }

    @Override
    public void establish() {
        wifiServerReceiver = new WiFiServerBroadcastReceiver(wifiManager, wifichannel, context);
        wifiServerReceiverIntentFilter = new IntentFilter();
        wifiServerReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiServerReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiServerReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiServerReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        context.registerReceiver(wifiServerReceiver, wifiServerReceiverIntentFilter);

    }

    @Override
    public void connect() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }


}
