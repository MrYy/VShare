package com.ANT.MiddleWare.WiFi.WiFiTCP;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.ANT.MiddleWare.Entities.FileFragment;
import com.ANT.MiddleWare.Integrity.IntegrityCheck;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
	private HashMap<String, FileFragment> fragMap = new LinkedHashMap<>();
	private Stack<Message> fragMsg = new Stack<>();
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
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					serverThread();
				}
			}).start();
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

	/**
	 * The role of server, is only designed to send fragment to the client.
	 * The role of a client , is only to receive the message and tell the server whether or not want the fragment.
	 * As a result , in theory ,a node is not only a server but also a client.
	 * And the connection method above , which is 'switch' statement should be changed to button.
	 *
	 */
	private  void serverThread() {
		try {
			System.out.println("start listen");
			System.out.println(ip);
			InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName(ip), 12345);
			Selector selector = Selector.open();
			ServerSocketChannel ssc = ServerSocketChannel.open();
			ssc.configureBlocking(false);
			ssc.socket().bind(addr);
			ssc.register(selector, SelectionKey.OP_ACCEPT);
			//client
			while (true) {
				int readyChannel = selector.select();
				if (readyChannel == 0) continue;
				Set<SelectionKey> selectedChannel = selector.selectedKeys();
				Iterator ite = selectedChannel.iterator();
				while (ite.hasNext()) {
					SelectionKey mKey = (SelectionKey) ite.next();
					if (mKey.isAcceptable()) {
						SocketChannel ss = ((ServerSocketChannel) mKey.channel()).accept();
						ss.configureBlocking(false);
						ss.register(mKey.selector(), SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);
					} else if (mKey.isReadable()) {
						SocketChannel sc = ((SocketChannel) mKey.channel());
						Message message = Method.readMessage(sc);
						if (message==null) return;
						int seg = message.getSegIndex();
						int frag = message.getStartFragmentIndex();
						String key = String.valueOf(seg) + String.valueOf(frag);
						if (message.getMsgType() == Message.MessageType.WANT) {
							//client want the fragment
							//push the fragment message to stack
							//ready to send
							Message reply = new Message();
							reply.setFragment(fragMap.get(key));
							fragMsg.push(reply);
						} //no answer if not want
						fragMap.remove(key);
					} else if (mKey.isWritable()) {
						SocketChannel sc = (SocketChannel) mKey.channel();
						if (fragMsg.empty()) {
							//has no fragment message ready to send
							//send a message to ask whether client want
							Message msgObj = new Message();
							FileFragment ff = taskList.pop();
							msgObj.setMsgType(Message.MessageType.GIVE, ff.getSegmentID(), ff.getStartIndex());
							fragMap.put(String.valueOf(ff.getSegmentID())
									+String.valueOf(ff.getStartIndex()),  ff);
							Method.sendMessage(sc, msgObj);
							TimeUnit.SECONDS.sleep(1);
						}else {
							//has fragment to send
							//send fragment to client
							Method.sendMessage(sc,fragMsg.pop());
						}

					}
					ite.remove();
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
