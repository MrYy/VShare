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
import com.ANT.MiddleWare.WiFi.WiFiTCP.Method;

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
    private static Runtime runtime;
    private static final String CMD_GET_WPA = "cat data/misc/wifi/wpa_supplicant.conf\n";
    private final String file_path = "data/misc/wifi/wpa_supplicant.conf";
    private List<ScanResult> wifiList;
    private boolean isConnected = false;
    private WiFiReceiver wifiReceiver;
    private TimerTask mTimerTask;

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
        mTimer = new Timer();
        mTimerTask = new TimerTask(){
            @Override
            public void run() {
                if (isConnected) {
                    mTimer.cancel();
                    return;
                }
                connectHotPot();
            }
        };
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

    public static synchronized String run(String[] cmd, String workdirectory)
            throws IOException {
        StringBuffer result = new StringBuffer();
        try {
            // 创建操作系统进程（也可以由Runtime.exec()启动）
            // Runtime runtime = Runtime.getRuntime();
            // Process proc = runtime.exec(cmd);
            // InputStream inputstream = proc.getInputStream();
            ProcessBuilder builder = new ProcessBuilder(cmd);

            InputStream in = null;
            // 设置一个路径（绝对路径了就不一定需要）
            if (workdirectory != null) {
                // 设置工作目录（同上）
                builder.directory(new File(workdirectory));
                // 合并标准错误和标准输出
                builder.redirectErrorStream(true);
                // 启动一个新进程
                Process process = builder.start();

                // 读取进程标准输出流
                in = process.getInputStream();
                byte[] re = new byte[1024];
                while (in.read(re) != -1) {
                    result = result.append(new String(re));
                }
            }
            // 关闭输入流
            if (in != null) {
                in.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result.toString();
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
        InetAddress serverAddr = com.ANT.MiddleWare.WiFi.WiFiTCP.Method.intToInetAddress(serverAddress);
        Log.d(TAG, "server's ip address:"+serverAddr);
        wifiManager.setWifiEnabled(false);
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
