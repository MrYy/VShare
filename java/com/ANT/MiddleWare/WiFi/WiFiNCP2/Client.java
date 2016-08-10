package com.ANT.MiddleWare.WiFi.WiFiNCP2;

import android.util.Log;

import com.ANT.MiddleWare.Entities.FileFragment;
import com.ANT.MiddleWare.PartyPlayerActivity.ViewVideoActivity;
import com.ANT.MiddleWare.PartyPlayerActivity.bean.Message;
import com.ANT.MiddleWare.PartyPlayerActivity.bean.SendTask;
import com.ANT.MiddleWare.PartyPlayerActivity.policy.directStatus.IsOwner;
import com.ANT.MiddleWare.PartyPlayerActivity.util.Method;
import com.ANT.MiddleWare.WiFi.WiFiTCP.MyException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SocketChannel sc = null;
        SelectionKey mKey = null;
        Log.d(TAG, "try to connect");
        Message nameMsg = new Message();
        nameMsg.setName(ViewVideoActivity.userName);
        nameMsg.setMessage(ViewVideoActivity.userName);
        ViewVideoActivity.sendMsg(nameMsg);
        try {
            sc = SocketChannel.open();
            sc.connect(new InetSocketAddress(remoteAddress.getHostAddress(), remotePort));
            sc.configureBlocking(false);
            sc.socket().getTcpNoDelay();
            Selector selector = Selector.open();
            sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            while (true) {
                int selected = selector.select();
                if (selected == 0) continue;
                Set<SelectionKey> mKeys = selector.selectedKeys();
                Iterator ite = mKeys.iterator();
                while (ite.hasNext()) {
                    mKey = (SelectionKey) ite.next();
                    if (mKey.isReadable()) {
                        //在这里准备添加报头
//                            Log.d(TAG, "client is reading");
                        SocketChannel mSc = (SocketChannel) mKey.channel();
                        Method.read(mSc);
//                            Log.d(TAG, "finish reading");
                    } else if (mKey.isWritable()) {
//                            Log.d(TAG, "client is writing");
                        SocketChannel mSc = (SocketChannel) mKey.channel();
//                            TimeUnit.SECONDS.sleep(3);
//                        Message testMsg = new Message();
//                            testMsg.setMessage(Method.getRandomString(300));
//                            ViewVideoActivity.sendMsg(testMsg);
                        try {
                            while (!ViewVideoActivity.sendMessageQueue.isEmpty()) {
                                SendTask sendTask = ViewVideoActivity.sendMessageQueue.poll();
                                Message msg = sendTask.getMsg();
                                msg.setName(ViewVideoActivity.userName);
                                Method.send(msg, mSc);
                            }
                            while (!ViewVideoActivity.taskMessageQueue.isEmpty()) {
                                //发送报文
                                FileFragment ff = ViewVideoActivity.taskMessageQueue.poll().getMsg().getFragment();
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
        } catch (ConnectException ce) {
            ExecutorService es = Executors.newFixedThreadPool(1);
            try {
                es.execute(new Client(InetAddress.getByName(IsOwner.ip), 12345));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
    }
}
