package com.example.socialmediaapp.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.socialmediaapp.Adapter.AdapterPost;
import com.example.socialmediaapp.AddPostActivity;
import com.example.socialmediaapp.Model.ModelPost;
import com.example.socialmediaapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    RecyclerView rcv_post;

    FirebaseDatabase database;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    ArrayList<ModelPost> arrls_post;
    AdapterPost adapterPost;
    ProgressDialog progressDialog;
    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rcv_post = view.findViewById(R.id.rcv_post);

        arrls_post = new ArrayList<>();
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Đang tải bài viết...");

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, true);
        layoutManager.setStackFromEnd(true);
        rcv_post.setLayoutManager(layoutManager);
        
        // Read post
        readPost();
        return view;
    }

    private void readPost() {
        progressDialog.show();
        DatabaseReference reference = database.getReference("Post");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                arrls_post.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (!dataSnapshot.child("uid").getValue(String.class).equals(currentUser.getUid().toString())) {
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
                adapterPost = new AdapterPost(getActivity(), arrls_post);
                rcv_post.setAdapter(adapterPost);
                adapterPost.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Tải bài viết không thành công", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true); // Cho biết fragment có menu riêng và sẽ gọi đến onCreateOptionsMenu
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_actionbar, menu);

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
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void searchPost(String query) {
        DatabaseReference reference = database.getReference("Post");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrls_post.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (!dataSnapshot.child("uid").getValue(String.class).equals(currentUser.getUid().toString())) {
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
                adapterPost = new AdapterPost(getActivity(), arrls_post);
                rcv_post.setAdapter(adapterPost);
                adapterPost.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.it_addpost) {
            Intent intent = new Intent(getActivity(), AddPostActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}