package com.ANT.MiddleWare.PartyPlayerActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ANT.MiddleWare.DASHProxyServer.DashProxyServer;
import com.ANT.MiddleWare.Entities.FileFragment;
import com.ANT.MiddleWare.Integrity.IntegrityCheck;
import com.ANT.MiddleWare.PartyPlayerActivity.bean.MenuLayout;
import com.ANT.MiddleWare.PartyPlayerActivity.bean.Message;
import com.ANT.MiddleWare.PartyPlayerActivity.bean.SendTask;
import com.ANT.MiddleWare.PartyPlayerActivity.bean.StatisticsFactory;
import com.ANT.MiddleWare.PartyPlayerActivity.util.Method;
import com.ANT.MiddleWare.WiFi.WiFiNCP2.Client;
import com.ANT.MiddleWare.WiFi.WiFiNCP2.ServerThread;

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
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class ViewVideoActivity extends FragmentActivity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnInfoListener {
    private static final String TAG = ViewVideoActivity.class.getSimpleName();
    private ImageButton play;
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
    private String shareLocalPath = "http://127.0.0.1:9999/local";
    private String path="http://127.0.0.1:9999/4/index.m3u8";
    private String path2= Environment.getExternalStorageDirectory()+"/video/4/1.mp4";
//    private String path="";
    private VideoView mVideoView;
    private FrameLayout frameLayout;
    private RelativeLayout playSetLayout;
    boolean isPortrait=true;
    private long mPosition=0;
    private int vheight=0;
    private static Set<InetAddress> mClients = new HashSet<>();
    private MenuLayout menuLayout;
    private ViewGroup mGroup;
    private View mView;
    private String[] videoList;
    private List<ContentModel> list;
    private DrawerLayout mDrawerLayout;
    private RelativeLayout leftLayout;
    private ListView mDrawerList;
    private ContentAdapter adapter;
    private String vpath;
    public static final String SYSTEM_MESSAGE_SHARE_LOCAL = "asdfnvlxczvoj3asfpizfj323fsadf[]]adfadsf,./";
    private static CyclicBarrier cyclicBarrier = new CyclicBarrier(2);
    private final static Lock lock = new ReentrantLock();
    private final static Condition condition = lock.newCondition();
    public  Handler mHandler = new Handler(){

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "share local");
            android.os.Message msgL = new android.os.Message();
            switch (msg.what) {
                case 1:
                    //准备接收视频
                    configureData.setWorkingMode(ConfigureData.WorkMode.LOCAL_MODE);
                    Method.display(ViewVideoActivity.this,"播主开始推送视频啦");
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    initPlayVideo(shareLocalPath);
                    break;
                case 2:
                    break;
            }

        }
    };
    private SweetAlertDialog pDialog;

    public Handler getmHandler() {
        return mHandler;
    }
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

    public  static Message getMsg() {
        lock.lock();
        try {
            while (receiveMessageQueue.size() == 0) try {
                condition.await();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return receiveMessageQueue.poll();
        }finally {
            lock.unlock();
        }
    }

    public static void insertReceiveMQ(Message msg) {
        lock.lock();
        try {
            condition.signalAll();
            receiveMessageQueue.add(msg);
        }finally {
            lock.unlock();
        }
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
            SharedPreferences preferences = getSharedPreferences(getString(R.string.user_save),MODE_PRIVATE);
            String first = preferences.getString(getString(R.string.user_first_login), "");
            if (first.equals("")) {
                mView = createView();
                mGroup = (ViewGroup) ViewVideoActivity.this.getWindow().getDecorView();
                mGroup.addView(mView);
            }else {
                alertDialog();
            }


        }
        menuLayout = (MenuLayout) findViewById(R.id.bottom_menu);
        menuLayout.setFocuse(MenuLayout.BUTTON.LEFT);
        pDialog = new SweetAlertDialog(ViewVideoActivity.this, SweetAlertDialog.PROGRESS_TYPE);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
    }

    @Override
    protected void onResume() {
        super.onResume();
        MenuLayout menuLayout = (MenuLayout) findViewById(R.id.bottom_menu);
        menuLayout.setFocuse(MenuLayout.BUTTON.LEFT);
    }

    private View createView() {
        FrameLayout parent = new FrameLayout(this);
        parent.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        final View mBg = new View(ViewVideoActivity.this);
        mBg.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        mBg.setBackgroundResource(R.drawable.mask);
        mBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGroup.removeView(mView);
                getBaseContext().getSharedPreferences(getString(R.string.user_save), Context.MODE_PRIVATE).edit().putString(getString(R.string.user_first_login),"ok").commit();
                alertDialog();
            }
        });

        parent.setPadding(0, 0, 0, getNavBarHeight(this));
        parent.addView(mBg);
        return parent;
    }

    private void alertDialog() {
        new SweetAlertDialog(ViewVideoActivity.this, SweetAlertDialog.SUCCESS_TYPE)
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
    public int getNavBarHeight(Context c) {
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            boolean hasMenuKey = ViewConfiguration.get(c).hasPermanentMenuKey();
            boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);

            if (!hasMenuKey && !hasBackKey) {
                //The device has a navigation bar
                Resources resources = c.getResources();

                int orientation = getResources().getConfiguration().orientation;
                int resourceId;
                if (isTablet(c)) {
                    resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
                } else {
                    resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_width", "dimen", "android");
                }

                if (resourceId > 0) {
                    return getResources().getDimensionPixelSize(resourceId);
                }
            }
        }
        return result;
    }
    private boolean isTablet(Context c) {
        return (c.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }



    private void initPlayVideo(String path) {
        StatisticsFactory.startStatistic();
        //path=editVideoPath.getText().toString().trim();
        Log.d(TAG, path);
        if (!path.startsWith("http")) {
            Log.d(TAG, "local video");
            if (isAp) {
                Method.display(ViewVideoActivity.this,"开始推送视频");
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            shareLocalVideo(path);
        }else {
            //切换3g下载模式
            configureData.setWorkingMode(ConfigureData.WorkMode.FAKE_MODE);
        }
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

    private void shareLocalVideo(String path) {
        Message msg = new Message();
        msg.setMessage(SYSTEM_MESSAGE_SHARE_LOCAL);
        sendMsg(msg);
        Method.shareLocalVideo(path);
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
//                if(playSetLayout.getVisibility()!=View.GONE){
//                    playSetLayout.setVisibility(View.GONE);
//                }
                mVideoView.start();
                break;
        }
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //playSetLayout.setVisibility(View.VISIBLE);
        mPosition=0;
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if(!isPortrait){
                LinearLayout.LayoutParams fl_lp=new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        vheight
                );
                frameLayout.setLayoutParams(fl_lp);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                vp.setVisibility(View.VISIBLE);
                menuLayout.setVisibility(View.VISIBLE);
                isPortrait=true;
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
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
        play = (ImageButton) findViewById(R.id.button_view_video);
        playSetLayout= (RelativeLayout) findViewById(R.id.playSet);
        mDrawerLayout= (DrawerLayout) findViewById(R.id.drawer_layout);
        leftLayout= (RelativeLayout) findViewById(R.id.left);
        mDrawerList= (ListView) findViewById(R.id.left_drawer);
        videoList=new String[]{path,path2,"path3"};
        list=new ArrayList<ContentModel>();

        list.add(new ContentModel(R.drawable.video, "共享网络视频"));
        list.add(new ContentModel(R.drawable.video, "推送本地视频"));
        adapter=new ContentAdapter(this,list);
        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerIemClickListener());
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
//                initPlayVideo();
//                playSetLayout.setVisibility(View.GONE);
                 mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });

    }

    private void initDashProxy() {
        configureData.setWorkingMode(ConfigureData.WorkMode.FAKE_MODE);
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


    private class DrawerIemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            vpath=videoList[i];
            Log.d(TAG, "onItemClick: path"+vpath);
            initPlayVideo(vpath);
            mDrawerList.setItemChecked(i,true);
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        }
    }
}
