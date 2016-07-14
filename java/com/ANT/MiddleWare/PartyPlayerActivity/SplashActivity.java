package com.ANT.MiddleWare.PartyPlayerActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;

import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.bgabanner.BGABanner;

public class SplashActivity extends Activity implements ViewPager.OnPageChangeListener {
    private BGABanner banner;
    private int count=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_start);
        SharedPreferences prefernces = SplashActivity.this.getSharedPreferences(getString(R.string.user_save), Context.MODE_PRIVATE);
        String first = prefernces.getString(getString(R.string.user_first_login), "");
        if (!first.equals("")) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
            return;
        }
        banner = (BGABanner) findViewById(R.id.banner_splash_pager);
        banner.setTransitionEffect(BGABanner.TransitionEffect.Accordion);
        banner.startAutoPlay();
        // 设置page切换时长
        banner.setPageChangeDuration(500);
        banner.addOnPageChangeListener(this);
        List<View> views = new ArrayList<>();
        views.add(getLayoutInflater().inflate(R.layout.enter_layout_3, null));
        views.add(getLayoutInflater().inflate(R.layout.enter_layout_1, null));
        View lastView = getLayoutInflater().inflate(R.layout.enter_layout_2, null);
        views.add(lastView);
        lastView.findViewById(R.id.splash_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        });
        banner.setViews(views);
    }



    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        count++;
        if (count > 40) {
            banner.stopAutoPlay();
        }
    }

    @Override
    public void onPageSelected(int position) {



    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
