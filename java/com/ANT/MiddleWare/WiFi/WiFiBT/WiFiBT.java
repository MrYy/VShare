package com.ANT.MiddleWare.WiFi.WiFiBT;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.ANT.MiddleWare.WiFi.WiFiPulic;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WiFiBT extends WiFiPulic {
	private static final String TAG = WiFiBT.class.getSimpleName();
	private Context context=null;
	private TelephonyManager tm;
	private Process proc;
	private String ip ;
	private WifiManager wifi;
	private static ExecutorService es = Executors.newCachedThreadPool();
	private  ButtonInterface buttonListener = null;
	public interface ButtonInterface{
		public void onClick();
	}
	
	public WiFiBT(Context contect) {
		super(contect);
		this.context = contect;
		makeToast("I am BT");
		tm = (TelephonyManager) contect
				.getSystemService(Activity.TELEPHONY_SERVICE);

		String s = tm.getDeviceId();
		int len = s.length();
		int number = Integer.parseInt(s.substring(len - 2));
		ip = "192.168.1." + number;
		Log.v(TAG, "ip " + ip);
		try {
			proc = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(proc.getOutputStream());
			os.writeBytes("netcfg wlan0 up\n");
			os.writeBytes("wpa_supplicant -iwlan0 -c/data/misc/wifi/wpa_supplicant.conf -B\n");
			os.writeBytes("ifconfig wlan0 " + ip + " netmask 255.255.255.0\n");
			os.writeBytes("exit\n");
			os.flush();
			proc.waitFor();

			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
				}
			}).start();



		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		wifi = (WifiManager) contect.getSystemService(Context.WIFI_SERVICE);
		if (wifi != null) {
			WifiManager.WifiLock lock = wifi
					.createWifiLock("Log_Tag");
			lock.acquire();
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
	
	private void makeToast(String msg){
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}



}
