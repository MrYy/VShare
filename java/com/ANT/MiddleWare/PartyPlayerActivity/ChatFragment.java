package com.ANT.MiddleWare.PartyPlayerActivity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {


    private View view;
    private ListView listView;
    private EditText editText;
    private Button chat_btn;
    private SlideSwitch slideSwitch;
    private List<Msg> msgList=new ArrayList<Msg>();
    private List<String> usersList=new ArrayList<String>();
    private MsgAdapter msgAdapter;
    private UsersAdapter usersAdapter;
    private String hoster;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chat, container, false);
        listView=(ListView)view.findViewById(R.id.list_main);
        editText=(EditText)view.findViewById(R.id.edit_main);
        chat_btn=(Button)view.findViewById(R.id.button_main);
        slideSwitch=(SlideSwitch)view.findViewById(R.id.sl_switch);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        slideSwitch.setOnStateChangedListener(new SlideSwitch.OnStateChangedListener() {

            @Override
            public void onStateChanged(boolean state) {
                // TODO Auto-generated method stub
                if (true == state)
                    onTalkState();
                else onUsersState();
            }

        });
    }
    public void onTalkState(){
        msgAdapter = new MsgAdapter(getActivity(),R.layout.chat_item_message,msgList);
        chat_btn.setText("send");
        editText.setHint("聊天列表");
        chat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = editText.getText().toString();
                if (!"".equals(content)) {
                    Msg msg = new Msg(content, Msg.TYP_SEND, hoster, System.currentTimeMillis());
                    msgList.add(msg);
                    msgAdapter.notifyDataSetChanged();
                    listView.setSelection(msgList.size());
                    editText.setText("");
                    //开启发送线程
                    startSendThread(msg);

                }
            }
        });
        startReceiveThread();
        listView.setAdapter(msgAdapter);
    }
    public void onUsersState(){
        UsersInit();
        chat_btn.setText("refresh");
        editText.setHint("附近的人");
        usersAdapter = new UsersAdapter(getActivity(),R.layout.chat_item_users,usersList);
        listView.setAdapter(usersAdapter);
    }
    public void startReceiveThread(){
        new Thread(){

        }.start();
    }
    public void startSendThread(Msg msg){
        new Thread(){
         }.start();
    }
    private void UsersInit(){
        usersList.add("fanfan");
        usersList.add("yangyang");

    }
}
