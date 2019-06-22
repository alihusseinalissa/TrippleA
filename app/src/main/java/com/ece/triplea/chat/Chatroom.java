package com.ece.triplea.chat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ece.triplea.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Chatroom extends AppCompatActivity {

    private DatabaseReference root ;
    private ImageButton btn_send_msg ;

    private EditText input_msg ;
    private String mMode;
    private String mChildName;
    private String mSenderName;
    private String mUserName;
    private String chat_room_name ;


    private String temp_key ;

    ChatAppMsgAdapter adapter ;
    RecyclerView myView ;
    List<ChatAppMsgDTO> myValues ;
    CardView cardView ;
    TextView textView ;
    private String chat_msg ,chat_user_name ;
    LinearLayoutManager llm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        input_msg = (EditText)findViewById(R.id.editText);
        btn_send_msg = (ImageButton) findViewById(R.id.button);


        myValues = new ArrayList<ChatAppMsgDTO>();
        adapter = new ChatAppMsgAdapter(myValues);
        myView =  (RecyclerView)findViewById(R.id.recyclerview);
        myView.setHasFixedSize(true);
        myView.setAdapter(adapter);
        llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        myView.setLayoutManager(llm);
        cardView =(CardView)findViewById(R.id.card_view) ;
        textView = (TextView)findViewById(R.id.text_cardview);



        mMode = getIntent().getExtras().getString("mode");
        mChildName = getIntent().getExtras().getString("childName");
        mUserName = getIntent().getExtras().getString("userName");
        chat_room_name = getIntent().getExtras().getString("chatroom");
        mSenderName = mMode.equals("parent") ? mChildName : "Your Parent";
        this.setTitle("Chat with " + mSenderName);

        root = FirebaseDatabase.getInstance().getReference().child(chat_room_name);



        btn_send_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String, Object> map = new HashMap<>();
                temp_key = root.push().getKey();
                root.updateChildren(map);

                DatabaseReference message_root = root.child(temp_key);
                Map<String, Object> map2 = new HashMap<>();
                map2.put("name", mMode);
                map2.put("msg", input_msg.getText().toString());

                message_root.updateChildren(map2);

                input_msg.setText("");
                myView.scrollToPosition(myValues.size());

            }
        });


        root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Add_Chat(dataSnapshot);


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Add_Chat(dataSnapshot);

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void Add_Chat(DataSnapshot dataSnapshot){

        Iterator i = dataSnapshot.getChildren().iterator();
        input_msg.setText("");
        while (i.hasNext()){
            chat_msg = (String)((DataSnapshot)i.next()).getValue();
            chat_user_name =(String)((DataSnapshot)i.next()).getValue();
            ChatAppMsgDTO msgDto;



            if(chat_user_name.equals(mMode)){
                msgDto = new ChatAppMsgDTO(ChatAppMsgDTO.MSG_TYPE_SENT, "You: " + chat_msg);
                myValues.add(msgDto);
            }else {
                msgDto = new ChatAppMsgDTO(ChatAppMsgDTO.MSG_TYPE_RECEIVED, mSenderName + ": " + chat_msg);
                myValues.add(msgDto);
//                Notification.Builder mBuilder = new Notification.Builder(Chatroom.this);
//
//                //Step 2
//                mBuilder.setSmallIcon(R.drawable.ic_message_black_24dp);
//                mBuilder.setContentTitle("New Message");
//                mBuilder.setContentText(msgDto.getMsgContent());
//
//                mBuilder.setAutoCancel(true);
//
//
//                Intent intent = new Intent(Chatroom.this,MapsActivity.class);
////                intent.putExtra(DESC_KEY,detailText);
//                PendingIntent pIntent = PendingIntent.getActivity(Chatroom.this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
//                mBuilder.setContentIntent(pIntent);
//
//                //Step 3
//                Notification notif = mBuilder.build();
//
//                //Step 4
//                NotificationManager notifyMngr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//
//                int notificationId = 103;
//                notifyMngr.notify(notificationId,notif);
            }



            adapter.notifyItemInserted(myValues.size()-1);
            myView.scrollToPosition(myValues.size()-1);

        }

    }
}
