package com.ANT.MiddleWare.WiFi.WiFiNCP2;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.ANT.MiddleWare.Entities.FileFragment;
import com.ANT.MiddleWare.PartyPlayerActivity.ViewVideoActivity;
import com.ANT.MiddleWare.PartyPlayerActivity.bean.Message;
import com.ANT.MiddleWare.PartyPlayerActivity.bean.SendTask;
import com.ANT.MiddleWare.PartyPlayerActivity.util.Method;
import com.ANT.MiddleWare.WiFi.WiFiTCP.MyException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
                Log.d("TAG","ready chanel num:" + String.valueOf(readChannel));
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
                        ViewVideoActivity.setClients(clients);
                        Log.d(TAG, "server thread set:" + String.valueOf(clients.size()) + ":" + clients);
                        Log.d(TAG, "view video set :" + String.valueOf(ViewVideoActivity.getClients().size()) + ViewVideoActivity.getClients());
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "has a client in", Toast.LENGTH_SHORT).show();
                            }
                        });
                        sc.configureBlocking(false);
                        sc.register(mKey.selector(), SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                        Iterator<String> iterator = ViewVideoActivity.onLineUsers.iterator();
                        while (iterator.hasNext()) {
                            Message nameMsg = new Message();
                            String mName = iterator.next();
                            nameMsg.setName(mName);
                            nameMsg.setMessage(mName);
                            ViewVideoActivity.sendMsg(nameMsg);
                        }
                    } else if (mKey.isReadable()) {
//                        Log.d(TAG, "server thread is readable");
                        SocketChannel mSc = (SocketChannel) mKey.channel();
                        Method.read(mSc);
                    }
                    else if (mKey.isWritable()) {
                        SocketChannel sc = (SocketChannel) mKey.channel();
                        InetAddress mRemoteAddr = sc.socket().getInetAddress();
                        sc.socket().setTcpNoDelay(true);
//                        Log.d(TAG, "server is writing");
//                        TimeUnit.SECONDS.sleep(2);
//                        Message testMsg = new Message();
//                        testMsg.setMessage(Method.getRandomString(300));
//                        ViewVideoActivity.sendMsg(testMsg);
                        try {
                            while (!ViewVideoActivity.sendMessageQueue.isEmpty()) {
                                SendTask sendTask = ViewVideoActivity.sendMessageQueue.peek();
                                Message msg =sendTask .getMsg();

                                count++;
                                if (count < 15) {
                                                                    Log.d(TAG, "msg type:"+String.valueOf(msg.getType())+"remote addr:" + String.valueOf(mRemoteAddr) + " setAddr:" + sendTask.getmClients()+
                                " set size:"+String.valueOf(sendTask.getmClients().size())
                                +" msg:"+msg.getMessage()+" msg queue:"+String.valueOf(ViewVideoActivity.sendMessageQueue.size()));
                                }

                                if (sendTask.getmClients().contains(mRemoteAddr)) {
                                    msg.setName(ViewVideoActivity.userName);
                                    Method.send(msg, sc);
                                    sendTask.getmClients().remove(mRemoteAddr);
                                }
                                if (sendTask.getmClients().size() == 0) {
                                    ViewVideoActivity.sendMessageQueue.poll();
                                }
                            }
//                            Log.d(TAG, "after message"+" queue size "+String.valueOf(ViewVideoActivity.sendMessageQueue.size()));

                            while (!ViewVideoActivity.taskMessageQueue.isEmpty()) {
                                //发送报文
                                SendTask sendTask = ViewVideoActivity.taskMessageQueue.peek();
                                Message msg = sendTask.getMsg();
                                if (sendTask.getmClients().contains(mRemoteAddr)) {
                                    FileFragment ff = msg.getFragment();
                                    Message msgObj = new Message();
                                    msgObj.setFragment(ff);
                                    Method.send(msgObj, sc);
                                    sendTask.getmClients().remove(mRemoteAddr);
                                }
                                if (sendTask.getmClients().size() == 0) {
                                    ViewVideoActivity.taskMessageQueue.poll();
                                }
                            }
//                            Log.d(TAG, "after task");

                        } catch (MyException e) {
                            Log.d(TAG, "catch");
                        }
//                        Log.d(TAG, "finish writing");
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
