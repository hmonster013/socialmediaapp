package com.example.socialmediaapp.Adapter;

import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaapp.AddPostActivity;
import com.example.socialmediaapp.ChatActivity;
import com.example.socialmediaapp.Model.ModelPost;
import com.example.socialmediaapp.PostDetailActivity;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.ThereProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class AdapterPost extends RecyclerView.Adapter<AdapterPost.PostHolder>{
    ArrayList<ModelPost> arrls_post;
    Context context;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference likesReferences = database.getReference("Likes");
    DatabaseReference postsReferences = database.getReference("Post");
    FirebaseUser currentUser = mAuth.getCurrentUser();
    public AdapterPost() {

    }

    public AdapterPost(Context context, ArrayList<ModelPost> arrls_post) {
        this.context = context;
        this.arrls_post = arrls_post;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_post, parent, false);
        return new PostHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, int position) {
        ModelPost modelPost = arrls_post.get(position);

        likesReferences.child(modelPost.getpId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(currentUser.getUid())) {
                    holder.isLike = true;
                    holder.bt_like.setText("Đã thích");
                } else {
                    holder.isLike = false;
                    holder.bt_like.setText("Thích");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Thông báo", error.getMessage());
            }
        });

        //Định dạng kiểu dữ liệu thời gian
        Date date = new Date(Long.parseLong(modelPost.getpTime()));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm aa", Locale.getDefault());
        String formattedDate = simpleDateFormat.format(date);

        // Gán dữ liệu vào view
        try {
            Picasso.get().load(modelPost.getuDp()).into(holder.civ_uAvatar);
        } catch (Exception e) {
            holder.civ_uAvatar.setImageResource(R.drawable.ic_default_avatar);
        }
        holder.txv_uName.setText(modelPost.getuName());
        holder.txv_pTime.setText(formattedDate);
        holder.txv_pTitle.setText(modelPost.getpTitle());
        holder.txv_pDescription.setText(modelPost.getpDescr());
        holder.txv_likes.setText(modelPost.getpLike() + " thích");
        holder.txv_comments.setText(modelPost.getpComments() + " bình luận");
        try {
            Picasso.get().load(modelPost.getpImage()).into(holder.iv_pImage);
        } catch (Exception e) {
            holder.iv_pImage.setVisibility(View.GONE);
        }

        // Set sự kiện click vào header của post
        holder.lnl_thereProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] item = {"Xem trang cá nhân", "Nhắn tin"};

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(item, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent intent = new Intent(context, ThereProfileActivity.class);
                            intent.putExtra("hisUid", modelPost.getUid());
                            context.startActivity(intent);
                        } else {
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("hisUid", modelPost.getUid());
                            context.startActivity(intent);
                        }
                    }
                });

                builder.create().show();
            }
        });

        // Set sự kiện click cho bt more
        holder.btMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, v);
                MenuInflater menuInflater = popupMenu.getMenuInflater();
                menuInflater.inflate(R.menu.menu_post, popupMenu.getMenu());

                if (!modelPost.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    MenuItem editItem = popupMenu.getMenu().findItem(R.id.it_editPost);
                    editItem.setVisible(false);
                    MenuItem deleteItem = popupMenu.getMenu().findItem(R.id.it_deletePost);
                    deleteItem.setVisible(false);
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.it_openPost) {
                            Intent intent = new Intent(context, PostDetailActivity.class);
                            intent.putExtra("pId", modelPost.getpId());
                            context.startActivity(intent);
                        }
                        if (item.getItemId() == R.id.it_editPost) {
                            Intent intent = new Intent(context, AddPostActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("isPost", modelPost);
                            intent.putExtras(bundle);
                            context.startActivity(intent);
                            return true;
                        }
                        if (item.getItemId() == R.id.it_deletePost) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Thông báo");
                            builder.setMessage("Bạn có muốn xóa bài viết không");
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference reference = database.getReference("Post/" + modelPost.getpId());
                                    reference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(context, "Xóa bài viết thành công", Toast.LENGTH_SHORT);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context, "Xóa bài viết thất bại", Toast.LENGTH_SHORT);
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
                        }
                        return false;
                    }
                });

                popupMenu.show();
            }
        });

        // Like Post
        holder.bt_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.isLike) {
                    postsReferences.child(modelPost.getpId()).child("pLike").setValue(modelPost.getpLike() - 1);
                    likesReferences.child(modelPost.getpId()).child(currentUser.getUid()).removeValue();
                    holder.isLike = false;
                    holder.bt_like.setText("LIKE");
                } else {
                    postsReferences.child(modelPost.getpId()).child("pLike").setValue(modelPost.getpLike() + 1);
                    likesReferences.child(modelPost.getpId()).child(currentUser.getUid()).setValue(true);
                    holder.isLike = true;
                    holder.bt_like.setText("LIKED");
                }
            }
        });

        // Comment Post
        holder.bt_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("pId", modelPost.getpId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (arrls_post.isEmpty()) {
            return 0;
        }
        return arrls_post.size();
    }

    public static class PostHolder extends RecyclerView.ViewHolder {
        LinearLayout lnl_thereProfile;
        CircleImageView civ_uAvatar;
        TextView txv_uName;
        TextView txv_pTime;
        ImageButton btMore;
        TextView txv_pTitle;
        TextView txv_pDescription;
        ImageView iv_pImage;
        TextView txv_likes;
        TextView txv_comments;
        Button bt_like;
        Button bt_comment;
        Button bt_share;
        boolean isLike;
        public PostHolder(View view) {
            super(view);

            lnl_thereProfile = view.findViewById(R.id.lnl_thereProfile);
            civ_uAvatar = view.findViewById(R.id.civ_uAvatar);
            txv_uName = view.findViewById(R.id.txv_uName);
            txv_pTime = view.findViewById(R.id.txv_pTime);
            btMore = view.findViewById(R.id.btMore);
            txv_pTitle = view.findViewById(R.id.txv_pTitle);
            txv_pDescription = view.findViewById(R.id.txv_pDescription);
            iv_pImage = view.findViewById(R.id.iv_pImage);
            txv_likes = view.findViewById(R.id.txv_likes);
            txv_comments = view.findViewById(R.id.txv_comments);
            bt_like = view.findViewById(R.id.bt_like);
            bt_comment = view.findViewById(R.id.bt_comment);
            bt_share = view.findViewById(R.id.bt_share);
        }
    }
}
