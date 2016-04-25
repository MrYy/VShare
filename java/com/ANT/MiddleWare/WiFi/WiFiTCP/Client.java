package com.ANT.MiddleWare.WiFi.WiFiTCP;

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
import java.util.Stack;

/**
 * Created by David on 16/4/21.
 */
public class Client implements Runnable {
    private InetAddress remoteAddress;
    private int remotePort;
    private Stack<Message> msgStack = new Stack<>();
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
                        if (!msgStack.empty()) {
                            //ready to send
                            //send the message in the stack
                            SocketChannel bSc = (SocketChannel) mKey.channel();
                            Method.sendMessage(bSc, msgStack.pop());
                        }
                        //this makes the channel to ready to read
                        mKey.interestOps(SelectionKey.OP_READ);
                    }else if (mKey.isReadable()) {
                        Message msg = Method.readMessage((SocketChannel) mKey.channel());
                        if(msg==null) return;
                        Message.Type type = msg.getType();
                        if (type == Message.Type.Message) {
                            System.out.println(msg.getType().getDescribe()+":"+msg.getMessage());
                            Message.MessageType msgType = msg.getMsgType();
                            if (msgType == Message.MessageType.GIVE) {
                                //the server has some message to give
                                int seg = msg.getSegIndex();
                                int frag = msg.getStartFragmentIndex();
                                Message reply = new Message();
                                if (checkHas(seg, frag)) {
                                    reply.setMsgType(Message.MessageType.WANT , seg ,frag);
                                }else {
                                    reply.setMsgType(Message.MessageType.NOT_WANT , seg ,frag);
                                }
                                msgStack.push(reply);
                            }
                        }else if(type== Message.Type.Fragment) {
                            //receive fragment and insert into the segment
                            FileFragment ff = msg.getFragment();
                            IntegrityCheck.getInstance().insert(ff.getSegmentID(),ff);

                            return;
                        }

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

    private boolean checkHas(int seg, int fragStart) {
        //check whether the client want the message.
        //should has a list kind of already downloaded fragments.
        return false;
    }
}

