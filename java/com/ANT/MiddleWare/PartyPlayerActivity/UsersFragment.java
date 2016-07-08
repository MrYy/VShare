package com.ANT.MiddleWare.PartyPlayerActivity;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.ANT.MiddleWare.PartyPlayerActivity.Msg;
import com.ANT.MiddleWare.PartyPlayerActivity.MsgAdapter;
import com.ANT.MiddleWare.PartyPlayerActivity.R;
import com.ANT.MiddleWare.PartyPlayerActivity.UsersAdapter;
import com.ANT.MiddleWare.PartyPlayerActivity.ViewVideoActivity;
import com.ANT.MiddleWare.WiFi.WiFiTCP.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.ANT.MiddleWare.PartyPlayerActivity.ViewVideoActivity.sendMsg;


/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {

    final int RIGHT = 0;
    final int LEFT = 1;
    private View view;
    private ListView listView;
    private EditText editText;
    private ImageButton chat_btn;
    private List<Msg> msgList=new ArrayList<Msg>();
    private List<String> usersList=new ArrayList<String>();
    private MsgAdapter msgAdapter;
    private UsersAdapter usersAdapter;
    private String hoster = "aaa";


    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chat, container, false);
        listView=(ListView)view.findViewById(R.id.list_main);
        editText=(EditText)view.findViewById(R.id.edit_main);
        chat_btn=(ImageButton)view.findViewById(R.id.button_main);
        onUsersState();
        return view;
    }

    public void onTalkState(){
        chat_btn.setBackgroundResource(R.drawable.send);
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
                    Message message= new Message();
                    message.setMessage(content);
                    sendMsg(message);
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
        chat_btn.setBackgroundResource(R.drawable.refresh);
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
