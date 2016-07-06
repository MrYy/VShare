package com.ANT.MiddleWare.PartyPlayerActivity;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by David on 16/7/6.
 */
public class UsersAdapter extends ArrayAdapter<String> {
    private int resourceId;
    public UsersAdapter(Context context,int textViewResourceId,List<String> objects){
        super(context,textViewResourceId,objects);
        resourceId=textViewResourceId;
    }
    @Override
    public View getView(final int position,View convertView,ViewGroup parent){
        View view;
        ViewHolder viewHolder;
        if (convertView==null){
            view= LayoutInflater.from(getContext()).inflate(resourceId,null);
            viewHolder=new ViewHolder();
            viewHolder.namelayout=(LinearLayout)view.findViewById(R.id.nameLayout);
            viewHolder.names=(TextView)view.findViewById(R.id.names);
            view.setTag(viewHolder);}
        else{
            view=convertView;
            viewHolder=(ViewHolder)view.getTag();
        }

        viewHolder.namelayout.setVisibility(View.VISIBLE);
        viewHolder.names.setText(getItem(position));
        viewHolder.names.setTextColor(Color.BLACK);


        return view;}
    class ViewHolder{
        LinearLayout namelayout;
        TextView names;

    }


}