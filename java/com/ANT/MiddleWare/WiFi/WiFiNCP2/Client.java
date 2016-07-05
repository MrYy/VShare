package com.ANT.MiddleWare.WiFi.WiFiNCP2;

import android.util.Log;

import com.ANT.MiddleWare.WiFi.WiFiTCP.Message;
import com.ANT.MiddleWare.WiFi.WiFiTCP.Method;
import com.ANT.MiddleWare.WiFi.WiFiTCP.MyException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by zxc on 2016/6/15.
 */
public class Client implements Runnable {
    private InetAddress remoteAddress;
    private int remotePort;
    private static final String TAG = Client.class.getSimpleName();

    public Client(InetAddress remoteAddress, int remotePort) {
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
    }

    @Override
    public void run() {
        SocketChannel sc = null;
        SelectionKey mKey = null;

        try {
            sc = SocketChannel.open();
            sc.connect(new InetSocketAddress(remoteAddress.getHostAddress(), remotePort));
            sc.configureBlocking(false);
            sc.socket().getTcpNoDelay();
            Selector selector = Selector.open();
            sc.register(selector, SelectionKey.OP_READ);
            while (true) {
                int selected = selector.select();
                if(selected==0) continue;
                Set<SelectionKey> mKeys =selector.selectedKeys();
                Iterator ite = mKeys.iterator();
                while (ite.hasNext()) {
                    mKey = (SelectionKey) ite.next();
                    if (mKey.isReadable()) {
                        Message msg = null;
                        msg = Method.readMessage((SocketChannel) mKey.channel(),636);
                        if (msg != null) {
                            Log.d(TAG, "message:" + msg.getMessage() + "  count:" + msg.getCount());
                        }
                    }
                    ite.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
    }
}
