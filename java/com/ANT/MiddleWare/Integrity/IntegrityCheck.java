package com.ANT.MiddleWare.Integrity;

import android.util.Log;
import android.util.SparseArray;

import com.ANT.MiddleWare.Celluar.CellularDown;
import com.ANT.MiddleWare.Celluar.CellularDown.CellType;
import com.ANT.MiddleWare.Entities.FileFragment;
import com.ANT.MiddleWare.Entities.Segment;
import com.ANT.MiddleWare.PartyPlayerActivity.ViewVideoActivity;
import com.ANT.MiddleWare.WiFi.WiFiFactory;
import com.ANT.MiddleWare.WiFi.WiFiTCP.Client;

public class IntegrityCheck {
	private static final String TAG = IntegrityCheck.class.getSimpleName();
	// single instance mode
	private static IntegrityCheck instance;
	private SparseArray<Segment> urlMap;
	public static final String URL_TAG = "http://buptant.cn/autoChart/du/video/ljw2016/zxyqwe/download.php";
	public static final String GROUP_TAG = "http://buptant.cn/autoChart/du/video/ljw2016/zxyqwe/appdown.php";
	public static final String TCP_TAG =  "http://buptant.cn/autoChart/du/video/ljw2016/zxyqwe/tcpappdown.php";
	public static final String JUNIT_TAG = "http://127.0.0.1:9999/junit.php";
	public static final String URI_TAG = "http://127.1.1.1:9999/";

	private IntegrityCheck() {
		urlMap = new SparseArray<Segment>();
	}

	public void clear() {
		urlMap = null;
		urlMap = new SparseArray<Segment>();
	}
	public static synchronized IntegrityCheck getInstance() {
		if (instance == null) {
			instance = new IntegrityCheck();
		}
		return instance;
	}

	public byte[] getSegments(int uri) {
		return getSegments(uri, CellType.Single);
	}

	public byte[] getSegments(int uri, CellType ct) {
		int id = uri;
		synchronized (this) {
			if (urlMap.indexOfKey(id) < 0) {
				urlMap.put(id, new Segment(id, -1));
			}
			Segment segment = urlMap.get(id);
			if (segment.checkIntegrity()) {
				return segment.getData();
			} else {
				CellularDown.queryFragment(ct, id);
				Log.v(TAG, "url" + id);
			}
		}
		while (true) {
			synchronized (this) {
				Segment segment = urlMap.get(id);
				if (segment.checkIntegrity()) {
					return segment.getData();
				}
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				return null;// Empty Data
			}
		}

	}

	public void setSegLength(int id, int totalLength) {
		Segment s = urlMap.get(id);
		s.setSegLength(totalLength);
	}

	public void insert(int id, FileFragment fm) {
		synchronized (this) {
			if (urlMap.indexOfKey(id) < 0) {
				urlMap.put(id, new Segment(id, -1));
			}
		}
		Segment s = urlMap.get(id);
		s.insert(fm);
		ViewVideoActivity.insert(fm);
//		WiFiFactory.insertF(fm);
	}
	public void insert(int id, FileFragment fm,int n) {
		synchronized (this) {
			if (urlMap.indexOfKey(id) < 0) {
				urlMap.put(id, new Segment(id, -1));
			}
		}
		Segment s = urlMap.get(id);
		s.insert(fm);
		s.checkIntegrity();
	}
	public void insert(int id, FileFragment fm,Client client) {
		synchronized (this) {
			if (urlMap.indexOfKey(id) < 0) {
				urlMap.put(id, new Segment(id, -1));
			}
		}
		Segment s = urlMap.get(id);
		s.insert(fm);
		s.checkIntegrity();
	}
	public Segment getSeg(int id) {
		return urlMap.get(id);
	}
}
