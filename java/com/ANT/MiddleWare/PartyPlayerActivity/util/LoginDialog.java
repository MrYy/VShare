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
        name = (EditText) findViewById(R.id.edittext_account);
        password = (EditText) findViewById(R.id.edittext_password);
        registerButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_register:
                String nameStr;
                String passwordStr;
                if((nameStr = name.getText().toString().trim()).equals("")||(passwordStr = password.getText().toString().trim()).equals("")){
                    Method.display(getContext(),"请输入账号或密码");
                    return;
                }
                Map<String, String> req = new HashMap<>();
                req.put("name", nameStr);
                req.put("password", passwordStr);
                Method.postRequest(getContext(), DashApplication.REGISTER, req, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        try {
                            JSONObject res = new JSONObject(s);
                            if (res.getString("code").equals("200")) {
                                Method.display(getContext(),"注册成功");
                                dismiss();
                            }else {
                                Method.display(getContext(),res.getString("msg"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
        }
    }
}
