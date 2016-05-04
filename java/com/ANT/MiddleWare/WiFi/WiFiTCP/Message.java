package com.ANT.MiddleWare.WiFi.WiFiTCP;

import com.ANT.MiddleWare.Entities.FileFragment;

import java.io.ByteArrayOutputStream;
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
    private Type type;
    private String message = "";
    private FileFragment fragment = null;
    private byte[] bytesObj = null;
    public FileFragment getFragment() {
        return fragment;
    }

    public int getLength() {
        if(bytesObj!=null) return bytesObj.length;
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