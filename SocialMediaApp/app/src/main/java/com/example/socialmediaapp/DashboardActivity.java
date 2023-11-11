package com.example.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.socialmediaapp.Fragment.ChatListFragment;
import com.example.socialmediaapp.Fragment.HomeFragment;
import com.example.socialmediaapp.Fragment.MenuFragment;
import com.example.socialmediaapp.Fragment.ProfileFragment;
import com.example.socialmediaapp.Fragment.UsersFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {
    Toolbar toolbar;

    FirebaseAuth mAuth;
    BottomNavigationView bnv_menu;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        bnv_menu = findViewById(R.id.bnv_menu);
        toolbar = findViewById(R.id.action_bar);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //Cài đặt toolBar
        toolbar.setTitle("Trang chủ");
        setSupportActionBar(toolbar);

        //Fragment mặc định
        if (currentUser != null) {
            HomeFragment homeFragment = new HomeFragment();
            createNewFragment(homeFragment);
        }

        bnv_menu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int currentId = item.getItemId();
                if (currentId == R.id.it_home) {
                    toolbar.setTitle("Trang chủ");
                    HomeFragment homeFragment = new HomeFragment();
                    createNewFragment(homeFragment);
                }
                if (currentId == R.id.it_friends) {
                    toolbar.setTitle("Bạn bè");
                    UsersFragment usersFragment = new UsersFragment();
                    createNewFragment(usersFragment);
                }
                if (currentId == R.id.it_chats) {
                    toolbar.setTitle("Tin nhắn");
                    ChatListFragment chatListFragment = new ChatListFragment();
                    createNewFragment(chatListFragment);
                }
                if (currentId == R.id.it_me) {
                    toolbar.setTitle("Trang cá nhân");
                    ProfileFragment profileFragment = new ProfileFragment();
                    createNewFragment(profileFragment);
                }
                if (currentId == R.id.it_menu) {
                    toolbar.setTitle("Menu");
                    MenuFragment menuFragment = new MenuFragment();
                    createNewFragment(menuFragment);
                }
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

    private void checkUserStatus() {
        if (currentUser == null) {
            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void createNewFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fgm_container, fragment)
                .commit();
    }
}