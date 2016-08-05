package com.ANT.MiddleWare.PartyPlayerActivity.policy.directStatus;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import com.ANT.MiddleWare.PartyPlayerActivity.R;
import com.ANT.MiddleWare.WiFi.WiFiNCP2.ServerThread;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zxc on 2016/8/4.
 */
public class IsOwner implements Status {
    private WifiP2pManager manager;
    public static final String ip = "192.168.49.1";
    private WifiP2pManager.Channel channel;
    private Context context;
    private static final String TAG = "WIFI DIRECT";

    public IsOwner(Context context, WifiP2pManager manager, WifiP2pManager.Channel channel) {
        this.context = context;
        this.channel = channel;
        this.manager = manager;
    }


    @Override
    public void supportWifiDirect() {

        manager.createGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "owner start");
                ExecutorService es = Executors.newFixedThreadPool(1);
                changeWifiDirectName(context.getString(R.string.ssid));
                //开启serverthread
//                    es.execute(new ServerThread(InetAddress.getByName(ip),context));

            }

            @Override
            public void onFailure(int i) {

            }
        });
    }

    @Override
    public void findPeers() {

    }

    @Override
    public void connectSuccess() {
        ExecutorService es = Executors.newFixedThreadPool(1);
        try {
            es.execute(new ServerThread(InetAddress.getByName(ip),context));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void changeWifiDirectName(final String newName){
        Method m=null;
        try{
            m=manager.getClass().getMethod("setDeviceName",new Class[]{channel.getClass(),
                    String.class,WifiP2pManager.ActionListener.class});
        }catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        try{
            if(m!=null){
                m.invoke(manager, channel,newName,new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Name changed to "+newName);
                    }
                    @Override
                    public void onFailure(int reason) {
                        Log.d(TAG, "The name was not changed");
                    }
                });
            }
        }catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
