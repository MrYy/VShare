package com.ANT.MiddleWare.PartyPlayerActivity.util;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

import com.ANT.MiddleWare.PartyPlayerActivity.R;

/**
 * Created by zxc on 2016/7/11.
 */
public class LoginDialog extends Dialog implements View.OnClickListener{
    private View mDialogView;
    private Button registerButton;
    private EditText name;
    private EditText password;
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
        setContentView(R.layout.register);
        mDialogView = getWindow().getDecorView().findViewById(android.R.id.content);
        mDialogView.startAnimation(AnimationUtils.loadAnimation(getContext(),R.anim.modal_in));
        registerButton = (Button) findViewById(R.id.button_register);
        name = (EditText) findViewById(R.id.edittext_name);
        password = (EditText) findViewById(R.id.edittext_password);
        registerButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_register:

                break;
        }
    }
}
