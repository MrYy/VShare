package com.ANT.MiddleWare.WiFi.WiFiTCP;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.ANT.MiddleWare.Entities.FileFragment;
import com.ANT.MiddleWare.WiFi.WiFiFactory;
import com.ANT.MiddleWare.WiFi.WiFiPulic;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class WiFiTCP extends  WiFiPulic {

private static final String TAG=WiFiTCP.class.getSimpleName();
	private Context context=null;
	private TelephonyManager tm;
	private Process proc;
	private String ip ;
	private WifiManager wifi;
	private PipedInputStream pi = new PipedInputStream();
	private PipedOutputStream po = new PipedOutputStream();
	public static final int EMERGEN_SEND_TAG = -2;
	public static final int FRAG_REQST_TAG = -3;
	public WiFiTCP(final Context contect) {
		super(contect);
		this.context = contect;
		makeToast("I am TCP");

		tm = (TelephonyManager) contect
				.getSystemService(Activity.TELEPHONY_SERVICE);
		try{pi.connect(po);
		}catch(IOException e){
			e.printStackTrace();}

		String s = tm.getDeviceId();
		int len = s.length();
		final int number = Integer.parseInt(s.substring(len - 2));
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
			ServerThread st = new ServerThread(WiFiTCP.this,ip);
			st.start();
			Client client = null;
			switch (number) {
				case 16:
					client=new Client(InetAddress.getByName("192.168.1.89"), 12345);
				    new Thread(client).start();
					break;
				case 51:
					client=new Client(InetAddress.getByName("192.168.1.89"), 12345);
					new Thread(client).start();
					break;
				case 89:
					client = new Client(InetAddress.getByName("192.168.1.51"), 12345);
					new Thread(client).start();
				default:
					Toast.makeText(contect, "无法获得本机ip", Toast.LENGTH_SHORT).show();
					break;

			}



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
	private void makeToast(String msg){
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	public Stack<FileFragment> getTaskList() {
		return taskList;
	}






	@Override
	public void destroy() throws InterruptedException {

	}

	@Override
	public void notify(int seg, int start) {
	}

	@Override
	public void EmergencySend(byte[] data) throws FileFragment.FileFragmentException, IOException {

	}
}
