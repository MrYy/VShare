package com.ANT.MiddleWare.WiFi.WiFiNCP2;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ANT.MiddleWare.Entities.FileFragment;
import com.ANT.MiddleWare.PartyPlayerActivity.ViewVideoActivity;
import com.ANT.MiddleWare.WiFi.WiFiTCP.Message;
import com.ANT.MiddleWare.WiFi.WiFiTCP.Method;
import com.ANT.MiddleWare.WiFi.WiFiTCP.MyException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by zxc on 2016/6/15.
 */
public class ServerThread extends Thread {
    public static final String TAG = ServerThread.class.getSimpleName();
    private InetAddress ip;
    private Context context;
    private Set<InetAddress> clients;
    private int count = 0;

    public ServerThread(InetAddress ip, Context context) {
        clients = new HashSet<InetAddress>();
        this.ip = ip;
        this.context = context;
    }

    @Override
    public void run() {
        super.run();
        Log.d(TAG, "server is running");
        InetSocketAddress addr = new InetSocketAddress(ip.getHostAddress(), 12345);
        try {
            Selector selector = Selector.open();
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.socket().bind(addr);
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                int readChannel = selector.select();
                if (readChannel == 0) continue;
                ;
                Set<SelectionKey> selectedChannel = selector.selectedKeys();
                Iterator ite = selectedChannel.iterator();
                while (ite.hasNext()) {
                    SelectionKey mKey = (SelectionKey) ite.next();
                    if (mKey.isAcceptable()) {
                        Log.d(TAG, "ap accept new socket");
                        SocketChannel sc = ((ServerSocketChannel) mKey.channel()).accept();
                        InetAddress clientAddr = sc.socket().getInetAddress();
                        Log.d(TAG, "client address:" + clientAddr.toString());
                        clients.add(clientAddr);
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "has a client in", Toast.LENGTH_SHORT).show();
                            }
                        });
//                        if (clients.size() < 2) {
                        sc.configureBlocking(false);
                        sc.register(mKey.selector(), SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                        Message name = new Message();
                        name.setName(ViewVideoActivity.userName);
                        ViewVideoActivity.sendMsg(name);
//                        }
                    } else if (mKey.isWritable()) {
                        SocketChannel sc = (SocketChannel) mKey.channel();
                        sc.socket().setTcpNoDelay(true);
                        Log.d(TAG, "server is writing");
                        Message testMsg = new Message();
                        testMsg.setMessage(Method.getRandomString(300));
                        ViewVideoActivity.sendMsg(testMsg);
                        try {
                            while (!ViewVideoActivity.sendMessageQueue.isEmpty()) {
                                Message msg = ViewVideoActivity.sendMessageQueue.poll();
                                msg.setName(ViewVideoActivity.userName);
                                Method.send(msg, sc);
                            }
                            while (!ViewVideoActivity.getTaskQueue().isEmpty()) {
                                //发送报文
                                FileFragment ff = ViewVideoActivity.taskQueue.poll();
                                Message msgObj = new Message();
                                msgObj.setFragment(ff);
                                Method.send(msgObj, sc);
                            }
                        } catch (MyException e) {
                            Log.d(TAG, "catch");
                        }
                        mKey.interestOps(SelectionKey.OP_READ);
                    } else if (mKey.isReadable()) {
                        Log.d(TAG, "server thread is readable");
                        SocketChannel mSc = (SocketChannel) mKey.channel();
                        Method.read(mSc);
                        mKey.interestOps(SelectionKey.OP_WRITE);
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
