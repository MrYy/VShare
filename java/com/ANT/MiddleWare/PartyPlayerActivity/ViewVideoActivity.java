package com.ANT.MiddleWare.PartyPlayerActivity;

import android.Manifest;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.ANT.MiddleWare.DASHProxyServer.DashProxyServer;
import com.ANT.MiddleWare.WiFi.WiFiTCP.Method;

public class ViewVideoActivity extends FragmentActivity implements View.OnClickListener {

    private Button buttonView;
    private EditText editTextLocation;
    private DashProxyServer server = new DashProxyServer();
    public static ConfigureData configureData = new ConfigureData(null);
    private Switch switchButton;
    private WifiManager wifiManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_view_video);
        init();
    }

    private void init() {
        buttonView = (Button) findViewById(R.id.button_view_video);
        editTextLocation = (EditText) findViewById(R.id.edittext_video_location);
        switchButton = (Switch) findViewById(R.id.switch_publish_video);
        switchButton.setOnClickListener(this);
        switchButton.setChecked(false);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Fragment video = new VideoFragment();
        Fragment chatroom = new ChatFragment();
        ft.add(R.id.fragment_chat_room,chatroom);
        ft.add(R.id.fragment_video_player, video);
        ft.commit();
        initDashProxy();
    }
    private void initDashProxy() {
            configureData.setWorkingMode(ConfigureData.WorkMode.LOCAL_MODE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_publish_video:
                if (switchButton.isChecked()) {
                    wifiManager.setWifiEnabled(false);
                    Method.changeApState(this,wifiManager,true);
                }else {
                    Method.changeApState(this,wifiManager,false);
                }
                break;
        }
    }
}
