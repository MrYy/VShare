package com.ANT.MiddleWare.PartyPlayerActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ANT.MiddleWare.PartyPlayerActivity.bean.DashApplication;
import com.ANT.MiddleWare.PartyPlayerActivity.util.Method;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by David on 16/7/6.
 */
public class MsgAdapter extends ArrayAdapter<Msg> {
    private final Bitmap defaultBitmap;
    private int resourceId;
    private boolean rightOk = false;
    private boolean leftOK = false;
    private Bitmap download;

    public MsgAdapter(Context context, int textViewResourceId, List<Msg> objects){
        super(context,textViewResourceId,objects);
        Iterator<String> ite = ViewVideoActivity.onLineUsers.iterator();
        defaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.profile_default);


        resourceId=textViewResourceId;
    }
    @Override
    public View getView(int position,View convertView,ViewGroup parent){
        Msg msg=getItem(position);
        View view;
        ViewHolder viewHolder;
        if (convertView==null){
            view= LayoutInflater.from(getContext()).inflate(resourceId,null);
            viewHolder=new ViewHolder();
            viewHolder.leftLayout=(LinearLayout)view.findViewById(R.id.left_layout);
            viewHolder.rightLayout=(LinearLayout)view.findViewById(R.id.right_layout);
            viewHolder.leftMsg=(TextView)view.findViewById(R.id.left_msg);
            viewHolder.rightMsg=(TextView)view.findViewById(R.id.right_msg);
            viewHolder.Sender=(TextView)view.findViewById(R.id.sender);
            viewHolder.Reciever=(TextView)view.findViewById(R.id.reciever);
            viewHolder.Timesamp=(TextView)view.findViewById(R.id.timesamp);
            viewHolder.leftPhoto = (ImageView) view.findViewById(R.id.left_photo);
            viewHolder.rightPhoto = (ImageView) view.findViewById(R.id.right_photo);
            view.setTag(viewHolder);}
        else{
            view=convertView;
            viewHolder=(ViewHolder)view.getTag();
        }
        viewHolder.Timesamp.setText(getChatTime(msg.getTimesamp()));
        String name = msg.getName();
        if (msg.getType()==Msg.TYP_RECIEVED){
            viewHolder.leftLayout.setVisibility(View.VISIBLE);
            viewHolder.rightLayout.setVisibility(View.GONE);
            viewHolder.leftMsg.setText(msg.getContent());
            Method.cachePhoto(getContext(),viewHolder.leftPhoto,name);
            viewHolder.Reciever.setText(name);;}

        else if(msg.getType()==Msg.TYP_SEND){
            viewHolder.leftLayout.setVisibility(View.GONE);
            viewHolder.rightLayout.setVisibility(View.VISIBLE);
            viewHolder.rightMsg.setText(msg.getContent());
            Method.cachePhoto(getContext(),viewHolder.rightPhoto,name);
            viewHolder.Sender.setText(name);}


        return view;}
    public static String getChatTime(long timesamp) {
        String result = "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd");
        Date today = new Date(System.currentTimeMillis());
        Date otherDay = new Date(timesamp);
        int temp = Integer.parseInt(sdf.format(today))
                - Integer.parseInt(sdf.format(otherDay));
        switch (temp) {
            case 0:
                result = "今天 " + getHourAndMin(timesamp);
                break;
            case 1:
                result = "昨天 " + getHourAndMin(timesamp);
                break;
            case 2:
                result = "前天 " + getHourAndMin(timesamp);
                break;

            default:
                result = getTime(timesamp);
                break;
        }
        return result;
    }

    public static String getHourAndMin(long time) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(new Date(time));
    }

    public static String getTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm");
        return format.format(new Date(time));
    }

    class ViewHolder{
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMsg;
        TextView rightMsg;
        TextView Sender;
        TextView Reciever;
        TextView Timesamp;
        ImageView leftPhoto;
        ImageView rightPhoto;
    }

}
