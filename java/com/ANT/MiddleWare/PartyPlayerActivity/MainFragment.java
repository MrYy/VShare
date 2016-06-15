package com.ANT.MiddleWare.PartyPlayerActivity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.ANT.MiddleWare.DASHProxyServer.DashProxyServer;
import com.ANT.MiddleWare.Entities.FileFragment.FileFragmentException;
import com.ANT.MiddleWare.Integrity.IntegrityCheck;
import com.ANT.MiddleWare.WiFi.WiFiFactory;
import com.ANT.MiddleWare.WiFi.WiFiFactory.WiFiType;
import com.ANT.MiddleWare.WiFi.WiFiNCP2.WiFiNCP2;
import com.ANT.MiddleWare.WiFi.WiFiTCP.Client;
import com.ANT.MiddleWare.WiFi.WiFiTCP.ServerThread;
import com.ANT.MiddleWare.WiFi.WiFiTCP.WiFiTCP;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressLint("NewApi")
public class MainFragment extends Fragment {
	private static final String TAG = MainFragment.class.getSimpleName();

	private Button btStart;
	private Button btStop;
	// private EditText etUrl;
	// private Button btConfirm;
	private Button btCaptain;
	private Button btPlayer;
	private Button clearBuff;
	// private Button btLow;
	// private Button btMid;
	// private Button btHigh;

	private DashProxyServer server = new DashProxyServer();
	public static ConfigureData configureData = new ConfigureData(null);
	private static final boolean SEVER_START_TAG = true;
	private static final boolean SEVER_STOP_TAG = false;
	private static final String SETTING_DIALOG_TAG = "setting";
	public static String rateTag = "";
	public static String taskID = "" + new Date().getTime();

	private Handler myHandler;
	private boolean adhocSelect = false;
	private boolean tcpSelect=false;
	private boolean ncp2Ap = false;
	private List<String> list = new ArrayList<String>();
	private ArrayAdapter<String> adapter;
	private Spinner mySpinner;
	private ApObservable apObservable;

	public class ApObservable extends Observable {
		private boolean isAp;

		public boolean isAp() {
			return isAp;
		}

		public void setAp(boolean ap) {
			isAp = ap;
			setChanged();
			notifyObservers();
		}
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		myHandler = new Handler();

		list.add("NONE");
		list.add("ADHOC MODE");
		list.add("BT MODE");
		list.add("NCP2 MODE");
		list.add("TCP MODE");

	}

