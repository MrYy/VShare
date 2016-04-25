package com.ANT.MiddleWare.WiFi;

import java.io.IOException;

import android.content.Context;
import android.util.Log;

import com.ANT.MiddleWare.Entities.FileFragment;
import com.ANT.MiddleWare.Entities.FileFragment.FileFragmentException;
import com.ANT.MiddleWare.WiFi.WiFiBT.WiFiBT;
import com.ANT.MiddleWare.WiFi.WiFiBroad.WiFiBroad;
import com.ANT.MiddleWare.WiFi.WiFiNCP2.WiFiNCP2;
import com.ANT.MiddleWare.WiFi.WiFiTCP.WiFiTCP;

public class WiFiFactory {
	private static final String TAG = WiFiFactory.class.getSimpleName();

	public static enum WiFiType {
		EMPTY, TCP_ALL, BROAD, NCP2, BT
	}

	private static WiFiPulic instance = new WiFiEmpty(null);
	private static WiFiType ins_type = WiFiFactory.WiFiType.EMPTY;

	private WiFiFactory() {
	}

	public static void changeInstance(Context contect, WiFiType type)
			throws InterruptedException, IOException {
		synchronized (TAG) {
			if (type != ins_type) {
				Log.d(TAG, "INS_TYPE " + ins_type + " " + type);
				instance.destroy();
				instance = null;
				ins_type = type;
			}
			if (instance == null) {
				Log.d(TAG, "" + type);
				switch (type) {
				case TCP_ALL:
					instance = new WiFiTCP(contect);
					break;
				case BROAD:
					instance = new WiFiBroad(contect);
					break;
				case NCP2:
					instance = new WiFiNCP2(contect);
					break;
				case BT:
					instance = new WiFiBT(contect);
					break;
				default:
					instance = new WiFiEmpty(contect);
					break;
				}
			}
		}
	}

	public static void insertF(FileFragment fm) {
		synchronized (TAG) {
			instance.insertF(fm.clone());
		}
	}

	public static void notify(int seg, int start) {
		synchronized (TAG) {
			instance.notify(seg, start);
		}
	}

	public static void EmergencySend(byte[] data) throws FileFragmentException,
			IOException {
		synchronized (TAG) {
			instance.EmergencySend(data);
		}
	}
}
