package com.ANT.MiddleWare.PartyPlayerActivity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.Window;

import com.ANT.MiddleWare.PartyPlayerActivity.bean.Statistics;
import com.ANT.MiddleWare.PartyPlayerActivity.bean.StatisticsFactory;

public class MainActivity extends FragmentActivity {
	private static final String TAG = MainActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
		WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		manager.setWifiEnabled(false);
		if (fragment == null) {
			fragment = new LoginFragment();
			fm.beginTransaction().add(R.id.fragmentContainer, fragment)
					.commit();
		}
		//统计下载量，下载速度测试代码，暂无用
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						TimeUnit.MILLISECONDS.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					StatisticsFactory.getInstance(StatisticsFactory.Type.gReceive).add(100);
//						TimeUnit.SECONDS.sleep(1);
						StatisticsFactory.getInstance(StatisticsFactory.Type.wifiSend).add(200);
//						TimeUnit.SECONDS.sleep(1);
						StatisticsFactory.getInstance(StatisticsFactory.Type.wifiReceive).add(300);
				}
			}
		});

	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		// TODO Auto-generated method stub
		setOverflowIconVisible(featureId, menu);
		return super.onMenuOpened(featureId, menu);
	}

	public static void setOverflowIconVisible(int featureId, Menu menu) {
		if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
			if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
				try {
					Method m = menu.getClass().getDeclaredMethod(
							"setOptionalIconsVisible", Boolean.TYPE);
					m.setAccessible(true);
					m.invoke(menu, true);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
