package com.ANT.MiddleWare.PartyPlayerActivity.bean;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by zxc on 2016/7/11.
 */
public class SendTask {
    private List<InetAddress> mClients = new ArrayList<>();

    private Message msg;

    public Message getMsg() {
        return msg;
    }

    public void setMsg(Message msg) {
        this.msg = msg;
    }

    public List<InetAddress> getmClients() {
        return mClients;
    }

    public void setClients(Set<InetAddress> clients) {
        Iterator<InetAddress> iterator = clients.iterator();
        while (iterator.hasNext()) {
            try {
                mClients.add(InetAddress.getByName(iterator.next().getHostName()));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

}
