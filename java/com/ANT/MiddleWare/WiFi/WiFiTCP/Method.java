package com.ANT.MiddleWare.WiFi.WiFiTCP;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

/**
 * Created by David on 16/4/21.
 */
public class Method {
    private static final String TAG = Method.class.getSimpleName();
    public static void sendMessage(SocketChannel bSc, byte[] bytesObj) throws MyException {
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
            int wantSize = 328699;
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