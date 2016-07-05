package com.ANT.MiddleWare.WiFi.WiFiNCP2;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.ANT.MiddleWare.PartyPlayerActivity.R;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zxc on 2016/6/14.
 */
public class DumpUnil {
    public static final String fileDir = "/data/local/tcpdump/";
    public static final String fileTcpdump = fileDir + "tcpdump";
    public static final String fileOutPath = Environment.getExternalStorageDirectory().getPath() + "/";
    public static final String fileName = "capture.pcap";
    public static final String cmdTcpdump = fileTcpdump + ""+"\n";
    public static final String TAG = DumpUnil.class.getSimpleName();
    private Context context;
    private Process dumpProcess;

    public DumpUnil(Context context) {
        this.context = context;
    }

    public void startCapture() {
        initTcpDump();
        Log.d(TAG, "start cature");
        try {
            dumpProcess = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(dumpProcess.getOutputStream());
            os.writeBytes(cmdTcpdump);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void initTcpDump() {
        chkEnvironment();
        File file = new File(fileTcpdump);
        if (!file.exists()) {
            InputStream is = context.getResources().openRawResource(R.raw.tcpdump);
            try {
                FileOutputStream fos = new FileOutputStream(file);
                Log.v("tcpdump", "writing file to " + fileDir);
                byte[] buffer = new byte[8192];
                int cnt = 0;
                while ((cnt = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, cnt);
                }
                Log.v("tcpdump", "file is created!");
                fos.close();
                is.close();
                Runtime.getRuntime().exec("chmod 777 " + fileTcpdump);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void chkEnvironment() {
        if (!(new File(fileDir).exists())) {
            Process root = null;
            try {
                root = Runtime.getRuntime().exec("su");
                DataOutputStream dos = new DataOutputStream(root.getOutputStream());
                dos.writeBytes("mkdir -p " + fileDir + "\n");
                dos.flush();
                dos.writeBytes("chmod -R 777 " + fileDir + "\n");
                dos.flush();
                dos.writeBytes("exit\n");
                dos.flush();
                dos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
