package com.ANT.MiddleWare.WiFi.WiFiTCP;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by David on 16/4/21.
 */
public class Method {
    public static void sendMessage(SocketChannel bSc,Message msgObj) {

        byte[] bytesObj = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(msgObj);
            bytesObj = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        ByteBuffer buf = ByteBuffer.allocate(bytesObj.length);
        buf.put(bytesObj);
        buf.flip();
        while (buf.hasRemaining()) {
            try {
                bSc.write(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static Message readMessage(SocketChannel sc) {
        try {
            ByteBuffer buf = ByteBuffer.allocate(1024);
            int byteRead = sc.read(buf);
            if (byteRead >0) {
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

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}