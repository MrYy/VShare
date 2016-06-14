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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

/**
 * Created by ge on 2016/4/25.
 */
public class ServerThread extends Thread {
    protected final static Queue<FileFragment> taskQueue = new ConcurrentLinkedQueue<FileFragment>();
    private static final String TAG = ServerThread.class.getSimpleName();
    private String ip;
    private WiFiTCP wiFiTCP;
    private Context context;
    private Stack<FileFragment> convertStack = new Stack<FileFragment>();
    private Queue<LocalTask> localTask = new ConcurrentLinkedQueue<LocalTask>();
    private int oldStart = 0;
    private int fragSize;
    private int index = 0;

    public ServerThread(WiFiTCP wiFiTCP, String ip, Context context) {
        this.ip = ip;
        this.wiFiTCP = wiFiTCP;
        this.context = context;
    }
    private  byte[] byteMerger(byte[] byte_1, byte[] byte_2){
        byte[] byte_3 = new byte[byte_1.length+byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
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

                        //deal with threads
                        Set<String> links = WiFiTCP.links;
                        if (links!=null) {
                            Queue<String> toLink = new LinkedList<String>();
                            for (String link : links) {
                                toLink.add(link);
                            }
                            while (!toLink.isEmpty()) {
                                String ip = toLink.poll();
                                Log.d(TAG, "即将连接ip：" + ip);
                                new Thread(new Client(InetAddress.getByName(ip), 12345, context)).start();
                            }
                            WiFiTCP.links = null ;
                        }
                    } else if (mKey.isWritable()) {
                        //can write ,send fragment
                        SocketChannel sc = (SocketChannel) mKey.channel();
                        sc.socket().setTcpNoDelay(true);
                        InetAddress mAddr = sc.socket().getInetAddress();
                        Message msgObj = new Message();

                        Stack<FileFragment> taskList = wiFiTCP.getTaskList();
                        while (!taskList.empty()) {
                            FileFragment ff = taskList.pop();
                            convertStack.add(ff);
                        }
                        while (!convertStack.empty()) {
                            //LocalTask is a wrapper,
                            //wrap a address set which records the address that already has been sent to.
                            FileFragment ff = convertStack.pop();
                            LocalTask lt = new LocalTask(ff);
                            localTask.add(lt);
                            Log.d(TAG,"size of localTask:" + String.valueOf(localTask.size()));
                        }
                        if (!localTask.isEmpty()) {
                            //taskQueue has value.
                            //send fragment in taskList to any one of the clients
                            LocalTask taskToSend= localTask.peek();
                            if (taskToSend.getAddrs().contains(mAddr)){
                                Log.d(TAG, "the client has already been sent");
                                ite.remove();
                                return;
                            }
                            FileFragment ff = taskToSend.getFf();
                            Method.record(ff,"send",String.valueOf(++index));
                            msgObj.setFragment(ff);
                            byte[] msgByte = msgObj.getBytes();
                            if (count == 0) {
                                fragSize = msgByte.length;
                                count++;
                            }
                            try {
                                if (ff.getStartIndex() < oldStart) {
                                    Log.d(TAG, "开始发送下一段");
                                }
                                oldStart = ff.getStartIndex();
                                int length = msgByte.length;
                                Log.d(TAG, "fragSize:" + fragSize+" length:"+length);
                                if (length < fragSize) {
                                    Log.d(TAG, "最后一片补偿");
                                    byte[] addMsg = new byte[fragSize - length];
                                    Method.sendMessage(sc, byteMerger(msgByte, addMsg));
                                } else {
                                    Log.d(TAG, "send fragment to:"+mAddr.toString()+" ,start: " + String.valueOf(ff.getStartIndex()) + "message size:" + msgByte.length + " after send ,queue size:" +
                                            String.valueOf(localTask.size()));
                                    Method.sendMessage(sc, msgByte);
                                }
                                if (taskToSend.getAddrs().size() == WiFiTCP.linkSize) {
                                    //the single piece has sent to all of the clients
                                    localTask.poll();
                                }else {
                                    taskToSend.getAddrs().add(mAddr);
                                }

                            } catch (MyException e) {
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
        private Set<InetAddress> addrs;
        public LocalTask(FileFragment ff) {
            addrs = new HashSet<InetAddress>();
            this.ff = ff;
        }
        public FileFragment getFf() {
            return ff;
        }
        public Set<InetAddress> getAddrs() {
            return addrs;
        }
    }
}
