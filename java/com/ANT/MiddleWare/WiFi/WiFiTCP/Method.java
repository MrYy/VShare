package com.ANT.MiddleWare.WiFi.WiFiTCP;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by David on 16/4/21.
 */
public class Method {
    private static final String TAG = Method.class.getSimpleName();
    public static void sendMessage(SocketChannel bSc, byte[] bytesObj) {
        ByteBuffer buf = ByteBuffer.allocate(bytesObj.length);
        buf.put(bytesObj);
        buf.flip();
        try {
            bSc.write(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Message readMessage(SocketChannel sc) {
        try {
            int wantSize = 326;
            ByteBuffer buf = ByteBuffer.allocate(wantSize);
            int byteRead = sc.read(buf);
            Log.d(TAG, "接收的字节：" + String.valueOf(byteRead));

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
            e.printStackTrace();
        } catch (EOFException e) {
            //exception because of the end of stream
            //reconnect
            try {
                new Thread(new Client(InetAddress.getByName("192.168.1.89"), 12345)).start();
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            }
        }catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}