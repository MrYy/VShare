package com.ANT.MiddleWare.PartyPlayerActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.ANT.MiddleWare.DASHProxyServer.DashProxyServer;
import com.ANT.MiddleWare.WiFi.WiFiNCP2.Client;
import com.ANT.MiddleWare.WiFi.WiFiNCP2.ServerThread;
import com.ANT.MiddleWare.WiFi.WiFiTCP.Method;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ViewVideoActivity extends FragmentActivity  {
    private static final String TAG = ViewVideoActivity.class.getSimpleName();
    private Button buttonView;
    private EditText editTextLocation;
    private DashProxyServer server = new DashProxyServer();
    public static ConfigureData configureData = new ConfigureData(null);
    private Switch switchButton;
    private WifiManager wifiManager;
    private Process proc;
    private ArrayList<String> passableHotsPot;
    private Timer mTimer;
    private static final String CMD_GET_WPA = "cat data/misc/wifi/wpa_supplicant.conf\n";
    private final String file_path = "data/misc/wifi/wpa_supplicant.conf";
    private List<ScanResult> wifiList;
    private boolean isConnected = false;
    private WiFiReceiver wifiReceiver;
    private InetAddress serverAddr;
    private InetAddress mAddr;

    private final class WiFiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            wifiList = wifiManager.getScanResults();
            if (wifiList == null || wifiList.size() == 0 || isConnected) return;
            onReceiveNewNetworks(wifiList);
        }
        public void onReceiveNewNetworks(List<ScanResult> wifiList) {
            passableHotsPot = new ArrayList<String>();
            for (ScanResult result : wifiList) {
                System.out.println("wifi ssid is:"+result.SSID);
                if ((result.SSID).contains(getString(R.string.ap_ssid)))
                    passableHotsPot.add(result.SSID);
            }
            synchronized (this) {
                connectToHotpot();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_view_video);
        init();
        boolean publishFlag = getIntent().getBooleanExtra(getString(R.string.publish_video), false);
        if (publishFlag) {
            wifiManager.setWifiEnabled(false);
            Method.changeApState(this, wifiManager, true);
            DhcpInfo info = wifiManager.getDhcpInfo();
            int serverAddress = info.ipAddress;
            mAddr = com.ANT.MiddleWare.WiFi.WiFiTCP.Method.intToInetAddress(serverAddress);
            Log.d(TAG, mAddr.toString());
            new ServerThread(mAddr, this).start();
        }else {
            connectHotPot();
        }
    }

    private void connectHotPot() {
        Method.changeApState(ViewVideoActivity.this, wifiManager, false);
        wifiManager.setWifiEnabled(true);
        wifiManager.startScan();
        wifiReceiver = new WiFiReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    private void init() {
        buttonView = (Button) findViewById(R.id.button_view_video);
        editTextLocation = (EditText) findViewById(R.id.edittext_video_location);
        switchButton = (Switch) findViewById(R.id.switch_publish_video);
        switchButton.setChecked(false);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Fragment video = new VideoFragment();
        Fragment chatroom = new ChatFragment();
//        ft.add(R.id.fragment_chat_room,chatroom);
        ft.add(R.id.fragment_video_player, video);
        ft.commit();
        initDashProxy();
    }

    private void initDashProxy() {
        configureData.setWorkingMode(ConfigureData.WorkMode.LOCAL_MODE);
    }

    private void connectToHotpot() {
        if (passableHotsPot == null || passableHotsPot.size() == 0)
            return;
        WifiConfiguration wifiConfig = setWifiParams(passableHotsPot.get(0));
        int wcgID = wifiManager.addNetwork(wifiConfig);
        boolean flag = wifiManager.enableNetwork(wcgID, true);
        isConnected = flag;
        if (!flag) return;
        unregisterReceiver(wifiReceiver);
        DhcpInfo info = wifiManager.getDhcpInfo();
        int serverAddress = info.serverAddress;
        serverAddr = com.ANT.MiddleWare.WiFi.WiFiTCP.Method.intToInetAddress(serverAddress);
        Log.d(TAG, "server's ip address:"+serverAddr);
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
        new Client(serverAddr, 12345);
    }
    private WifiConfiguration setWifiParams(String ssid) {
        WifiConfiguration apConfig = new WifiConfiguration();
        apConfig.SSID = "\"" + ssid + "\"";
        apConfig.preSharedKey = "\"" + getString(R.string.ap_password) + "\"";
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
