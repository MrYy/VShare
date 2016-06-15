package com.ANT.MiddleWare.WiFi.WiFiTCP;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.ANT.MiddleWare.Entities.FileFragment;
import com.ANT.MiddleWare.PartyPlayerActivity.R;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by David on 16/4/21.
 */
public class Method {
    private static final String TAG = Method.class.getSimpleName();
    public static   InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = { (byte)(0xff & hostAddress),
                (byte)(0xff & (hostAddress >> 8)),
                (byte)(0xff & (hostAddress >> 16)),
                (byte)(0xff & (hostAddress >> 24)) };

        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }
    public static void record(FileFragment f, String type) {
        record(f,type,"");
    }
    public static void record(FileFragment f,String type,String des) {
        String startOffset=String.valueOf(f.getStartIndex());
        String stopOffset=String.valueOf(f.getStopIndex());
        String segId=String.valueOf(f.getSegmentID());
        SimpleDateFormat format=new SimpleDateFormat("HH:mm:ss:SSS");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str = format.format(curDate);
        String text="count:"+des+" sId:"+segId+"\t start:"+startOffset+"\t stop:"
                +stopOffset+"\t time:"+System.currentTimeMillis()+"\t "+str+"\n";
        String dir= Environment.getExternalStorageDirectory().getAbsolutePath()+"/ltcptest/";
        File filedir=new File(dir);
        filedir.mkdir();
        File file=new File(dir, "l"+type+"_ch1_sp0.txt");
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file, true);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fos.write(text.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void sendMessage(SocketChannel bSc, byte[] bytesObj) throws MyException {
        Log.d("send size:", String.valueOf(bytesObj.length));
        ByteBuffer buf = ByteBuffer.allocate(bytesObj.length);
        buf.put(bytesObj);
        buf.flip();
        try {
            try {
                while (buf.hasRemaining()) {
                    bSc.write(buf);
                }
            } catch (SocketException e) {
                throw new MyException();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Message readMessage(SocketChannel sc) throws MyException {
        try {
            //33787
            //328699
            //message 696
            int wantSize = 164345;
            ByteBuffer buf = ByteBuffer.allocate(wantSize);
            //read in while
            int byteRead = 0 ;
            int i = 0;
            while (byteRead<buf.limit()) {
                int count = sc.read(buf);
                if (count < 0) break;
                byteRead += count;
                //check the last fragment,
                //try to wait  seconds.
//                if(count==0) {
//                    i++;
//                    TimeUnit.SECONDS.sleep(1);
//                    if (i>6) {
//                        if(byteRead==696) {  Log.d(TAG, "最后一片读取");  break;}
//                    }
//                }

                if(count!=0)  Log.d(TAG, "接收的字节：" + String.valueOf(byteRead));
            }
            if (byteRead > 0) {
                buf.flip();
                byte[] content = new byte[buf.limit()];
                buf.get(content);
                ByteArrayInputStream byteArrayInputStream =
                        new ByteArrayInputStream(content);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                Message message = (Message) objectInputStream.readObject();
                objectInputStream.close();
                byteArrayInputStream.close();
                buf.clear();
                return message;

            }

        } catch (StreamCorruptedException e) {
            // Thrown when control information that was read from an object
            // stream violates internal consistency checks.
//            e.printStackTrace();
            Log.d(TAG, "下载完毕");
        } catch (EOFException e) {
            //exception because of the end of stream
            //reconnect
            try {
                sc.socket().close();
                new Thread(new Client(InetAddress.getByName("192.168.1.51"), 12345)).start();
                throw new MyException();
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
//        catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return null;
    }

    public static void changeApState(Context context, WifiManager wifiManager,Boolean open) {
        WifiConfiguration apConfig = new WifiConfiguration();
        apConfig.SSID = context.getString(R.string.ap_ssid);
        apConfig.preSharedKey = context.getString(R.string.ap_password);
        apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        try {
            java.lang.reflect.Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            try {
                method.invoke(wifiManager,apConfig,open);
                if (open) {
                    Toast.makeText(context, "ap is on", Toast.LENGTH_SHORT).show();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}