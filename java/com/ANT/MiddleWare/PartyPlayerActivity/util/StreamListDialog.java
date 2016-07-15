package com.ANT.MiddleWare.PartyPlayerActivity.util;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ANT.MiddleWare.PartyPlayerActivity.R;

/**
 * Created by FanYifan on 2016/7/15.
 */
public class StreamListDialog extends Dialog {
    private ListView streamList;
    private String[] list;
    private String path="http://127.0.0.1:9999/4/index.m3u8";
    private String spath;
    private MyListener myListener;

    public interface MyListener{
        public void deliver(String path);
    }

    public StreamListDialog(Context context, MyListener myListener) {
        this(context,R.style.alert_dialog);
        this.myListener=myListener;
    }

    public StreamListDialog(Context context, int themeResId) {
        super(context, themeResId);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stream_video_list);
        streamList= (ListView) findViewById(R.id.stream_list);
        list=new String[]{path};
        streamList.setAdapter(new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,list));
        streamList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                spath=list[i];
                myListener.deliver(spath);
                dismiss();
            }
        });
    }
}
