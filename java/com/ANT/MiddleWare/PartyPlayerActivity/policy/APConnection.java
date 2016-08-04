package com.ANT.MiddleWare.PartyPlayerActivity.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.ANT.MiddleWare.PartyPlayerActivity.R;
import com.ANT.MiddleWare.PartyPlayerActivity.ViewVideoActivity;
import com.ANT.MiddleWare.PartyPlayerActivity.util.Method;
import com.ANT.MiddleWare.WiFi.WiFiNCP2.Client;
import com.ANT.MiddleWare.WiFi.WiFiNCP2.ServerThread;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zxc on 2016/8/4.
 */
public class APConnection implements ConnectionPolicy {
    private static final String TAG = APConnection.class.getSimpleName();
       private WifiManager wifiManager;
   private Context context;
    private ArrayList<String> passableHotsPot;
    private WiFiReceiver wifiReceiver;
    private Process proc;
    private InetAddress serverAddr;

    public APConnection(Context context,WifiManager wifiManager) {
        this.wifiManager = wifiManager;
        this.context = context;
    }

    @Override
    public void establish() {
        wifiManager.setWifiEnabled(false);
        Method.changeApState(context, wifiManager, true);
        DhcpInfo info = wifiManager.getDhcpInfo();
        int serverAddress = info.ipAddress;
        InetAddress mAddr = Method.intToInetAddress(serverAddress);
        Log.d("ViewVideoActivity", mAddr.toString());
        new ServerThread(mAddr, context).start();
    }

    @Override
    public void connect() {
        Method.changeApState(context, wifiManager, false);
        wifiManager.setWifiEnabled(true);
        wifiManager.startScan();
        wifiReceiver = new WiFiReceiver();
        context.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }
    private final class WiFiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> wifiList;
            wifiList = wifiManager.getScanResults();
            if (wifiList == null || wifiList.size() == 0 || ViewVideoActivity.isConnected) return;
            onReceiveNewNetworks(wifiList);
        }
    }
    public void onReceiveNewNetworks(List<ScanResult> wifiList) {
        passableHotsPot = new ArrayList<String>();
        for (ScanResult result : wifiList) {
            System.out.println("wifi ssid is:"+result.SSID);
            if ((result.SSID).contains(context.getString(R.string.ap_ssid)))
                passableHotsPot.add(result.SSID);
        }
        synchronized (this) {
            connectToHotpot();
        }
    }
    private void connectToHotpot() {
        if (passableHotsPot == null || passableHotsPot.size() == 0)
            return;
        WifiConfiguration wifiConfig = setWifiParams(passableHotsPot.get(0));
        int wcgID = wifiManager.addNetwork(wifiConfig);
        boolean flag = wifiManager.enableNetwork(wcgID, true);
        ViewVideoActivity.isConnected = flag;
        if (!flag) return;
        context.unregisterReceiver(wifiReceiver);

        wifiManager.setWifiEnabled(false);
        try {
            proc = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(proc.getOutputStream());
            os.writeBytes("netcfg wlan0 up\n");
            os.writeBytes("wpa_supplicant -iwlan0 -c/data/misc/wifi/wpa_supplicant.conf -B\n");
            os.writeBytes("netcfg wlan0 dhcp\n");
            os.writeBytes("exit\n");
            os.flush();
            proc.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DhcpInfo info = wifiManager.getDhcpInfo();
        int serverAddress = info.serverAddress;
        serverAddr = Method.intToInetAddress(serverAddress);
        Log.d(TAG, "server's ip address:" + serverAddr);
        int mAddress = info.ipAddress;
        Log.d(TAG, "my ip address:" + Method.intToInetAddress(mAddress));

        try {
            //获取服务端ap地址存在问题
            new Thread(new Client(InetAddress.getByName("192.168.43.1"), 12345)).start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }
    private WifiConfiguration setWifiParams(String ssid) {
        WifiConfiguration apConfig = new WifiConfiguration();
        apConfig.SSID = "\"" + ssid + "\"";
        apConfig.preSharedKey = "\"" + context.getString(R.string.ap_password) + "\"";
        apConfig.hiddenSSID = true;
        apConfig.status = WifiConfiguration.Status.ENABLED;
        apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        return apConfig;
    }
}
