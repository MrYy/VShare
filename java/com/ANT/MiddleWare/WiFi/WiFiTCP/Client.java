package com.ANT.MiddleWare.WiFi.WiFiTCP;

import android.util.Log;

import com.ANT.MiddleWare.Entities.FileFragment;
import com.ANT.MiddleWare.Integrity.IntegrityCheck;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by David on 16/4/21.
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
            System.out.println("client connect");
            sc.configureBlocking(false);
            Selector selector = Selector.open();
            sc.register(selector, SelectionKey.OP_READ);
            while (!Thread.interrupted()) {
                int selected = selector.select();
                if (selected == 0) continue;
                Set<SelectionKey> mKeys = selector.selectedKeys();
                Iterator ite = mKeys.iterator();
                while (ite.hasNext()) {
                    mKey = (SelectionKey) ite.next();
                    if (mKey.isReadable()) {
                        //client is used to receive the fragment
                        Message msg = Method.readMessage((SocketChannel) mKey.channel());
                        if (msg==null) continue;
                        if(msg.getType()== Message.Type.Message) {
                            Log.d(TAG, msg.getMessage());
                        }else {
                            System.out.println(msg.getType().getDescribe());
                            FileFragment ff = msg.getFragment();
                            Log.d("insert fragment", String.valueOf(ff.getStartIndex()));
                            IntegrityCheck.getInstance().insert(ff.getSegmentID(), ff, this);
                        }

                    }
                    ite.remove();
                }
            }
        } catch (IOException e) {
            if (mKey != null) {
                mKey.cancel();
                if (mKey.channel() != null) {
                    try {
                        mKey.channel().close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

    }
}

