package com.example.socialmediaapp.Fragment;

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

import com.example.socialmediaapp.Adapter.AdapterUser;
import com.example.socialmediaapp.Model.ModelUser;
import com.example.socialmediaapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UsersFragment extends Fragment {
    RecyclerView rcv_users;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseDatabase database;
    DatabaseReference databaseReference;

    ArrayList<ModelUser> arrls_users;
    AdapterUser adapterUser;
    public UsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        rcv_users = view.findViewById(R.id.rcv_users);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Users");

        arrls_users = new ArrayList<>();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rcv_users.setLayoutManager(layoutManager);
        adapterUser = new AdapterUser(getContext(), arrls_users);
        rcv_users.setAdapter(adapterUser);
        getAllUsers();

        return view;
    }

    private void getAllUsers() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrls_users.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    if (!dataSnapshot.child("uid").getValue().equals(currentUser.getUid())){
                        ModelUser temp_user = dataSnapshot.getValue(ModelUser.class);
                        arrls_users.add(temp_user);
                    }
                }
                adapterUser.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchUsers(String query) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrls_users.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    ModelUser temp_user = dataSnapshot.getValue(ModelUser.class);
                    if (!dataSnapshot.child("uid").getValue().equals(currentUser.getUid())){
                        if (temp_user.getName().toLowerCase().contains(query.toLowerCase()) ||
                        temp_user.getEmail().toLowerCase().contains(query.toLowerCase())){
                            arrls_users.add(temp_user);
                        }
                    }
                }
                adapterUser.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

        MenuItem it_addpost = menu.findItem(R.id.it_addpost);
        it_addpost.setVisible(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query.trim())) {
                    searchUsers(query);
                } else {
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText.trim())) {
                    searchUsers(newText);
                } else {
                    getAllUsers();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}