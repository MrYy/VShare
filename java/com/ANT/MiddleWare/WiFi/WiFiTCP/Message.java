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
    private Type type;
    private FileFragment fragment = null;
    public FileFragment getFragment() {
        return fragment;
    }
    public void setFragment(FileFragment fragment) {
        this.fragment = fragment;
        this.type = Type.Fragment;
    }

    public Type getType() {
        return type;
    }

}