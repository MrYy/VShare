package com.ANT.MiddleWare.PartyPlayerActivity;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.ANT.MiddleWare.DASHProxyServer.DashProxyServer;

public class ProxyService extends Service {
	private static final String TAG = ProxyService.class.getSimpleName();

	public MyBinder sBinder;

	public class MyBinder extends Binder {

		public ProxyService getProxyService() {
			return ProxyService.this;
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return sBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startedId) {

		DashProxyServer server = new DashProxyServer();
		try {
			server.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		sBinder = new MyBinder();
	}

}
