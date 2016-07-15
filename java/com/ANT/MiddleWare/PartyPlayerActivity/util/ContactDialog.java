package com.ANT.MiddleWare.PartyPlayerActivity.util;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

import com.ANT.MiddleWare.PartyPlayerActivity.R;
import com.ANT.MiddleWare.PartyPlayerActivity.bean.DashApplication;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zxc on 2016/7/11.
 */
public class ContactDialog extends Dialog  {
    private View mContactView;

    public ContactDialog(Context context) {
        this(context, R.style.alert_dialog);
    }

    public ContactDialog(Context context, int themeResId) {
        super(context, themeResId);
        setCancelable(true);
        setCanceledOnTouchOutside(true);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect_layout);
        mContactView = getWindow().getDecorView().findViewById(android.R.id.content);
        mContactView.startAnimation(AnimationUtils.loadAnimation(getContext(),R.anim.modal_in));
        mContactView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContactView.destroyDrawingCache();
            }
        });
    }


}

