package com.ANT.MiddleWare.PartyPlayerActivity.policy.directStatus;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by zxc on 2016/8/4.
 */
public class IsClient implements Status {
    private static final String TAG = "WIFI DIRECT";

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
        wifiManager.discoverPeers(wifichannel,actionListener );
    }
    private WifiP2pManager.ActionListener actionListener = new WifiP2pManager.ActionListener() {
        @Override
        public void onSuccess() {
            Log.d(TAG, "start search");
        }
        @Override
        public void onFailure(int i) {
            Log.d(TAG, "search fail");
            wifiManager.discoverPeers(wifichannel,actionListener);
        }
    };
    @Override
    public void findPeers() {
        wifiManager.requestPeers(wifichannel, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                Collection<WifiP2pDevice> aList = wifiP2pDeviceList.getDeviceList();
                peers.addAll(aList);
                for (WifiP2pDevice device : peers) {
                    Log.d(TAG, "device name:"+String.valueOf(device.deviceName)+"is owner"+String.valueOf(device.isGroupOwner()));

                }
            }
        });
    }
}
