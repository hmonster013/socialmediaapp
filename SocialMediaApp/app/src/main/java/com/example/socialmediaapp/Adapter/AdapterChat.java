package com.example.socialmediaapp.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaapp.Model.ModelChat;
import com.example.socialmediaapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterChat extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final int VIEW_TYPE_SENDER = 1;
    private static final int VIEW_TYPE_RECEIVER = 2;
    ArrayList<ModelChat> arrls_chats;
    String currentUserId;
    String urlImage;
    Context context;

    public AdapterChat() {

    }

    public AdapterChat(Context context, ArrayList<ModelChat> arrls_chats, String currentUserId, String urlImage) {
        this.arrls_chats = arrls_chats;
        this.currentUserId = currentUserId;
        this.urlImage = urlImage;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_SENDER){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_chat_right, parent, false);
            return new ChatHolderRight(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_chat_left, parent, false);
            return new ChatHolderLeft(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ModelChat temp_chat = arrls_chats.get(position);
        //Định dạng kiểu dữ liệu thời gian
        Date date = new Date(Long.parseLong(temp_chat.getTimestamp()));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm aa", Locale.getDefault());
        String formattedDate = simpleDateFormat.format(date);

        if (holder instanceof ChatHolderLeft) {
            //Setting đoạn chat bên trái
            try {
                Picasso.get().load(urlImage).into(((ChatHolderLeft) holder).civ_avatar);
            } catch (Exception e) {
                ((ChatHolderLeft) holder).civ_avatar.setImageResource(R.drawable.ic_default_avatar);
            }

            ((ChatHolderLeft) holder).txv_message.setText(temp_chat.getMessage());
            ((ChatHolderLeft) holder).txv_time.setText(formattedDate);
            // Gán text cho trạng thái
            if (temp_chat.isSeen()) {
                ((ChatHolderLeft) holder).txv_isseen.setText("Đã nhận");
            } else {
                ((ChatHolderLeft) holder).txv_isseen.setText("Đã gửi");
            }
            // Nếu là tin nhắn cuối cùng thì hiển thị trạng thái
            if (position == arrls_chats.size() - 1) {
                ((ChatHolderLeft) holder).txv_isseen.setVisibility(View.VISIBLE);
            } else {
                ((ChatHolderLeft) holder).txv_isseen.setVisibility(View.GONE);
            }
        } else {
            // Setting đoạn chat bên phải
            ((ChatHolderRight) holder).txv_message.setText(temp_chat.getMessage());
            ((ChatHolderRight) holder).txv_time.setText(formattedDate);
            // Gán text cho trạng thái
            if (temp_chat.isSeen()) {
                ((ChatHolderRight) holder).txv_isseen.setText("Đã nhận");
            } else {
                ((ChatHolderRight) holder).txv_isseen.setText("Đã gửi");
            }
            // Nếu là tin nhắn cuối cùng thì hiển thị trạng thái
            if (position == arrls_chats.size() - 1) {
                ((ChatHolderRight) holder).txv_isseen.setVisibility(View.VISIBLE);
            } else {
                ((ChatHolderRight) holder).txv_isseen.setVisibility(View.GONE);
            }
            ((ChatHolderRight) holder).txv_message.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Thông báo");
                    builder.setMessage("Bạn có muốn xóa tin nhắn này không");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference databaseReference = database.getReference("Chats");
                            databaseReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        if (dataSnapshot.child("sender").getValue(String.class).equals(currentUserId) &&
                                                dataSnapshot.child("message").getValue(String.class).equals(temp_chat.getMessage())) {
                                            databaseReference.child(dataSnapshot.getKey()).child("message").setValue("Tin nhắn đã bị xóa");
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    });
                    builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.create().show();
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        ModelChat temp_chat = arrls_chats.get(position);
        if (temp_chat.getSender().equals(currentUserId)){
            return VIEW_TYPE_SENDER;
        }
        return VIEW_TYPE_RECEIVER;
    }

    @Override
    public int getItemCount() {
        if (arrls_chats.isEmpty()) {
            return 0;
        }
        return arrls_chats.size();
    }

    public static class ChatHolderLeft extends RecyclerView.ViewHolder{
        CircleImageView civ_avatar;
        TextView txv_message;
        TextView txv_time;
        TextView txv_isseen;
        public ChatHolderLeft(View view) {
            super(view);
            civ_avatar = view.findViewById(R.id.civ_avatar);
            txv_message = view.findViewById(R.id.txv_message);
            txv_time = view.findViewById(R.id.txv_time);
            txv_isseen = view.findViewById(R.id.txv_isseen);
        }
    }

    public static class ChatHolderRight extends RecyclerView.ViewHolder{
        TextView txv_message;
        TextView txv_time;
        TextView txv_isseen;
        public ChatHolderRight(View view) {
            super(view);
            txv_message = view.findViewById(R.id.txv_message);
            txv_time = view.findViewById(R.id.txv_time);
            txv_isseen = view.findViewById(R.id.txv_isseen);
        }
    }
}
