package com.ANT.MiddleWare.WiFi.WiFiTCP;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

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
    private Context context = null;
    public Client(InetAddress remoteAddress, int remotePort,Context context) {
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        this.context = context;
    }

    public Client(InetAddress remoteAddress,int remotePort ) {
        this.remotePort = remotePort;
        this.remoteAddress = remoteAddress;
    }

    @Override
    public void run() {
        SocketChannel sc = null;
        SelectionKey mKey = null;
        try {

            sc = SocketChannel.open();
            sc.connect(new InetSocketAddress(remoteAddress.getHostAddress(), remotePort));
            System.out.println("client connect");
            if (context != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "client is connected",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
            sc.configureBlocking(false);
            sc.socket().getTcpNoDelay();
            Selector selector = Selector.open();
            sc.register(selector, SelectionKey.OP_READ);
            boolean flag = true;
            while (flag) {
                if (!sc.isConnected()) {
                    Log.d(TAG, "client finish");
                    break;
                }

                int selected = selector.select();
                if (selected == 0) continue;
                Set<SelectionKey> mKeys = selector.selectedKeys();
                Iterator ite = mKeys.iterator();
                while (ite.hasNext()) {
                    mKey = (SelectionKey) ite.next();
                    if (mKey.isReadable()) {
                        //client is used to receive the fragment
                        Message msg = null;
                        try {
                            msg = Method.readMessage((SocketChannel) mKey.channel());
                        } catch (MyException e) {
                            flag = false;
                            break;
                        }
                        //msg null,finish the client .
                        if (msg == null) break;
                        if (msg.getType() == Message.Type.Message) {
                            //intent to send sessionid
                            Log.d(TAG, msg.getMessage() + ":" + msg.getCount());
                            Log.d(TAG, "运行线程数:" + String.valueOf(Thread.activeCount()));
                        } else {
                            System.out.println(msg.getType().getDescribe());
                            FileFragment ff = msg.getFragment();
                            Log.d("insert fragment",String.valueOf(ff.getSegmentID())+" "+ String.valueOf(ff.getStartIndex()));
//                            Log.d("check integrity", String.valueOf(IntegrityCheck.getInstance().getSeg(ff.getSegmentID()).checkIntegrity()));
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

