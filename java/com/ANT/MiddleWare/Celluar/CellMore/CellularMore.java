package com.ANT.MiddleWare.Celluar.CellMore;

import android.util.Log;

import com.ANT.MiddleWare.Celluar.CellularDown;
import com.ANT.MiddleWare.Celluar.GroupCell.GroupCell;
import com.ANT.MiddleWare.Entities.FileFragment;
import com.ANT.MiddleWare.Entities.Segment;
import com.ANT.MiddleWare.Entities.Segment.SegmentException;
import com.ANT.MiddleWare.Integrity.IntegrityCheck;
import com.ANT.MiddleWare.PartyPlayerActivity.ViewVideoActivity;
import com.ANT.MiddleWare.PartyPlayerActivity.bean.Message;
import com.ANT.MiddleWare.PartyPlayerActivity.bean.StatisticsFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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
                Log.v(TAG, "no " + url + "  miss" + miss);
                HttpURLConnection connection = null;
                try {
                    URL uurl = new URL(IntegrityCheck.GROUP_TAG + "?filename=" + url
                            + ".mp4&sessionid=" + GroupCell.groupSession +
                            "&user_name=" + ViewVideoActivity.userName + "&miss=" + miss);

                    Log.d(TAG, "" + uurl);
                    connection = (HttpURLConnection) uurl.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(5000);
                    connection.setUseCaches(false);
                    connection.setDoInput(true);
                    connection.setRequestProperty("Accept-Encoding", "");
                    connection.setDoOutput(true);
//				Log.d(TAG,
//						"ResponseCode " + url + " "
//								+ connection.getResponseCode());

                    if (connection.getResponseCode() == 206) {
                        InputStream in = connection.getInputStream();
                        String videoName = connection.getHeaderField("video-name");
                        String videoRate = videoName.split("/")[1];
                        Log.d(TAG, "video rate:" + String.valueOf(videoRate));
                        Message msg = new Message();
                        msg.setMessage(ViewVideoActivity.SYSTEM_MESSAGE+videoRate);
                        ViewVideoActivity.insertReceiveMQ(msg);
                        String contentRange = connection.getHeaderField(
                                "Content-Range").toString();
//					Log.d(TAG, "Content-Range " + contentRange);
                        String range = contentRange.split(" ")[1].trim();
                        String start = range.split("-")[0];
                        String end = range.split("-")[1].split("/")[0];
                        String total = range.split("-")[1].split("/")[1];
                        int startOffset = Integer.parseInt(start);
                        int endOffset = Integer.parseInt(end);
                        int totalLength = Integer.parseInt(total);
                        int pieceLength = endOffset - startOffset;
                        if (pieceLength < 0 || totalLength < 0) {
                            return;
                        }
                        byte[] tmpbuff = new byte[pieceLength];
                        int hasRead = 0;
                        while (hasRead < pieceLength) {
                            hasRead += in.read(tmpbuff, hasRead, pieceLength
                                    - hasRead);
                        }
                        StatisticsFactory.getInstance(StatisticsFactory.Type.gReceive).add(hasRead / 10);
                        IC.setSegLength(url, totalLength);
                        FileFragment fm = new FileFragment(startOffset, endOffset,
                                url, totalLength);
                        Log.d(TAG, "" + url + " " + fm);
                        fm.setData(tmpbuff);
                        IC.insert(url, fm, 0);
                        IC.getSeg(url).checkIntegrity();
                        break;
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    Log.d(TAG, "MalformedURLException");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "IOException");
                } catch (FileFragment.FileFragmentException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        connection.disconnect();

                    } catch (ArrayIndexOutOfBoundsException ie) {

                    } catch (IllegalArgumentException ie) {

                    }
                }
                try {
                    Log.d(TAG, "start sleep");
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
