package com.ANT.MiddleWare.WiFi.WiFiTCP;

import com.ANT.MiddleWare.Entities.FileFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by David on 16/4/18.
 */
public  class Message implements Serializable {
    /**
     *
     */
    public static enum  Type{
        Message("message"),Fragment("fragment");
        private String describe;
        Type(String describe){
            this.describe = describe;
        }

        public String getDescribe() {
            return describe;
        }
    }
    public static enum MessageType{
        WANT,NOT_WANT,GIVE
    }
    private int segIndex;
    private int startFragmentIndex;
    private Type type;
    private MessageType msgType;
    private FileFragment fragment = null;
    private String message = "";
    public FileFragment getFragment() {
        return fragment;
    }

    public void setFragment(FileFragment fragment) {
        this.type = Type.Fragment;
        this.fragment = fragment;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getSegIndex() {
        return segIndex;
    }

    public void setSegIndex(int segIndex) {
        this.segIndex = segIndex;
    }

    public int getStartFragmentIndex() {
        return startFragmentIndex;
    }

    public void setStartFragmentIndex(int startFragmentIndex) {
        this.startFragmentIndex = startFragmentIndex;
    }

    public MessageType getMsgType() {
        return msgType;
    }

    public void setMsgType(MessageType msgType,int segIndex,int startFragmentIndex) {
        this.type = Type.Message;
        this.msgType = msgType;
        this.startFragmentIndex = startFragmentIndex;
        this.segIndex = segIndex;
    }
}