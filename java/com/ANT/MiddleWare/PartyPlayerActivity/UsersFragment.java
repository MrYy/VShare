package com.ANT.MiddleWare.PartyPlayerActivity;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.ANT.MiddleWare.PartyPlayerActivity.ViewVideoActivity.onLineUsers;



/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {

    private View view;
    private ListView listView;
    private EditText editText;
    private ImageButton chat_btn;
    private List<String> usersList=new ArrayList<String>();
    private UsersAdapter usersAdapter;


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

    public void onUsersState(){
        UsersInit();
        chat_btn.setBackgroundResource(R.drawable.refresh);
        editText.setHint("  附近的人");
        usersAdapter = new UsersAdapter(getActivity(),R.layout.chat_item_users,usersList);
        listView.setAdapter(usersAdapter);
        listView.setSelection(usersList.size());
        chat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsersInit();
                usersAdapter.notifyDataSetChanged();
                listView.setSelection(usersList.size());
            }
        });
    }

    private void UsersInit(){
        usersList.clear();
        for(Iterator iterator = onLineUsers.iterator(); iterator.hasNext();)
            usersList.add((String)iterator.next());

    }
}
