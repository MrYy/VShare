package com.ANT.MiddleWare.PartyPlayerActivity.util;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.ANT.MiddleWare.PartyPlayerActivity.R;
import com.ANT.MiddleWare.PartyPlayerActivity.bean.MenuLayout;
import com.ANT.MiddleWare.PartyPlayerActivity.bean.Statistics;

public class StatisticsActivity extends Activity {
    public static final int gR = 0;
    public static final int wR = 1;
    public static final int wS = 2;
    public static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int speed = msg.arg1;
            switch (msg.what) {
                case gR:
                    textView1.setText(String.valueOf(speed)+" kbit/s");
                    break;
                case wR:
                    textView2.setText(String.valueOf(speed)+" kbit/s");
                    break;
                case wS:
                    textView3.setText(String.valueOf(speed)+" kbit/s");
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private static TextView textView1;
    private static TextView textView2;
    private static TextView textView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        textView1 = (TextView) findViewById(R.id.textView4);
        textView2 = (TextView) findViewById(R.id.textView5);
        textView3 = (TextView) findViewById(R.id.textView6);

    }

    @Override
    protected void onResume() {
        super.onResume();
        MenuLayout menuLayout = (MenuLayout)findViewById(R.id.bottom_menu);
        menuLayout.setFocuse(MenuLayout.BUTTON.CENTER);
        Log.d("TAG", "on resume");

    }
}
