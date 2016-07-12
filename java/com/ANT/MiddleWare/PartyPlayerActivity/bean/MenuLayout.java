package com.ANT.MiddleWare.PartyPlayerActivity.bean;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.Image;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.ANT.MiddleWare.PartyPlayerActivity.R;

/**
 * Created by zxc on 2016/7/12.
 */
public class MenuLayout extends LinearLayout {
    private Resources resouces;
    private ImageButton buttonMy;
    private ImageButton buttonView;
    private ImageButton buttonData;

    public static enum BUTTON {
        LEFT, CENTER, RIGHT
    }

    public MenuLayout(Context context) {
        this(context, null);
    }

    public MenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.bottom_menu_layout, this);
        buttonMy = (ImageButton) findViewById(R.id.menu_button_right);
        buttonView = (ImageButton) findViewById(R.id.menu_button_left);
        buttonData = (ImageButton) findViewById(R.id.menu_button_center);
        resouces = context.getResources();
    }

    public void setFocuse(BUTTON location) {
        switch (location) {
            case RIGHT:
                buttonView.setImageBitmap(BitmapFactory.decodeResource(resouces, R.drawable.button_view_normal));
                buttonData.setImageBitmap(BitmapFactory.decodeResource(resouces, R.drawable.button_data_normal));
                buttonMy.setImageBitmap(BitmapFactory.decodeResource(resouces, R.drawable.button_my_focus));
                break;
            case CENTER:
                buttonView.setImageBitmap(BitmapFactory.decodeResource(resouces, R.drawable.button_view_normal));
                buttonData.setImageBitmap(BitmapFactory.decodeResource(resouces, R.drawable.button_data_focus));
                buttonMy.setImageBitmap(BitmapFactory.decodeResource(resouces, R.drawable.button_my_normal));
                break;
            case LEFT:
                buttonView.setImageBitmap(BitmapFactory.decodeResource(resouces, R.drawable.button_view_focus));
                buttonData.setImageBitmap(BitmapFactory.decodeResource(resouces, R.drawable.button_data_normal));
                buttonMy.setImageBitmap(BitmapFactory.decodeResource(resouces, R.drawable.button_my_normal));
                break;
        }
    }


}
