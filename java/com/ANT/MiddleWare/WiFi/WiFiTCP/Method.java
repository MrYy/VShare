package com.ANT.MiddleWare.WiFi.WiFiTCP;

import android.os.Environment;
import android.util.Log;

import com.ANT.MiddleWare.Entities.FileFragment;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
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

    public static void record(FileFragment f,String type) {
        String startOffset=String.valueOf(f.getStartIndex());
        String stopOffset=String.valueOf(f.getStopIndex());
        String segId=String.valueOf(f.getSegmentID());
        SimpleDateFormat format=new SimpleDateFormat("HH:mm:ss:SSS");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str = format.format(curDate);
        String text="sId:"+segId+"\t start:"+startOffset+"\t stop:"
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

}