package com.ANT.MiddleWare.PartyPlayerActivity.bean;

import com.ANT.MiddleWare.Entities.FileFragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by David on 16/4/18.
 */
public class Message implements Serializable {
    /**
     *
     */
    public static enum Type {
        Message("message"), Fragment("fragment");
        private String describe;

        Type(String describe) {
            this.describe = describe;
        }

        public String getDescribe() {
            return describe;
        }
    }

    private Type type;

    public void setLength(int length) {
        this.length = length;
    }

    public int getMsgLength() {
        return length;
    }
    private int length;
    private int count;
    private String message = "";
    private FileFragment fragment = null;
    private byte[] bytesObj = null;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FileFragment getFragment() {
        return fragment;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public int getLength() {
        if (bytesObj != null) return bytesObj.length;
        return -1;
    }

    public void setFragment(FileFragment fragment) {
        this.fragment = fragment;
        this.type = Type.Fragment;
    }

    public void setMessage(String message) {
        this.message = message;
        this.type = Type.Message;
    }

    public String getMessage() {
        return message;
    }

    public Type getType() {
        return type;
    }

    public byte[] getBytes() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(this);
            objectOutputStream.flush();
            bytesObj = byteArrayOutputStream.toByteArray();
            return bytesObj;
        } catch (IOException e) {
            e.printStackTrace();

        } finally {

            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}