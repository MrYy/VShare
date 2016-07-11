package com.ANT.MiddleWare.WiFi.WiFiNCP2;

import android.util.Log;

import com.ANT.MiddleWare.Entities.FileFragment;
import com.ANT.MiddleWare.PartyPlayerActivity.ViewVideoActivity;
import com.ANT.MiddleWare.PartyPlayerActivity.bean.Message;
import com.ANT.MiddleWare.PartyPlayerActivity.util.Method;
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
        Message name = new Message();
        name.setName(ViewVideoActivity.userName);
        ViewVideoActivity.sendMsg(name);
        try {
            sc = SocketChannel.open();
            sc.connect(new InetSocketAddress(remoteAddress.getHostAddress(), remotePort));
            sc.configureBlocking(false);
            sc.socket().getTcpNoDelay();
            Selector selector = Selector.open();
            sc.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE);
            while (true) {
                int selected = selector.select();
                if (selected == 0) continue;
                Set<SelectionKey> mKeys = selector.selectedKeys();
                Iterator ite = mKeys.iterator();
                while (ite.hasNext()) {
                    mKey = (SelectionKey) ite.next();
                        if (mKey.isReadable()) {
                        //在这里准备添加报头
                            Log.d(TAG, "client is reading");
                            SocketChannel mSc = (SocketChannel) mKey.channel();
                        Method.read(mSc);
                    } else if (mKey.isWritable()) {
//                            Log.d(TAG, "client is writing");
                        SocketChannel mSc = (SocketChannel)mKey.channel();
//                        Message testMsg = new Message();
//                        testMsg.setMessage(Method.getRandomString(300));
//                        ViewVideoActivity.sendMsg(testMsg);
                        try {
                            while (!ViewVideoActivity.sendMessageQueue.isEmpty()) {
                                Message msg = ViewVideoActivity.sendMessageQueue.poll();
                                msg.setName(ViewVideoActivity.userName);
                                Method.send(msg,mSc);
                            }
                            while (!ViewVideoActivity.getTaskQueue().isEmpty()) {
                                //发送报文
                                FileFragment ff = ViewVideoActivity.taskQueue.poll();
                                Message msgObj = new Message();
                                msgObj.setFragment(ff);
                                Method.send(msgObj, mSc);
                            }
                        } catch (MyException e) {
                            Log.d(TAG, "catch");
                        }
//                            Log.d(TAG, "finish write");
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
