package com.ANT.MiddleWare.PartyPlayerActivity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {


    private View view;
    private Activity context;
    private CheckBox checkBoxRem;
    private CheckBox checkBoxPublis;
    private EditText editText;

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_login, container, false);
        context = getActivity();
        checkBoxRem = (CheckBox)view.findViewById(R.id.checkbox_remember_account);
        checkBoxPublis = (CheckBox) view.findViewById(R.id.checkbox_publish_video);
        editText = (EditText) view.findViewById(R.id.edittext_name);
        ((Button)view.findViewById(R.id.button_login)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editText.getText().toString().trim();
                if (editText.equals("")) {
                    name = "小蚂蚁";
                }
                Intent intent = new Intent(getActivity(), ViewVideoActivity.class);
                intent.putExtra(context.getString(R.string.user_name), name);
                intent.putExtra(context.getString(R.string.publish_video), checkBoxPublis.isChecked());
                intent.putExtra("保存用户昵称", checkBoxRem.isChecked());
                startActivity(intent);
            }
        });
        return view;
    }

}
