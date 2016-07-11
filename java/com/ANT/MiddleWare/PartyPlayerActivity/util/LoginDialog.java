package com.ANT.MiddleWare.PartyPlayerActivity.util;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.ANT.MiddleWare.PartyPlayerActivity.R;

/**
 * Created by zxc on 2016/7/11.
 */
public class LoginDialog extends Dialog implements View.OnClickListener{
    public LoginDialog(Context context) {
        this(context,R.style.alert_dialog);
    }

    public LoginDialog(Context context, int themeResId) {
        super(context, themeResId);
        setCancelable(true);
        setCanceledOnTouchOutside(true);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_dialog);
    }

    @Override
    public void onClick(View v) {

    }
}
