package com.ANT.MiddleWare.PartyPlayerActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ANT.MiddleWare.DASHProxyServer.DashProxyServer;
import com.ANT.MiddleWare.Entities.FileFragment;
import com.ANT.MiddleWare.PartyPlayerActivity.bean.MenuLayout;
import com.ANT.MiddleWare.PartyPlayerActivity.bean.SendTask;
import com.ANT.MiddleWare.WiFi.WiFiNCP2.Client;
import com.ANT.MiddleWare.WiFi.WiFiNCP2.ServerThread;
import com.ANT.MiddleWare.PartyPlayerActivity.bean.Message;
import com.ANT.MiddleWare.PartyPlayerActivity.util.Method;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class ViewVideoActivity extends FragmentActivity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnInfoListener {
    private static final String TAG = ViewVideoActivity.class.getSimpleName();
    private Button play;
    private EditText editVideoPath;
    private DashProxyServer server = new DashProxyServer();
    public static ConfigureData configureData = new ConfigureData(null);
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
    public static final BlockingQueue<SendTask> taskMessageQueue = new LinkedBlockingQueue<SendTask>();
    public static final BlockingQueue<SendTask> sendMessageQueue = new LinkedBlockingQueue<SendTask>();
    public static final BlockingQueue<Message> receiveMessageQueue = new LinkedBlockingQueue<Message>();
    public static final Set<String> onLineUsers = new ConcurrentSkipListSet<>();
    public static String userName="";
    public static boolean isAp = false;
    private ViewPager vp;
    private String path="http://127.0.0.1:9999/4/index.m3u8";
//    private String path= Environment.getExternalStorageDirectory()+"/video/4/1.mp4";
//    private String path="";
    private VideoView mVideoView;
    private FrameLayout frameLayout;
    private LinearLayout playSetLayout;
    boolean isPortrait=true;
    private long mPosition=0;
    private int vheight=0;
    private static Set<InetAddress> mClients = new HashSet<>();
    /** 初次进入时候的蒙版背景 */
    private LinearLayout linearLayout_mask;
    /** 初次进入时的蒙版图片 */
    private ImageView imageView_mask;
    private MenuLayout menuLayout;

    public static void sendMsg(Message msg) {
        SendTask sendTask = new SendTask();
        sendTask.setMsg(msg);
        if (isAp) {
            sendTask.setClients(mClients);
        }
        sendMessageQueue.add(sendTask);
    }

    public static synchronized Set<InetAddress> getClients() {
        return mClients;
    }

    public static synchronized void setClients(Set<InetAddress> clients) {
        mClients = clients;
    }

    public static Message getMsg() {
        return receiveMessageQueue.poll();
    }

    public static void insert(FileFragment ff) {
        synchronized (taskMessageQueue) {
            Log.d(TAG, "taskQueue's length:" + String.valueOf(taskMessageQueue.size()));
            Message msg = new Message();
            msg.setFragment(ff);
            SendTask sendTask = new SendTask();
            sendTask.setMsg(msg);
            sendTask.setClients(mClients);
            taskMessageQueue.add(sendTask);
        }
    }
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (!LibsChecker.checkVitamioLibs(this)){
            return;
        }

        setContentView(R.layout.fragment_view_video);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        init();
        boolean publishFlag = getIntent().getBooleanExtra(getString(R.string.publish_video), false);
        userName = getIntent().getStringExtra(getString(R.string.user_name));
        if (userName.equals("")) {
            userName = "ant";
        }
        Log.d("ServerThread", userName);
        onLineUsers.add(userName);
        if (publishFlag) {
            new SweetAlertDialog(this,SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("选择身份")
                    .setConfirmText("播主").setCancelText("看客")
                    .showCancelButton(true)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            beHotPot();
                            sweetAlertDialog.cancel();
                        }
                    }).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    connectHotPot();
                    sweetAlertDialog.cancel();
                }
            }).show();
        }
        menuLayout = (MenuLayout)findViewById(R.id.bottom_menu);
        menuLayout.setFocuse(MenuLayout.BUTTON.LEFT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MenuLayout menuLayout = (MenuLayout)findViewById(R.id.bottom_menu);
        menuLayout.setFocuse(MenuLayout.BUTTON.LEFT);
    }

    private void initPlayVideo() {

        //path=editVideoPath.getText().toString().trim();
        mVideoView= (VideoView) findViewById(R.id.buffer);
        frameLayout= (FrameLayout) findViewById(R.id.fragment_video_player);
        vheight= frameLayout.getHeight();
        Log.d(TAG, "initPlayVideo: frameLoyout"+frameLayout.getHeight()+" "+frameLayout.getWidth());
        if (path == "") {
            // Tell the user to provide a media file URL/path.
            Toast.makeText(this,"Please edit url",Toast.LENGTH_LONG).show();
            return;
        } else {
            //streamVideo
            mVideoView.setVideoURI(Uri.parse(path));

            MediaController mc = new MediaController(this, true, frameLayout);
            mc.setOnControllerClick(new MediaController.OnControllerClick() {
                @Override
                public void OnClick(int type) {
                    //type 0 全屏
                    if (type == 0) {
                        if (isPortrait) {
                            LinearLayout.LayoutParams fl_lp = new LinearLayout.LayoutParams(
                                    getHeightPixel(ViewVideoActivity.this),
                                    getWidthPixel(ViewVideoActivity.this)-getStatusBarHeight(ViewVideoActivity.this)
                            );
                            vp.setVisibility(View.GONE);
                            frameLayout.setLayoutParams(fl_lp);
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, 0);//缩放参数，画面全屏
                            isPortrait = false;
                            menuLayout.setVisibility(View.GONE);

                        } else {
                            LinearLayout.LayoutParams fl_lp = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    vheight
                                    //DensityUtil.dip2px(260,ViewVideoActivity.this)
                            );
                            frameLayout.setLayoutParams(fl_lp);
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            vp.setVisibility(View.VISIBLE);
                            isPortrait = true;
                            menuLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });

            mVideoView.setMediaController(mc);
            mc.setVisibility(View.GONE);
            mVideoView.requestFocus();
            mVideoView.setOnInfoListener(ViewVideoActivity.this);
            mVideoView.setOnCompletionListener(ViewVideoActivity.this);
            //mVideoView.setOnBufferingUpdateListener(ViewVideoActivity.this);
            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    // optional need Vitamio 4.0
                    mediaPlayer.setPlaybackSpeed(1.0f);
                }
            });

            LinearLayout.LayoutParams fl_lp = new LinearLayout.LayoutParams(
                    getHeightPixel(ViewVideoActivity.this),
                    getWidthPixel(ViewVideoActivity.this)-getStatusBarHeight(ViewVideoActivity.this)
            );
            vp.setVisibility(View.GONE);
            frameLayout.setLayoutParams(fl_lp);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, 0);//缩放参数，画面全屏
            isPortrait = false;
            menuLayout.setVisibility(View.GONE);

        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch(what){
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if(mVideoView.isPlaying()){
                    mVideoView.pause();
                }
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                if(playSetLayout.getVisibility()!=View.GONE){
//                    playSetLayout.setVisibility(View.GONE);
                }
                mVideoView.start();
                break;
        }
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
//        playSetLayout.setVisibility(View.VISIBLE);
        mVideoView.seekTo(0);
    }

    private int getHeightPixel(FragmentActivity activity) {
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
        return localDisplayMetrics.heightPixels;
    }
    public int getWidthPixel(FragmentActivity activity)
    {
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
        return localDisplayMetrics.widthPixels;
    }
    public  int getStatusBarHeight(FragmentActivity activity){
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        return statusBarHeight;
    }

    private void beHotPot() {
        isAp = true;
        wifiManager.setWifiEnabled(false);
        Method.changeApState(ViewVideoActivity.this, wifiManager, true);
        DhcpInfo info = wifiManager.getDhcpInfo();
        int serverAddress = info.ipAddress;
        mAddr = Method.intToInetAddress(serverAddress);
        Log.d(TAG, mAddr.toString());
        new ServerThread(mAddr, ViewVideoActivity.this).start();
    }

    private void connectHotPot() {
        Method.changeApState(ViewVideoActivity.this, wifiManager, false);
        wifiManager.setWifiEnabled(true);
        wifiManager.startScan();
        wifiReceiver = new WiFiReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    private void init() {
        linearLayout_mask = (LinearLayout)findViewById(R.id.linearLayout_mask);
        imageView_mask = (ImageView)findViewById(R.id.imageView_mask);
        imageView_mask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearLayout_mask.setVisibility(View.GONE);
                getBaseContext().getSharedPreferences("Setting", Context.MODE_PRIVATE).edit().putBoolean("read_share", true).commit();
            }
        });
        setMask();
        play = (Button) findViewById(R.id.button_view_video);
        editVideoPath = (EditText) findViewById(R.id.edittext_video_location);
        playSetLayout= (LinearLayout) findViewById(R.id.playSet);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
        vp=(ViewPager)findViewById(R.id.viewpager);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
//        Fragment video = new VideoFragment();
//        ft.add(R.id.fragment_video_player, video);
//        ft.commit();
        List<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(new UsersFragment());
        fragments.add(new ChatFragment());
        FragAdapter adapter = new FragAdapter(getSupportFragmentManager(),fragments);
        vp.setAdapter(adapter);
        initDashProxy();
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPlayVideo();
//                playSetLayout.setVisibility(View.GONE);
            }
        });

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
    private void setMask() {
        SharedPreferences sharedPreferences = getBaseContext().getSharedPreferences(
                "Setting", Context.MODE_PRIVATE);
        boolean isread =  sharedPreferences.getBoolean("read_share", false);
        if(!isread){
            // 调整顶部背景图片的大小，适应不同分辨率的屏幕
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int width = dm.widthPixels;
            int height = (int) ((float) width *1.6);
            imageView_mask.setLayoutParams(new LinearLayout.LayoutParams(width, height));
            linearLayout_mask.setVisibility(View.VISIBLE);
        }else{
            linearLayout_mask.setVisibility(View.GONE);
        }
    }

}
