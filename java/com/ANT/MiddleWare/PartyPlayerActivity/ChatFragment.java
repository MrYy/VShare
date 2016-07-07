package com.ANT.MiddleWare.PartyPlayerActivity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {


    private View view;
    private ListView listView;
    private EditText editText;
    private Button chat_btn;
    private SlideSwitch slideSwitch;
    private Switch aSwitch;
    private List<Msg> msgList=new ArrayList<Msg>();
    private List<String> usersList=new ArrayList<String>();
    private MsgAdapter msgAdapter;
    private UsersAdapter usersAdapter;
    private String hoster = "aaa";

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
        //aSwitch=(Switch)view.findViewById(R.id.switch_chat);
//        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    Log.d("talk","talk");
//                    onTalkState();
//                } else {
//                    Log.d("user","user");
//                    onUsersState();
//                }
//            }
//
//        });
        onUsersState();
        slideSwitch.setOnStateChangedListener(new SlideSwitch.OnStateChangedListener() {
            @Override
            public void onStateChanged(boolean state) {
                if (true == state) {
                    Log.d("talk","talk");
                    onTalkState();
                } else {
                    Log.d("user","user");
                    onUsersState();
                }
            }
        });

        return view;
    }


    public void onTalkState(){
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
                    //startSendThread(msg);

                }
            }
        });
        //MsgInit();
        startReceiveThread();
    }
    public void onUsersState(){
        UsersInit();
        chat_btn.setText("refresh");
        editText.setHint("附近的人");
        usersAdapter = new UsersAdapter(getActivity(),R.layout.chat_item_users,usersList);
        listView.setAdapter(usersAdapter);
        listView.setSelection(usersList.size());
    }
    public void startReceiveThread(){

        Msg receivedmsg = new Msg("hello",Msg.TYP_RECIEVED,"bbb",System.currentTimeMillis());
        msgList.add(receivedmsg);
        msgAdapter = new MsgAdapter(getActivity(),R.layout.chat_item_message,msgList);
        listView.setAdapter(msgAdapter);
        listView.setSelection(msgList.size());
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    public void startSendThread(Msg msg){
    }
    public void MsgInit(){
     msgList = null;
    }
    private void UsersInit(){
        usersList.add("fanfan");
        usersList.add("yangyang");

    }
}
