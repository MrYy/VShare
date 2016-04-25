package com.ANT.MiddleWare.WiFi.WiFiTCP;

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
            sc.configureBlocking(false);
            Selector selector = Selector.open();
            sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            while (!Thread.interrupted()) {
                int selected = selector.select();
                if (selected == 0) continue;
                Set<SelectionKey> mKeys = selector.selectedKeys();
                Iterator ite = mKeys.iterator();
                while (ite.hasNext()) {
                    mKey = (SelectionKey) ite.next();
                    if (mKey.isWritable()) {
                        SocketChannel bSc = (SocketChannel) mKey.channel();
                        Message msg = new Message();
                        msg.setType(Message.Type.Message);
                        msg.setMessage("give me fragment");
                        Method.sendMessage(bSc,msg);
                        //下面这句话很关键，决定下一个优先的事件
                        //可以让客户端实现读写轮流
                        mKey.interestOps(SelectionKey.OP_READ);
                    }else if (mKey.isReadable()) {
                        Message msg = Method.readMessage((SocketChannel) mKey.channel());
                        System.out.println(msg.getType().getDescribe()+":"+msg.getMessage());
                        mKey.interestOps(SelectionKey.OP_WRITE);
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

