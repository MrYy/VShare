package com.ANT.MiddleWare.DASHProxyServer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Stack;

import android.os.Environment;
import android.util.Log;

import com.ANT.MiddleWare.Celluar.CellularDown;
import com.ANT.MiddleWare.Entities.FileFragment;
import com.ANT.MiddleWare.Integrity.IntegrityCheck;
import com.ANT.MiddleWare.PartyPlayerActivity.MainFragment;
import com.ANT.MiddleWare.PartyPlayerActivity.test.CellularDownTest;
import com.ANT.MiddleWare.WiFi.WiFiFactory;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by ljw on 6/18/15.
 */
public class DashProxyServer extends NanoHTTPD {
	private static final String TAG = DashProxyServer.class.getSimpleName();

	public DashProxyServer() {
		super(9999);
		try {
			this.start();
			Log.e(TAG, "start");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Response serve(IHTTPSession session) {
		try {
			if (!getFileName(session, ".m3u8").equals("")) {
				Log.v("TAG", "filename" + session.getUri());
				return localFile("/video/4/index.m3u8");
			} else {
				Log.v("TAG", "DashProxy uri:" + session.getUri());
				String playist = getFileName(session, ".mp4");
				Log.v("TAG", "playist" + playist);
				switch (MainFragment.configureData.getWorkingMode()) {
				case LOCAL_MODE:
					String dir=Environment.getExternalStorageDirectory().getAbsolutePath()+"/video/4/";
					for(int i=1;i<6;i++){
						File file=new File(dir, i+".mp4");
						int len=(int) file.length();
						try {
							BufferedInputStream in = null;
							in = new BufferedInputStream(new FileInputStream(file));
							ByteArrayOutputStream out = new ByteArrayOutputStream(1024);

							byte[] temp = new byte[1024];
							int size = 0;
							while ((size = in.read(temp)) != -1) {
								out.write(temp, 0, size);
							}
							Log.d("TAG", "len:" + len + "in.avaiable():" + in.available());
							byte[] content = out.toByteArray();
							FileFragment f =new FileFragment(0,len,i,len);
							f.setData(content);
							IntegrityCheck IC = IntegrityCheck.getInstance();
							if (f.isTooBig()) {
								FileFragment[] fragArray = null;
								try {
									fragArray = f.split();
								} catch (FileFragment.FileFragmentException e) {
									e.printStackTrace();
								}
								for (FileFragment ff : fragArray) {
									IC.insert(i, ff);
								}
							} else {
								IC.insert(i, f);
							}
							IC.getSeg(i).checkIntegrity();
							in.close();
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (FileFragment.FileFragmentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					return localFile("/video/4/" + playist);
				case G_MDOE:
					IntegrityCheck iTC = IntegrityCheck.getInstance();
					int tmpp = Integer.parseInt(playist.substring(0, 1));
					byte[] tmp = iTC.getSegments(tmpp, CellularDown.CellType.GROUP);
					return newFixedLengthResponse(Response.Status.OK,
							"application/x-mpegurl", tmp);
				case JUNIT_TEST_MODE:
					Stack<FileFragment> s = CellularDownTest.fraList;
					if (s.empty())
						return newFixedLengthResponse("");
					FileFragment f = s.pop();
					Response res = newFixedLengthResponse(
							Response.Status.PARTIAL_CONTENT,
							"application/x-mpegurl", f.getData());
					res.addHeader(
							"Content-Range",
							"Content-Range " + f.getStartIndex() + "-"
									+ f.getStopIndex() + "/"
									+ CellularDownTest.base);
					return res;
				default:
					return newFixedLengthResponse("");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return newFixedLengthResponse("");
		}
	}

	private String getFileName(IHTTPSession session, String key) {
		String uri = session.getUri();
		String playlist = "";
		for (String s : uri.split("/")) {
			if (s.contains(key)) {
				playlist = s;
			}
		}
		return playlist;
	}

	private Response localFile(String str) throws IOException {
		FileInputStream fis = new FileInputStream(
				Environment.getExternalStorageDirectory() + str);
		int length = fis.available();
		return newFixedLengthResponse(Response.Status.OK,
				"application/x-mpegurl", fis, length);
	}

	private Response newFixedLengthResponse(Response.IStatus status,
			String mimeType, byte[] bytes) {
		return newFixedLengthResponse(status, mimeType,
				new ByteArrayInputStream(bytes), bytes.length);
	}
}
