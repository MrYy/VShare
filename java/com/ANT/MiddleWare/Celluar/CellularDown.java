package com.ANT.MiddleWare.Celluar;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.util.Log;

import com.ANT.MiddleWare.Celluar.CellMore.CellularMore;
import com.ANT.MiddleWare.Celluar.GroupCell.GroupCell;
import com.ANT.MiddleWare.Celluar.SingleCell.SingleCell;
import com.ANT.MiddleWare.Celluar.WiFiMore.WiFiMore;

public class CellularDown {
	private static final String TAG = CellularDown.class.getSimpleName();
	private static final ExecutorService cachedThreadPool = Executors
			.newCachedThreadPool();

	public static enum CellType {
		CellMore, WiFiMore, Single, DASH, GROUP, NOCELL
	}

	private CellularDown() {
	}

	public static void queryFragment(CellType type, int url) {
		Log.d(TAG, "" + type + " " + url);
		switch (type) {
		case Single:
			cachedThreadPool.execute(new SingleCell(url));
			break;
		case CellMore:
			cachedThreadPool.execute(new CellularMore(url));
			break;
		case WiFiMore:
			cachedThreadPool.execute(new WiFiMore(url));
			break;
		case NOCELL:
			break;
		case DASH:
			break;
		case GROUP:
			cachedThreadPool.execute(new GroupCell(url));
			break;
		default:
			break;
		}
	}

}
