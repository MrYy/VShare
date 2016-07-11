package com.ANT.MiddleWare.PartyPlayerActivity;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;


import com.ANT.MiddleWare.PartyPlayerActivity.bean.Message;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static com.ANT.MiddleWare.PartyPlayerActivity.ViewVideoActivity.getMsg;
import static com.ANT.MiddleWare.PartyPlayerActivity.ViewVideoActivity.onLineUsers;
import static com.ANT.MiddleWare.PartyPlayerActivity.ViewVideoActivity.sendMsg;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private View view;
    private ListView listView;
    private EditText editText;
    private ImageButton chat_btn;
    private List<Msg> msgList=new ArrayList<Msg>();
    private List<String> usersList=new ArrayList<String>();
    private MsgAdapter msgAdapter;
    private UsersAdapter usersAdapter;
    private String hoster = "aaa";
    private static Boolean FLAG = false;

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
        chat_btn=(ImageButton)view.findViewById(R.id.button_main);
        onTalkState();
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
                    message.setName(hoster);
                    sendMsg(message);
                    //开启发送线程
                }
            }
        });
        //MsgInit();
        FLAG = true ;
        msgAdapter = new MsgAdapter(getActivity(), R.layout.chat_item_message, msgList);
        listView.setAdapter(msgAdapter);
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    //        Message message = new Message();
                   //        message = getMsg();
                  //        Msg receivedmsg = new Msg(message.getMessage(),Msg.TYP_RECIEVED,message.getName(),System.currentTimeMillis());
                            Msg receivedmsg = new Msg("hello", Msg.TYP_RECIEVED, "bbb", System.currentTimeMillis());
                            msgList.add(receivedmsg);
                             android.os.Message passmsg=new android.os.Message();
                             passmsg.what=1;
                             handler.sendMessage(passmsg);
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

            }
        }
        }).start();}

    Handler handler = new Handler() {
    public void handleMessage(android.os.Message msg){
          super.handleMessage(msg);
            if(msg.what==1){
                msgAdapter.notifyDataSetChanged();
                listView.setSelection(msgList.size());
            }
        }
    };

    public void MsgInit(){
    }
    private void UsersInit(){
        usersList.clear();
        for(Iterator iterator=onLineUsers.iterator();iterator.hasNext();)
        usersList.add((String)iterator.next());
    }
}
