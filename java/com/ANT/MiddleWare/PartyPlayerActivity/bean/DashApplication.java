package com.ANT.MiddleWare.PartyPlayerActivity.bean;

import android.app.Application;
import android.content.SharedPreferences;

import com.ANT.MiddleWare.PartyPlayerActivity.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ge on 2016/3/8.
 */
public class DashApplication extends Application {
    public static final String REGISTER = "http://101.200.135.129/zhanshibang/index.php/user/index/dash_register";
    public static final String INFO = "http://101.200.135.129/zhanshibang/index.php/user/index/user_info";
    public static final String UPLOAD = "http://101.200.135.129/zhanshibang/index.php/user/index/upload_photo";
    public static final String LOGIN = "http://101.200.135.129/zhanshibang/index.php/user/index/dash_login";
    private String pwd;


    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void setUser(String user,String password) {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.user_save), MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.user_save_name), user);
        editor.putString(getString(R.string.user_save_password), password);

        editor.apply();
    }

    public Map<String,String> getUser() {
        String user;
        SharedPreferences preferences = getSharedPreferences(getString(R.string.user_save), MODE_PRIVATE);
        user = preferences.getString(getString(R.string.user_save_name), "");
        pwd = preferences.getString(getString(R.string.user_save_password), "");
        Map<String, String> map = new HashMap<>();
        if (!user.equals("")) {
            map.put("name", user);
            map.put("password", pwd);
        }
        return map;
    }

}
