package com.ANT.MiddleWare.WiFi.WiFiNCP2;

import android.content.Context;
import android.util.Log;

import com.ANT.MiddleWare.PartyPlayerActivity.MainFragment;
import com.ANT.MiddleWare.WiFi.WiFiPulic;

public class WiFiNCP2 extends WiFiPulic {
	private static final String TAG = WiFiNCP2.class.getSimpleName();
	private final DumpUnil dumpUnil;

	public WiFiNCP2(Context contect) {
		super(contect);
		dumpUnil = new DumpUnil(contect);
		if (MainFragment.ncp2Ap) {
			Log.d(TAG, "I am ap");
		}else {
			Log.d(TAG, "I am client");

		}
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
