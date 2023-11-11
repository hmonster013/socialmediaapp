package com.example.socialmediaapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaapp.ChatActivity;
import com.example.socialmediaapp.Model.ModelUser;
import com.example.socialmediaapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.UserHolder>{
    Context context;

    ArrayList<ModelUser> arrls_users;
    public AdapterUser() {

    }

    public AdapterUser(Context context, ArrayList<ModelUser> arrls_users) {
        this.arrls_users = arrls_users;
        this.context = context;
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_user, parent, false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, int position) {
        ModelUser temp_user = arrls_users.get(position);
        try {

            Picasso.get().load("" + temp_user.getImage()).into(holder.civ_avatar);
        } catch (Exception e) {
            holder.civ_avatar.setImageResource(R.drawable.ic_default_avatar);
        }
        holder.txv_email.setText(temp_user.getEmail());
        holder.txv_name.setText(temp_user.getName());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("hisUid", temp_user.getUid());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        if (arrls_users.isEmpty()) {
            return 0;
        }
        return arrls_users.size();
    }

    public static class UserHolder extends RecyclerView.ViewHolder{
        CircleImageView civ_avatar;
        TextView txv_name;
        TextView txv_email;
        public UserHolder(View view){
            super(view);
            civ_avatar = view.findViewById(R.id.civ_avatar);
            txv_name = view.findViewById(R.id.txv_name);
            txv_email = view.findViewById(R.id.txv_email);
        }
    }
}
