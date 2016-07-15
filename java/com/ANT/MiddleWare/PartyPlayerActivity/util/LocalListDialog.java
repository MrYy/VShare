package com.ANT.MiddleWare.PartyPlayerActivity.util;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.ANT.MiddleWare.PartyPlayerActivity.R;

/**
 * Created by FanYifan on 2016/7/15.
 */
public class LocalListDialog extends Dialog {
    private ListView localList;
    private String[] list;
    private String path1=Environment.getExternalStorageDirectory()+"/video/4/1.mp4";
    private String path2=Environment.getExternalStorageDirectory()+"/video/4/2.mp4";
    private String path3=Environment.getExternalStorageDirectory()+"/video/4/3.mp4";
    private String path4=Environment.getExternalStorageDirectory()+"/video/4/4.mp4";
    private String lpath;
    public MyListener myListener;

    public interface MyListener{
        public void deliver(String path);
    }

    public LocalListDialog(Context context,MyListener myListener) {
        this(context,R.style.alert_dialog);
        this.myListener=myListener;
    }

    public LocalListDialog(Context context, int themeResId) {
        super(context, themeResId);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_video_list);
        localList = (ListView) findViewById(R.id.local_list);
        list=new String[]{path1,path2,path3,path4};
        localList.setAdapter(new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,list));
        localList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                lpath=list[i];
                myListener.deliver(lpath);
                dismiss();
            }
        });


    }

}
