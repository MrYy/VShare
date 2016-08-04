package com.ANT.MiddleWare.PartyPlayerActivity.policy.directStatus;

import android.content.Context;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import com.ANT.MiddleWare.PartyPlayerActivity.R;
import com.ANT.MiddleWare.PartyPlayerActivity.util.Method;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by zxc on 2016/8/4.
 */
public class IsClient implements Status {
    private static final String TAG = "WIFI DIRECT";
    private WifiP2pDevice targetDevice;
    private Context context;
    private List<WifiP2pDevice> peers = new ArrayList<>();
    private WifiP2pManager wifiManager;
    private WifiP2pManager.Channel wifichannel;
    public IsClient(Context context, WifiP2pManager wifiManager, WifiP2pManager.Channel wifichannel) {
        this.context = context;
        this.wifichannel = wifichannel;
        this.wifiManager = wifiManager;
    }

    @Override
    public void supportWifiDirect() {
        wifiManager.discoverPeers(wifichannel, searchListener);
    }
    private WifiP2pManager.ActionListener searchListener = new WifiP2pManager.ActionListener() {
        @Override
        public void onSuccess() {
            Log.d(TAG, "search success");
        }
        @Override
        public void onFailure(int i) {
            Log.d(TAG, "search fail");
            try {
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            wifiManager.discoverPeers(wifichannel, searchListener);
        }
    };
    @Override
    public void findPeers() {
        wifiManager.requestPeers(wifichannel, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                if (isConnected){
                    wifiManager.stopPeerDiscovery(wifichannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                        }
                        @Override
                        public void onFailure(int i) {
                        }
                    });
                    return;
                }
                Collection<WifiP2pDevice> aList = wifiP2pDeviceList.getDeviceList();
                peers.addAll(aList);
                for (WifiP2pDevice device : peers) {
                    Log.d(TAG, "device name:"+String.valueOf(device.deviceName)+" is owner "+String.valueOf(device.isGroupOwner())+" ip:"+String.valueOf(device.deviceAddress));
                    if (device.deviceName.equals(context.getString(R.string.ssid)) && device.isGroupOwner()) {
                        //connect to the owner;
                        targetDevice = device;
                        connect();
                    }
                }
            }
        });
    }

    @Override
    public void connectSuccess() {

    }

    private void connect() {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = targetDevice.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        wifiManager.connect(wifichannel, config,connectionLisener);
    }

    private boolean isConnected;
    private WifiP2pManager.ActionListener connectionLisener =  new WifiP2pManager.ActionListener() {

        @Override
        public void onSuccess() {
            // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            Log.d(TAG, "connect success");
            isConnected = true;
        }

        @Override
        public void onFailure(int reason) {
            Log.d(TAG, "connect fail");
            Method.display(context,"连接失败，正在重试");
            try {
                TimeUnit.MILLISECONDS.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            connect();
        }
    };
}
