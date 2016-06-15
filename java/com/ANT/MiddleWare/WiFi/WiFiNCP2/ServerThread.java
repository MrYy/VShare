package com.ANT.MiddleWare.WiFi.WiFiNCP2;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by zxc on 2016/6/15.
 */
public class ServerThread extends Thread{
    public static final String TAG = ServerThread.class.getSimpleName();
    private InetAddress ip;
    private Context context;
    public ServerThread(InetAddress ip, Context context) {
        this.ip = ip;
        this.context = context;
    }

    @Override
    public void run() {
        super.run();
        Log.d(TAG, "server is running");
        InetSocketAddress addr = new InetSocketAddress(ip.getHostAddress(),12345);
        try {
            Selector selector = Selector.open();
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.socket().bind(addr);
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                int readChannel = selector.select();
                if(readChannel==0) continue;;
                Set<SelectionKey> selectedChannel = selector.selectedKeys();
                Iterator ite = selectedChannel.iterator();
                while (ite.hasNext()) {
                    SelectionKey mKey = (SelectionKey) ite.next();
                    if (mKey.isAcceptable()) {
                        Log.d(TAG, "ap accept new socket");
                        SocketChannel sc = ((ServerSocketChannel) mKey.channel()).accept();
                        Log.d(TAG, "client address:" + sc.socket().getInetAddress().toString());
                        ((Activity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "has a client in", Toast.LENGTH_SHORT).show();
                            }
                        });
                        sc.configureBlocking(false);
                        sc.register(mKey.selector(), SelectionKey.OP_WRITE);
                    }

                    ite.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
