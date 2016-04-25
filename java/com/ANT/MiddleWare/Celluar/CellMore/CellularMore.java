package com.ANT.MiddleWare.Celluar.CellMore;

import android.util.Log;

import com.ANT.MiddleWare.Entities.Segment;
import com.ANT.MiddleWare.Entities.Segment.SegmentException;
import com.ANT.MiddleWare.Integrity.IntegrityCheck;

public class CellularMore extends Thread {
	private static final String TAG = CellularMore.class.getSimpleName();

	private int url;

	public CellularMore(int url) {
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
