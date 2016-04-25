package com.ANT.MiddleWare.Celluar.WiFiMore;

import android.util.Log;

import com.ANT.MiddleWare.Entities.Segment;
import com.ANT.MiddleWare.Entities.Segment.SegmentException;
import com.ANT.MiddleWare.Integrity.IntegrityCheck;
import com.ANT.MiddleWare.WiFi.WiFiFactory;

public class WiFiMore extends Thread {
	private static final String TAG = WiFiMore.class.getSimpleName();

	private int url;

	public WiFiMore(int url) {
		this.url = url;
	}

	@Override
	public void run() {
		IntegrityCheck IC = IntegrityCheck.getInstance();
		Segment Seg = IC.getSeg(url);
		if (Seg != null) {
			while (!Seg.checkIntegrity()) {
				int miss;
				try {
					miss = Seg.getMiss();
				} catch (SegmentException e) {
					e.printStackTrace();
					break;
				}
				Log.v(TAG, "no " + url + " " + miss);
				WiFiFactory.notify(url, miss);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			Log.d(TAG, "yes " + url);
		} else {
			Log.e(TAG, "a " + url);
		}
	}
}
