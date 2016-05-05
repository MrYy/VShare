package com.ANT.MiddleWare.WiFi.WiFiTCP;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.ANT.MiddleWare.Entities.FileFragment;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by ge on 2016/4/25.
 */
public class ServerThread extends Thread {
    private static final String TAG = ServerThread.class.getSimpleName();
    private String ip;
    private WiFiTCP wiFiTCP;
    private Context context;
    private Queue<LocalTask> localTask = new ConcurrentLinkedQueue<LocalTask>();
    protected final static Queue<FileFragment> taskQueue = new ConcurrentLinkedQueue<FileFragment>();

    public ServerThread(WiFiTCP wiFiTCP, String ip,Context context) {
        this.ip = ip;
        this.wiFiTCP = wiFiTCP;
        this.context = context;
    }

    @Override
    public void run() {
        super.run();
        try {
            System.out.println("start listen");
            System.out.println(ip);
            InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName(ip), 12345);
            Selector selector = Selector.open();
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.socket().bind(addr);
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            //client
            int count = 0;
            while (true) {
                int readyChannel = selector.select();
                if (readyChannel == 0) continue;
                Set<SelectionKey> selectedChannel = selector.selectedKeys();
                Iterator ite = selectedChannel.iterator();
                while (ite.hasNext()) {
                    SelectionKey mKey = (SelectionKey) ite.next();
                    if (mKey.isAcceptable()) {
                        System.out.println("accept new socket");
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "accept new socket",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                        SocketChannel ss = ((ServerSocketChannel) mKey.channel()).accept();
                        ss.configureBlocking(false);
                        ss.register(mKey.selector(), SelectionKey.OP_WRITE);
                    } else if (mKey.isWritable()) {
                        //can write ,send fragment
                        SocketChannel sc = (SocketChannel) mKey.channel();
                        boolean isConn = sc.isConnected();
                        InetAddress mAddr = sc.socket().getInetAddress();
                        Message msgObj = new Message();
                        //test code
                        msgObj.setMessage("hi");
                        msgObj.setCount(count++);
                        if (isConn){
                            try {
                                Method.sendMessage(sc, msgObj.getBytes());
                            } catch (MyException e) {
                                sc.socket().close();
                                Log.d(TAG, "catch");
                            }
                        }
                        Stack<FileFragment> taskList = wiFiTCP.getTaskList();
                        if (!taskQueue.isEmpty()) {
                            try {
                                TimeUnit.MILLISECONDS.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //taskQueue has value.
                            //send fragment in taskList to any one of the clients
                            FileFragment ff = taskQueue.poll();
                            Log.d(TAG, "send fragment" + String.valueOf(ff.getFragLength()));
                            msgObj.setFragment(ff);
                            try {
                                Method.sendMessage(sc, msgObj.getBytes());
                            } catch (MyException e) {
                                e.printStackTrace();
                            }
                            LocalTask mTask = new LocalTask(ff, mAddr);
                            localTask.add(mTask);
                        } else {
                            // no fragments to send
                            // handle the big fragment
                            if (!taskList.empty()) {
                                FileFragment ff = taskList.pop();
                                if (ff.isTooBig()) {
                                    //split big fragment
                                    FileFragment[] fragArray = null;
                                    try {
                                        fragArray = ff.split();
                                        for (FileFragment f : fragArray) {
                                            taskQueue.add(f);
                                            Log.d(TAG, "split fragment");
                                        }
                                    } catch (FileFragment.FileFragmentException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            //taskQueue is empty.
                            //some fragments may only be sent to one client,
                            // then pick them and send to another client
                            if (!localTask.isEmpty()) {
                                LocalTask lt = localTask.peek();
                                if (mAddr != lt.getIa()) {
                                    //send the frament to another client who does not have the fragment
                                    Message msg = new Message();
                                    msg.setFragment(lt.getFf());
                                    try {
                                        Method.sendMessage(sc, msg.getBytes());
                                    } catch (MyException e) {
                                        e.printStackTrace();
                                    }
                                    localTask.poll();
                                }
                            }
                        }
                    }
                    ite.remove();
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class LocalTask {
        private FileFragment ff;
        private InetAddress ia;

        public LocalTask(FileFragment ff, InetAddress ia) {
            this.ff = ff;
            this.ia = ia;
        }

        public FileFragment getFf() {
            return ff;
        }

        public InetAddress getIa() {
            return ia;
        }
    }
}
