package com.ANT.MiddleWare.WiFi.WiFiNCP2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import com.ANT.MiddleWare.PartyPlayerActivity.R;
import com.ANT.MiddleWare.PartyPlayerActivity.util.Method;
import com.ANT.MiddleWare.WiFi.WiFiPulic;

import java.net.InetAddress;
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
    private static Context context = null;
    public WiFiNCP2(Context contect) {
        super(contect);
        context = contect;
        dumpUnil = new DumpUnil(contect);
        manager = (WifiManager) contect.getSystemService(Context.WIFI_SERVICE);

        Method.changeApState(contect,manager,false);
        manager.setWifiEnabled(true);
        manager.startScan();

        wifiReceiver = new WiFiReceiver();
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
            DhcpInfo info = manager.getDhcpInfo();
            int serverAddress = info.ipAddress;
            InetAddress serverAddr = Method.intToInetAddress(serverAddress);
            Log.d(TAG, "server is listening:" + serverAddr.toString());
            if (context != null) {
                new ServerThread(serverAddr, context).start();
            }
        }
    }

    public void onReceiveNewNetworks(List<ScanResult> wifiList) {
        passableHotsPot = new ArrayList<String>();
        for (ScanResult result : wifiList) {
            System.out.println("wifi ssid is:"+result.SSID);
            if ((result.SSID).contains(contect.getString(R.string.ssid)))
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
        DhcpInfo info = manager.getDhcpInfo();

        int serverAddress = info.serverAddress;
        Toast.makeText(contect,"connect to ap",Toast.LENGTH_SHORT).show();
        InetAddress serverAddr = Method.intToInetAddress(serverAddress);
        Log.d(TAG, "server's ip address:"+serverAddr);
        new Thread(new Client(serverAddr, 12345)).start();
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
