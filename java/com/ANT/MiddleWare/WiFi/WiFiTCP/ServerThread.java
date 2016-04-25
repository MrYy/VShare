package com.ANT.MiddleWare.WiFi.WiFiTCP;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by ge on 2016/4/25.
 */
public class ServerThread extends Thread {
    private String ip;
    private WiFiTCP wiFiTCP;
    public ServerThread(WiFiTCP wiFiTCP, String ip) {
        this.ip = ip;
        this.wiFiTCP = wiFiTCP;
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
            while (true) {
                int readyChannel = selector.select();
                if (readyChannel == 0) continue;
                Set<SelectionKey> selectedChannel = selector.selectedKeys();
                Iterator ite = selectedChannel.iterator();
                while (ite.hasNext()) {
                    SelectionKey mKey = (SelectionKey) ite.next();
                    if (mKey.isAcceptable()) {
                        System.out.println("accept new socket");
                        SocketChannel ss = ((ServerSocketChannel) mKey.channel()).accept();
                        ss.configureBlocking(false);
                        ss.register(mKey.selector(), SelectionKey.OP_WRITE);
                    } else if (mKey.isWritable()) {
                        //can write ,send fragment
                        Message msgObj = new Message();
                        msgObj.setFragment(wiFiTCP.getTaskList().pop());
                        Method.sendMessage((SocketChannel) mKey.channel(), msgObj);
                    }
                    ite.remove();
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
