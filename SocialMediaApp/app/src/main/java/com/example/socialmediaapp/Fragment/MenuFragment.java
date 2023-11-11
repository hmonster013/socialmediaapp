package com.example.socialmediaapp.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.socialmediaapp.MainActivity;
import com.example.socialmediaapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class MenuFragment extends Fragment {
    FirebaseAuth mAuth;
    Button bt_dangxuat;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        bt_dangxuat = view.findViewById(R.id.bt_dangxuat);

        mAuth = FirebaseAuth.getInstance();

        bt_dangxuat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        });
        return view;
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
        it_search.setVisible(false);

        MenuItem it_addpost = menu.findItem(R.id.it_addpost);
        it_addpost.setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}