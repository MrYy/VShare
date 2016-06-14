package com.ANT.MiddleWare.WiFi.WiFiNCP2;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.ANT.MiddleWare.PartyPlayerActivity.MainFragment;
import com.ANT.MiddleWare.WiFi.WiFiPulic;

import java.util.Observable;
import java.util.Observer;

public class WiFiNCP2 extends WiFiPulic {
	private static final String TAG = WiFiNCP2.class.getSimpleName();
	private final DumpUnil dumpUnil;
	public static class ApObserver implements Observer {

		@Override
		public void update(Observable observable, Object data) {
			//this one is ap
		}
	}
	public WiFiNCP2(Context contect) {
		super(contect);
		dumpUnil = new DumpUnil(contect);
		WifiManager manager = (WifiManager) contect.getSystemService(Context.WIFI_SERVICE);

	}



	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void notify(int seg, int start) {
		// TODO Auto-generated method stub

	}

	@Override
	public void EmergencySend(byte[] data) {
		// TODO Auto-generated method stub
		
	}
}
