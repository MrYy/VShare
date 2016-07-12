package com.ANT.MiddleWare.PartyPlayerActivity;

import android.app.Activity;
import android.os.Bundle;

import com.ANT.MiddleWare.PartyPlayerActivity.bean.MenuLayout;

public class UserActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity);

    }

    @Override
    protected void onResume() {
        super.onResume();
        MenuLayout menuLayout = (MenuLayout)findViewById(R.id.bottom_menu);
        menuLayout.setFocuse(MenuLayout.BUTTON.RIGHT);
    }
}
