package com.ANT.MiddleWare.PartyPlayerActivity.bean;

import android.app.Application;

/**
 * Created by ge on 2016/3/8.
 */
public class DashApplication extends Application {
    public static final String REGISTER = "http://101.200.135.129/zhanshibang/index.php/user/index/dash_register";
    public static final String INFO = "http://101.200.135.129/zhanshibang/index.php/user/index/user_info";
    public static final String UPLOAD = "http://101.200.135.129/zhanshibang/index.php/user/index/upload_photo";


    @Override
    public void onCreate() {
        super.onCreate();

    }

}