	@SuppressLint("NewApi")
	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup parent,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_main, parent, false);

		btStart = (Button) v.findViewById(R.id.btStart);
		btStop = (Button) v.findViewById(R.id.btStop);
		// btConfirm = (Button) v.findViewById(R.id.btConfirm);
		btPlayer = (Button) v.findViewById(R.id.btChoose);
		btCaptain = (Button) v.findViewById(R.id.btCaptain);
		clearBuff=(Button)v.findViewById(R.id.btClear);
		// btCaptain.setClickable(false);
		// etUrl = (EditText) v.findViewById(R.id.url_edit_text);

		mySpinner = (Spinner) v.findViewById(R.id.Spinner_wifi_);
		adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, list);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mySpinner.setAdapter(adapter);

		mySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				switch (arg2) {
				case 0: // none
					adhocSelect = false;
					tcpSelect = false;
					break;
				case 1: // adhoc
					adhocSelect = true;
					tcpSelect = false;
					try {
						WiFiFactory.changeInstance(getActivity(),
								WiFiType.BROAD);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				case 2: // bt
					adhocSelect = false;
					tcpSelect = false;
					break;
				case 3: // ncp2
					adhocSelect = false;
					tcpSelect = false;
					ncp2Ap = true;
					btCaptain.setText("I am AP");
					try {
						WiFiFactory.changeInstance(getActivity(),WiFiType.NCP2);

					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					apObservable = new ApObservable();
					apObservable.setAp(false);
					apObservable.addObserver(new WiFiNCP2.ApObserver());
					break;
				case 4: // tcp
					tcpSelect =true;
					adhocSelect = false;
					try {
						btCaptain.setText("tcp send");
						WiFiFactory.changeInstance(getActivity(),
								WiFiType.TCP_ALL);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

					break;

				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				// arg0.setSelection(0);
			}
		});

		btCaptain.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (adhocSelect) {

					try {
						WiFiFactory.EmergencySend("I am Captain!"
								.getBytes("UTF-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (FileFragmentException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					myHandler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							Toast.makeText(getActivity(), "I am captain!",
									Toast.LENGTH_SHORT).show();
						}
					});
				} else if (tcpSelect) {
					TelephonyManager tm = (TelephonyManager) getActivity().
							getSystemService(Activity.TELEPHONY_SERVICE);
					String s = tm.getDeviceId();
					int len = s.length();
					final int number = Integer.parseInt(s.substring(len - 2));
					String ip = "192.168.1." + number;
					Log.v(TAG, "ip " + ip);
					WiFiTCP.init();
					try {
						Client client = null;
						String linkIp = "";
						switch (number) {
							case 71:
								linkIp = "192.168.1.89";
								break;
							case 40:
								linkIp = "192.168.1.89";
								break;
							case 89:
								linkIp = "192.168.1.40";
								break;
							case 51:
								linkIp = "192.168.1.89";
								break;
							default:
								Toast.makeText(getActivity(), "无法获得本机ip", Toast.LENGTH_SHORT).show();
								break;

						}
						if (!linkIp.equals("")) {
							client = new Client(InetAddress.getByName(linkIp), 12345,getActivity());
							new Thread(client).start();
							WiFiTCP.getLinks().remove(linkIp);
						}
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}

				} else if (ncp2Ap) {
					Log.d(TAG, "i am ap");
					//open ap
					WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
					wifiManager.setWifiEnabled(false);
					com.ANT.MiddleWare.WiFi.WiFiTCP.Method.changeApState(getActivity(),wifiManager,true);
					apObservable.setAp(true);


				}
			}});


		btStart.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				try {
					if (!configureData.isServiceAlive())
						server.start();
				} catch (IOException e) {
					e.printStackTrace();
				}

				sendNotification(SEVER_START_TAG);
				configureData.setServiceAlive(true);
				getActivity().getActionBar().setSubtitle("Service is running");

			}
		});

		btStop.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (server != null) {
					if (configureData.isServiceAlive())
						server.stop();
				}

				sendNotification(SEVER_STOP_TAG);
				getActivity().getActionBar().setSubtitle(
						"Service is not running");
				configureData.setServiceAlive(false);
			}
		});

		configureData.setUrl("http://127.0.0.1:9999/4/s-1.mp4");



		btPlayer.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (null == configureData.getUrl()) {
					Toast.makeText(getActivity(), "Please set URL!",
							Toast.LENGTH_SHORT).show();
					// }else if(!isNetworkAvailable()){
					// Toast.makeText(getActivity(),
					// "Please connect to the network!",
					// Toast.LENGTH_SHORT).show();
				} else if (!configureData.isServiceAlive()) {
					Toast.makeText(getActivity(), "Please start Service!",
							Toast.LENGTH_SHORT).show();
				} else if (!isLegel(configureData.getUrl())) {
					Toast.makeText(getActivity(), "Please give correct URL!",
							Toast.LENGTH_SHORT).show();
				} else {
					Intent j = new Intent();
					j.setDataAndType(Uri.parse(configureData.getUrl()),
							"video/*");
					// j = Intent.createChooser(j,
					// "Please select media player");
					startActivity(j);
				}

			}
		});


		clearBuff.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//PHP端清除缓存
				new Thread(new Runnable() {
					@Override
					public void run() {
						HttpURLConnection connection = null;
						try {
							URL uurl = new URL(IntegrityCheck.GROUP_TAG + "?filename=4.mp4&sessionid=lykfr9oyqipq2q3tvy14616291918cw"+
									"&rate=" + MainFragment.rateTag+"&clear=1");
							connection = (HttpURLConnection)uurl.openConnection();
							connection.setRequestMethod("GET");
							connection.setConnectTimeout(5000);
							connection.setUseCaches(false);
							connection.setDoInput(true);
							connection.setRequestProperty("Accept-Encoding", "");
							connection.setDoOutput(true);
							if (connection.getResponseCode() == 200) {
								//php should change ,otherwise only one host get the 200,and the others don't know download ends.
								Log.d(TAG, "restart success");
							}
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}finally {
							if (connection != null) {
								connection.disconnect();
							}
						}
					}
				}).start();
			}
		});

		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.main, menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		// no inspection SimplifiableIfStatement
		if (id == R.id.actionsettings) {
			android.app.FragmentManager fm = getActivity().getFragmentManager();
			SettingDialog stDialog = new SettingDialog();
			stDialog.show(fm, SETTING_DIALOG_TAG);

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void sendNotification(boolean status) {

		Notification notification;

		if (status) {
			notification = new NotificationCompat.Builder(getActivity())
					.setTicker("InterComponent Service Started!")
					.setSmallIcon(android.R.drawable.ic_menu_report_image)
					.setContentTitle("InterComponent Status")
					.setContentText(
							"InterComponent Service has Started, and is caputuring NC packets")
					.setAutoCancel(true).build();
		} else {
			notification = new NotificationCompat.Builder(getActivity())
					.setTicker("InterComponent Service Stopped!")
					.setSmallIcon(android.R.drawable.ic_menu_report_image)
					.setContentTitle("InterComponent Status")
					.setContentText(
							"InterComponent Service has Stopped, and is not caputuring NC packets")
					.setAutoCancel(true).build();
		}
		NotificationManager notificationManager = (NotificationManager) getActivity()
				.getSystemService(Activity.NOTIFICATION_SERVICE);
		notificationManager.notify(0, notification);

	}

	@SuppressWarnings("deprecation")
	public boolean isNetworkAvailable() {
		boolean available = false;
		ConnectivityManager cm = (ConnectivityManager) getActivity()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		WifiManager wm = (WifiManager) getActivity().getSystemService(
				Context.WIFI_SERVICE);
		available = cm.getBackgroundDataSetting()
				&& cm.getActiveNetworkInfo() != null;
		available = available && wm.isWifiEnabled();

		return available;
	}

	public boolean isLegel(String url) {
		boolean legel = false;

		String[] s = url.split("://");

		Log.v(TAG, "s0 " + s[0]);

		if (s[0].toLowerCase(Locale.CHINA).equals("http")) {
			legel = true;

		}

		return legel;
	}

}
