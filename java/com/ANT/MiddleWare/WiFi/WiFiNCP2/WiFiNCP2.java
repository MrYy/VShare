package com.ANT.MiddleWare.WiFi.WiFiNCP2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.ANT.MiddleWare.PartyPlayerActivity.MainFragment;
import com.ANT.MiddleWare.PartyPlayerActivity.R;
import com.ANT.MiddleWare.WiFi.WiFiPulic;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class WiFiNCP2 extends WiFiPulic {
    private static final String TAG = WiFiNCP2.class.getSimpleName();
    private final DumpUnil dumpUnil;
    private final WiFiReceiver wifiReceiver;
    private static WifiManager manager = null;
    private List<ScanResult> wifiList;
    private ArrayList<String> passableHotsPot;
    private boolean isConnected = false;

    public WiFiNCP2(Context contect) {
        super(contect);
        dumpUnil = new DumpUnil(contect);
        manager = (WifiManager) contect.getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WiFiReceiver();
        manager.startScan();
        manager.setWifiEnabled(true);
        contect.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }
    private final class WiFiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            wifiList = manager.getScanResults();
            if (wifiList == null || wifiList.size() == 0 || isConnected) return;
            onReceiveNewNetworks(wifiList);
        }
    }

    public static class ApObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            //this one is ap

        }
    }

    public void onReceiveNewNetworks(List<ScanResult> wifiList) {
        passableHotsPot = new ArrayList<String>();
        for (ScanResult result : wifiList) {
            System.out.println(result.SSID);
            if ((result.SSID).contains(contect.getString(R.string.ap_ssid)))
                passableHotsPot.add(result.SSID);
        }
        synchronized (this) {
            connectToHotpot();
        }
    }

    public void connectToHotpot() {
        if (passableHotsPot == null || passableHotsPot.size() == 0)
            return;
        WifiConfiguration wifiConfig = setWifiParams(passableHotsPot.get(0));
        int wcgID = manager.addNetwork(wifiConfig);
        boolean flag = manager.enableNetwork(wcgID, true);
        isConnected = flag;

        contect.unregisterReceiver(wifiReceiver);
        System.out.println("connect success? " + flag);
    }

    public WifiConfiguration setWifiParams(String ssid) {
        WifiConfiguration apConfig = new WifiConfiguration();
        apConfig.SSID = "\"" + ssid + "\"";
        apConfig.preSharedKey = "\"" + contect.getString(R.string.ap_password) + "\"";
        apConfig.hiddenSSID = true;
        apConfig.status = WifiConfiguration.Status.ENABLED;
        apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        return apConfig;
    }




    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    @Override
    public void notify(int seg, int start) {
        // TODO Auto-generated method stub

    }

    @Override
    public void EmergencySend(byte[] data) {
        // TODO Auto-generated method stub

    }
}
