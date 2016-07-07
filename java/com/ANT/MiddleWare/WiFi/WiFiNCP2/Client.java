package com.ANT.MiddleWare.WiFi.WiFiNCP2;

import android.util.Log;

import com.ANT.MiddleWare.Entities.FileFragment;
import com.ANT.MiddleWare.Integrity.IntegrityCheck;
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
    private int index;
    public Client(InetAddress remoteAddress, int remotePort) {
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
    }

    @Override
    public void run() {
        SocketChannel sc = null;
        SelectionKey mKey = null;
        Log.d(TAG, "try to connect");
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
                        //在这里准备添加报头
                        SocketChannel mSc = (SocketChannel) mKey.channel();
                        Message msgHeader = Method.readMessage(mSc, 250);
                        Log.d(TAG, "message length:" + msgHeader.getMsgLength());
                        Message msg = Method.readMessage(mSc,msgHeader.getMsgLength());
                        switch (msg.getType()) {
                            case Message:
                                Log.d(TAG, msg.getMessage());
                                break;
                            case Fragment:
                                FileFragment ff = msg.getFragment();
                                Method.record(ff,"receive",String.valueOf(++index));
                                Log.d("insert fragment",String.valueOf(ff.getSegmentID())+" "+ String.valueOf(ff.getStartIndex()));
//                            Log.d("check integrity", String.valueOf(IntegrityCheck.getInstance().getSeg(ff.getSegmentID()).checkIntegrity()));
                                IntegrityCheck.getInstance().insert(ff.getSegmentID(), ff, this);
                                break;

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
