package com.example.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialmediaapp.Adapter.AdapterComment;
import com.example.socialmediaapp.Model.ModelComment;
import com.example.socialmediaapp.Model.ModelPost;
import com.example.socialmediaapp.Model.ModelUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostDetailActivity extends AppCompatActivity {
    // Post View
    Toolbar toolbar;
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
    Button bt_share;
    LinearLayout lnl_thereProfile;

    // Comment View
    CircleImageView civ_avatar;
    EditText edt_comment;
    ImageButton bt_send;
    RecyclerView rcv_comments;

    // Firebase variable
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    DatabaseReference likesReferences = database.getReference("Likes");
    DatabaseReference postsReferences = database.getReference("Post");
    DatabaseReference userReferences = database.getReference("Users");

    // Base variable
    boolean isLike;
    ModelPost currentPost;
    ModelUser modelCurrentUser;
    String currentpId;
    ProgressDialog progressDialog;
    ArrayList<ModelComment> arrlsComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        toolbar = findViewById(R.id.toolbar);
        civ_uAvatar = findViewById(R.id.civ_uAvatar);
        txv_uName = findViewById(R.id.txv_uName);
        txv_pTime = findViewById(R.id.txv_pTime);
        btMore = findViewById(R.id.btMore);
        txv_pTitle = findViewById(R.id.txv_pTitle);
        txv_pDescription = findViewById(R.id.txv_pDescription);
        iv_pImage = findViewById(R.id.iv_pImage);
        txv_likes = findViewById(R.id.txv_likes);
        txv_comments = findViewById(R.id.txv_comments);
        bt_like = findViewById(R.id.bt_like);
        bt_share = findViewById(R.id.bt_share);
        lnl_thereProfile = findViewById(R.id.lnl_thereProfile);

        civ_avatar = findViewById(R.id.civ_avatar);
        edt_comment = findViewById(R.id.edt_comment);
        bt_send = findViewById(R.id.bt_send);
        rcv_comments = findViewById(R.id.rcv_comments);

        currentpId = getIntent().getStringExtra("pId");
        arrlsComments = new ArrayList<>();

        // Cài đặt RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        layoutManager.setStackFromEnd(true);
        rcv_comments.setLayoutManager(layoutManager);

        // Cài đặt toolBar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Chi tiết bài viết");

        progressDialog = new ProgressDialog(this);

        // Lấy dữ liệu cho bài viết
        getDataCurrentPost();
        // Lây dữ liệu cho người dùng hiện tại
        getDataCurrentUser();
        //Lấy dữ liệu comments của post hiện tại
        getDataComments();

        likesReferences.child(currentpId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(currentUser.getUid())) {
                    isLike = true;
                    bt_like.setText("Đã thích");
                } else {
                    isLike = false;
                    bt_like.setText("Thích");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Thông báo", error.getMessage());
            }
        });

        // Set sự kiện click vào header của post
        lnl_thereProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] item = {"Xem trang cá nhân", "Nhắn tin"};

                AlertDialog.Builder builder = new AlertDialog.Builder(PostDetailActivity.this);
                builder.setItems(item, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent intent = new Intent(PostDetailActivity.this, ThereProfileActivity.class);
                            intent.putExtra("hisUid", currentPost.getUid());
                            PostDetailActivity.this.startActivity(intent);
                        } else {
                            Intent intent = new Intent(PostDetailActivity.this, ChatActivity.class);
                            intent.putExtra("hisUid", currentPost.getUid());
                            PostDetailActivity.this.startActivity(intent);
                        }
                    }
                });

                builder.create().show();
            }
        });

        // Set sự kiện click cho bt more
        btMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(PostDetailActivity.this, v);
                MenuInflater menuInflater = popupMenu.getMenuInflater();
                menuInflater.inflate(R.menu.menu_post, popupMenu.getMenu());

                if (!currentPost.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    MenuItem editItem = popupMenu.getMenu().findItem(R.id.it_editPost);
                    editItem.setVisible(false);
                    MenuItem deleteItem = popupMenu.getMenu().findItem(R.id.it_deletePost);
                    deleteItem.setVisible(false);
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.it_openPost) {
                            Intent intent = new Intent(PostDetailActivity.this, PostDetailActivity.class);
                            intent.putExtra("pId", currentPost.getpId());
                            startActivity(intent);
                        }
                        if (item.getItemId() == R.id.it_editPost) {
                            Intent intent = new Intent(PostDetailActivity.this, AddPostActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("isPost", currentPost);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            return true;
                        }
                        if (item.getItemId() == R.id.it_deletePost) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(PostDetailActivity.this);
                            builder.setTitle("Thông báo");
                            builder.setMessage("Bạn có muốn xóa bài viết không");
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference reference = database.getReference("Post/" + currentPost.getpId());
                                    reference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(PostDetailActivity.this, "Xóa bài viết thành công", Toast.LENGTH_SHORT);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(PostDetailActivity.this, "Xóa bài viết thất bại", Toast.LENGTH_SHORT);
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
        bt_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLike) {
                    postsReferences.child(currentPost.getpId()).child("pLike").setValue(currentPost.getpLike() - 1);
                    likesReferences.child(currentPost.getpId()).child(currentUser.getUid()).removeValue();
                    isLike = false;
                    bt_like.setText("Thích");
                } else {
                    postsReferences.child(currentPost.getpId()).child("pLike").setValue(currentPost.getpLike() + 1);
                    likesReferences.child(currentPost.getpId()).child(currentUser.getUid()).setValue(true);
                    isLike = true;
                    bt_like.setText("Đã thích");
                }
            }
        });
        // Comment Post
        bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setTitle("Đang bình luận...");
                progressDialog.show();

                String timestamp = String.valueOf(System.currentTimeMillis());

                DatabaseReference commentReference = postsReferences.child(currentPost.getpId()).child("Comments");
                ModelComment comment = new ModelComment(timestamp,
                        edt_comment.getText().toString(),
                        timestamp,
                        modelCurrentUser.getUid(),
                        modelCurrentUser.getEmail(),
                        modelCurrentUser.getImage(),
                        modelCurrentUser.getName());

                commentReference.child(timestamp).setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Tăng số bình luận của post lên
                        DatabaseReference reference = postsReferences.child(currentPost.getpId());
                        reference.child("pComments").setValue(currentPost.getpComments() + 1);
                        progressDialog.dismiss();
                        edt_comment.setText("");
                        Toast.makeText(PostDetailActivity.this, "Đã bình luận", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(PostDetailActivity.this, "Bình luận không thành công", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    // Sử lý dữ liệu của current post
    private void getDataCurrentPost() {
        DatabaseReference reference = postsReferences.child(currentpId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentPost = new ModelPost(snapshot.child("uid").getValue(String.class),
                        snapshot.child("uName").getValue(String.class),
                        snapshot.child("uEmail").getValue(String.class),
                        snapshot.child("uDp").getValue(String.class),
                        snapshot.child("pId").getValue(String.class),
                        snapshot.child("pImage").getValue(String.class),
                        snapshot.child("pTime").getValue(String.class),
                        snapshot.child("pTitle").getValue(String.class),
                        snapshot.child("pDescr").getValue(String.class),
                        snapshot.child("pComments").getValue(Integer.class),
                        snapshot.child("pLike").getValue(Integer.class));

                //Định dạng kiểu dữ liệu thời gian
                Date date = new Date(Long.parseLong(currentPost.getpTime()));
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm aa", Locale.getDefault());
                String formattedDate = simpleDateFormat.format(date);

                // Gán dữ liệu vào view
                try {
                    Picasso.get().load(currentPost.getuDp()).into(civ_uAvatar);
                } catch (Exception e) {
                    civ_uAvatar.setImageResource(R.drawable.ic_default_avatar);
                }
                txv_uName.setText(currentPost.getuName());
                txv_pTime.setText(formattedDate);
                txv_pTitle.setText(currentPost.getpTitle());
                txv_pDescription.setText(currentPost.getpDescr());
                txv_likes.setText(currentPost.getpLike() + " thích");
                txv_comments.setText(currentPost.getpComments() + " bình luận");
                try {
                    Picasso.get().load(currentPost.getpImage()).into(iv_pImage);
                } catch (Exception e) {
                    iv_pImage.setVisibility(View.GONE);
                }

                // Set cho toolbar
                getSupportActionBar().setSubtitle("Tác giả: " + currentPost.getuEmail());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    // Lấy dữ liệu của người dùng hiện tại
    private void getDataCurrentUser() {
        DatabaseReference reference = userReferences.child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelCurrentUser = new ModelUser(snapshot.child("email").getValue(String.class),
                        snapshot.child("uid").getValue(String.class),
                        snapshot.child("name").getValue(String.class),
                        snapshot.child("phone").getValue(String.class),
                        snapshot.child("image").getValue(String.class),
                        snapshot.child("cover").getValue(String.class));
                try {
                    Picasso.get().load(modelCurrentUser.getImage()).into(civ_avatar);
                } catch (Exception e) {
                    civ_avatar.setImageResource(R.drawable.ic_default_avatar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    // Lấy dữ liệu comments của post
    private void getDataComments() {
        progressDialog.setTitle("Đang tải bình luận...");
        progressDialog.show();
        DatabaseReference reference = postsReferences.child(currentpId).child("Comments");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrlsComments.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ModelComment modelComment = new ModelComment(dataSnapshot.child("cId").getValue(String.class),
                            dataSnapshot.child("comment").getValue(String.class),
                            dataSnapshot.child("timestamp").getValue(String.class),
                            dataSnapshot.child("uid").getValue(String.class),
                            dataSnapshot.child("uEmail").getValue(String.class),
                            dataSnapshot.child("uDp").getValue(String.class),
                            dataSnapshot.child("uName").getValue(String.class));
                    arrlsComments.add(modelComment);
                }
                AdapterComment adapterComment = new AdapterComment(PostDetailActivity.this, arrlsComments, currentUser.getUid(), currentpId);
                rcv_comments.setAdapter(adapterComment);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}