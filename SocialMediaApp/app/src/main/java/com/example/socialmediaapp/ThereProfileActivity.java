package com.example.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialmediaapp.Adapter.AdapterPost;
import com.example.socialmediaapp.Model.ModelPost;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ThereProfileActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseDatabase database;
    DatabaseReference userReference;

    Toolbar toolbar;
    ImageView imgv_avatar;
    ImageView imgv_cover;
    TextView txv_name;
    TextView txv_email;
    TextView txv_phone;
    RecyclerView rcv_post;


    ArrayList<ModelPost> arrls_post;
    AdapterPost adapterPost;
    String hisUid;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);

        imgv_avatar = findViewById(R.id.imgv_avatar);
        imgv_cover = findViewById(R.id.imgv_cover);
        txv_name = findViewById(R.id.txv_name);
        txv_email = findViewById(R.id.txv_email);
        txv_phone = findViewById(R.id.txv_phone);
        rcv_post = findViewById(R.id.rcv_post);
        toolbar = findViewById(R.id.toolbar);

        //Setting toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Trang cá nhân");

        //Setting firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        userReference = database.getReference("Users");

        arrls_post = new ArrayList<>();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Đang tải bài viết...");
        hisUid = getIntent().getStringExtra("hisUid");

        //Setting recyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        layoutManager.setStackFromEnd(true);
        rcv_post.setLayoutManager(layoutManager);

        // Read post
        readPost();

        Query query = userReference.child(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txv_name.setText("" + snapshot.child("name").getValue());
                txv_email.setText("" + snapshot.child("email").getValue());
                txv_phone.setText("" + snapshot.child("phone").getValue());

                try {
                    Picasso.get().load("" + snapshot.child("image").getValue()).into(imgv_avatar);
                } catch (Exception e) {
                    imgv_avatar.setImageResource(R.drawable.ic_add_image);
                }

                try {
                    Picasso.get().load("" + snapshot.child("cover").getValue()).into(imgv_cover);
                } catch (Exception e) {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actionbar, menu);

        MenuItem it_search = menu.findItem(R.id.it_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(it_search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query.trim())) {
                    searchPost(query);
                } else {
                    readPost();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText.trim())) {
                    searchPost(newText);
                } else {
                    readPost();
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (item.getItemId() == R.id.it_addpost) {
            Intent intent = new Intent(ThereProfileActivity.this, AddPostActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void readPost() {
        progressDialog.show();
        DatabaseReference reference = database.getReference("Post");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrls_post.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child("uid").getValue(String.class).equals(hisUid)) {
                        ModelPost modelPost = new ModelPost(dataSnapshot.child("uid").getValue(String.class),
                                dataSnapshot.child("uName").getValue(String.class),
                                dataSnapshot.child("uEmail").getValue(String.class),
                                dataSnapshot.child("uDp").getValue(String.class),
                                dataSnapshot.child("pId").getValue(String.class),
                                dataSnapshot.child("pImage").getValue(String.class),
                                dataSnapshot.child("pTime").getValue(String.class),
                                dataSnapshot.child("pTitle").getValue(String.class),
                                dataSnapshot.child("pDescr").getValue(String.class),
                                dataSnapshot.child("pComments").getValue(Integer.class),
                                dataSnapshot.child("pLike").getValue(Integer.class));
                        arrls_post.add(modelPost);
                    }
                }
                adapterPost = new AdapterPost(ThereProfileActivity.this ,arrls_post);
                rcv_post.setAdapter(adapterPost);
                adapterPost.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(ThereProfileActivity.this, "Tải bài viết không thành công", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void searchPost(String query) {
        DatabaseReference reference = database.getReference("Post");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrls_post.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child("uid").getValue(String.class).equals(hisUid)) {
                        if (dataSnapshot.child("pTitle").getValue(String.class).contains(query)){
                            ModelPost modelPost = new ModelPost(dataSnapshot.child("uid").getValue(String.class),
                                    dataSnapshot.child("uName").getValue(String.class),
                                    dataSnapshot.child("uEmail").getValue(String.class),
                                    dataSnapshot.child("uDp").getValue(String.class),
                                    dataSnapshot.child("pId").getValue(String.class),
                                    dataSnapshot.child("pImage").getValue(String.class),
                                    dataSnapshot.child("pTime").getValue(String.class),
                                    dataSnapshot.child("pTitle").getValue(String.class),
                                    dataSnapshot.child("pDescr").getValue(String.class),
                                    dataSnapshot.child("pComments").getValue(Integer.class),
                                    dataSnapshot.child("pLike").getValue(Integer.class));
                            arrls_post.add(modelPost);
                        }
                    }
                }
                adapterPost = new AdapterPost(ThereProfileActivity.this, arrls_post);
                rcv_post.setAdapter(adapterPost);
                adapterPost.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}