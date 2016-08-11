package com.ANT.MiddleWare.DASHProxyServer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Stack;

import android.os.Environment;
import android.util.Log;

import com.ANT.MiddleWare.Celluar.CellularDown;
import com.ANT.MiddleWare.Celluar.GroupCell.GroupCell;
import com.ANT.MiddleWare.Entities.FileFragment;
import com.ANT.MiddleWare.Integrity.IntegrityCheck;
import com.ANT.MiddleWare.PartyPlayerActivity.ViewVideoActivity;
import com.ANT.MiddleWare.PartyPlayerActivity.bean.Message;
import com.ANT.MiddleWare.PartyPlayerActivity.test.CellularDownTest;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by ljw on 6/18/15.
 */
public class DashProxyServer extends NanoHTTPD {
	private static final String TAG = DashProxyServer.class.getSimpleName();
	public static final String SERVER_ADDR = "http://yyzwt.cn:12345/dash";
	public DashProxyServer() {
		super(9999);
		try {
			this.start();
			Log.e(TAG, "start");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private String SHA(final String strText, final String strType)
	{
		// 返回值
		String strResult = null;

		// 是否是有效字符串
		if (strText != null && strText.length() > 0)
		{
			try
			{
				// SHA 加密开始
				// 创建加密对象 并傳入加密類型
				MessageDigest messageDigest = MessageDigest.getInstance(strType);
				// 传入要加密的字符串
				messageDigest.update(strText.getBytes());
				// 得到 byte 類型结果
				byte byteBuffer[] = messageDigest.digest();

				// 將 byte 轉換爲 string
				StringBuffer strHexString = new StringBuffer();
				// 遍歷 byte buffer
				for (int i = 0; i < byteBuffer.length; i++)
				{
					String hex = Integer.toHexString(0xff & byteBuffer[i]);
					if (hex.length() == 1)
					{
						strHexString.append('0');
					}
					strHexString.append(hex);
				}
				// 得到返回結果
				strResult = strHexString.toString();
			}
			catch (NoSuchAlgorithmException e)
			{
				e.printStackTrace();
			}
		}

		return strResult;
	}


	@Override
	public Response serve(IHTTPSession session) {
		try {
			if (!getFileName(session, ".m3u8").equals("")) {
				Log.v("TAG", "filename" + session.getUri());
				IntegrityCheck.getInstance().clear();
				ViewVideoActivity.sendMessageQueue.clear();
				ViewVideoActivity.taskMessageQueue.clear();
				if(ViewVideoActivity.isOwner){
					//send message
					Message msg = new Message();
					String groupSession = SHA(ViewVideoActivity.userName+System.currentTimeMillis(),"SHA-256");
					msg.setMessage(ViewVideoActivity.SYSTEM_MESSAGE_SHARE_NETWORK+"http://127.0.0.1:9999"+session.getUri()+"~"
					+groupSession);
					GroupCell.groupSession = groupSession;
					Log.d("TAG", "group session is :" + groupSession);
					Log.d("TAG", msg.getMessage());
					ViewVideoActivity.sendMsg(msg);
				}
				return downloadM3U8(session.getUri());

			} else {
				Log.v("TAG", "DashProxy uri:" + session.getUri());
				String playist = getFileName(session, ".mp4");
				Log.v("TAG", "playist" + playist);
				switch (ViewVideoActivity.configureData.getWorkingMode()) {
					case FAKE_MODE:
						String dir=Environment.getExternalStorageDirectory().getAbsolutePath()+"/video/4/";
						int i=Integer.parseInt(session.getUri().substring(1, 2).toString());
						Log.d("pianming",Integer.toString(i));
//                    if (i > 1) {
//                        while (!IntegrityCheck.getInstance().getSeg(i - 1).checkIntegrity()) {}
//                    }
						File file=new File(dir, i+".mp4");
						int len=(int) file.length();
						try {
							BufferedInputStream in = null;
							try {
								in = new BufferedInputStream(new FileInputStream(file));
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
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
//test code
						int tmppl = Integer.parseInt(playist.substring(0, 1));
						byte[] tmpl = IntegrityCheck.getInstance().getSegments(tmppl, CellularDown.CellType.GROUP);
						return newFixedLengthResponse(Response.Status.OK,
								"application/x-mpegurl", tmpl);
				case LOCAL_MODE:
					return newFixedLengthResponse(Response.Status.OK,
							"application/x-mpegurl", IntegrityCheck.getInstance().getSegments(com.ANT.MiddleWare.PartyPlayerActivity.util.Method.LOCAL_VIDEO_SEGID));
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

	private Response downloadM3U8(String uri) throws IOException {
		String path = "/video"+uri;
		String remotePath = SERVER_ADDR + path;
		String localPath = Environment.getExternalStorageDirectory() + path;
		File file = new File(localPath);
		if (!file.isFile()){
			URL urlfile;
			HttpURLConnection httpUrl;
			BufferedInputStream bis;
			BufferedOutputStream bos;
			urlfile = new URL(remotePath);
			httpUrl = (HttpURLConnection)urlfile.openConnection();
			httpUrl.connect();
			bis = new BufferedInputStream(httpUrl.getInputStream());
			bos = new BufferedOutputStream(new FileOutputStream(file));
			File f = new File(localPath);
			int len = 1024;
			byte[] b = new byte[len];
			while ((len = bis.read(b)) != -1) {
				bos.write(b,0,len);
			}
			bos.flush();
			bis.close();
			httpUrl.disconnect();
			Log.d(TAG, "finish download m3u8");
		}
		return localFile(localPath);
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
				str);
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
