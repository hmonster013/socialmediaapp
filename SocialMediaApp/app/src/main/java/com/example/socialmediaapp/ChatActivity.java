package com.example.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialmediaapp.Adapter.AdapterChat;
import com.example.socialmediaapp.Model.ModelChat;
import com.example.socialmediaapp.Model.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    Toolbar toolbar;
    CircleImageView civ_avatar;
    TextView txv_name;
    TextView txv_userstatus;
    EditText edt_message;
    ImageButton bt_send;
    RecyclerView rcv_chat;

    FirebaseAuth mAuth;
    FirebaseDatabase database;

    String myUid;
    String hisUid;
    String hisAvatar;
    ArrayList<ModelChat> arrls_chats;
    AdapterChat adapterChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = findViewById(R.id.toolbar);
        civ_avatar = findViewById(R.id.civ_avatar);
        txv_name = findViewById(R.id.txv_name);
        txv_userstatus = findViewById(R.id.txv_userstatus);
        edt_message = findViewById(R.id.edt_message);
        bt_send = findViewById(R.id.bt_send);
        rcv_chat = findViewById(R.id.rcv_chat);

        // Setting toolbar
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        // Setting firebase
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Cập nhật dữ liệu đoạn chat
        Intent intent = getIntent();
        myUid = mAuth.getCurrentUser().getUid();
        hisUid = intent.getStringExtra("hisUid");

        // Cập nhật thông tin của người nhận
        getDataHisUser();

        //Cài đặt cho recyclerView
        arrls_chats = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        rcv_chat.setLayoutManager(layoutManager);
        readMessages();

        // Sự kiện của button gửi tin nhắn
        bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edt_message.getText().toString().isEmpty()){
                    Toast.makeText(ChatActivity.this, "Vui lòng nhập tin nhắn...", Toast.LENGTH_SHORT);
                } else {
                    senMessage(edt_message.getText().toString());
                    edt_message.setText("");
                }
            }
        });
    }

    private void readMessages() {
        DatabaseReference databaseReference = database.getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrls_chats.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ModelChat temp_chat = dataSnapshot.getValue(ModelChat.class);
                    if ((temp_chat.getSender().equals(myUid) && temp_chat.getReceiver().equals(hisUid)) ||
                            (temp_chat.getSender().equals(hisUid) && temp_chat.getReceiver().equals(myUid))) {
                        arrls_chats.add(temp_chat);
                    }
                }
                adapterChat = new AdapterChat(ChatActivity.this,arrls_chats, myUid, hisAvatar);
                rcv_chat.setAdapter(adapterChat);
                adapterChat.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getDataHisUser() {
        DatabaseReference databaseReference = database.getReference("Users");
        Query query = databaseReference.orderByChild("uid").equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ModelUser temp_user = dataSnapshot.getValue(ModelUser.class);
                    try {
                        hisAvatar = temp_user.getImage();
                        Picasso.get().load(hisAvatar).into(civ_avatar);
                    } catch (Exception e) {
                        civ_avatar.setImageResource(R.drawable.ic_default_avatar);
                    }
                    txv_name.setText(temp_user.getName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void senMessage(String message) {
        DatabaseReference chatReference = database.getReference();

        String timestamp = String.valueOf(System.currentTimeMillis());
        HashMap<String, Object> chat = new HashMap<>();
        chat.put("message", message);
        chat.put("sender", myUid);
        chat.put("receiver", hisUid);
        chat.put("timestamp", timestamp);
        chat.put("isseen", false);

        chatReference.child("Chats").push().setValue(chat);
    }
}