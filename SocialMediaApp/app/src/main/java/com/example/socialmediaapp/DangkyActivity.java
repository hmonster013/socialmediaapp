package com.example.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialmediaapp.Model.ModelUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DangkyActivity extends AppCompatActivity {
    TextView txv_startlogin;
    EditText edt_email;
    EditText edt_matkhau;
    Button bt_dangky;
    ProgressDialog progressDialog;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangky);

        txv_startlogin = findViewById(R.id.txv_startlogin);
        edt_email = findViewById(R.id.edt_email);
        edt_matkhau = findViewById(R.id.edt_matkhau);
        bt_dangky = findViewById(R.id.bt_dangky);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Đang tạo tài khoản...");

        mAuth = FirebaseAuth.getInstance();

        bt_dangky.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edt_email.getText().toString();
                String matkhau = edt_matkhau.getText().toString();

                createNewUser(email, matkhau);
            }
        });

        txv_startlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DangkyActivity.this, DangnhapActivity.class);
                startActivity(intent);
            }
        });
    }

    private void createNewUser(String email, String matkhau) {
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, matkhau)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("Trang thai", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            ModelUser user1 = new ModelUser(user.getEmail(), user.getUid(), "", "", "", "");
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference reference = database.getReference("Users");
                            reference.child(user1.getUid()).setValue(user1);

                            Intent intent = new Intent(DangkyActivity.this, DashboardActivity.class);
                            startActivity(intent);
                        } else {
                            Log.w("Trang thai", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(DangkyActivity.this, "Đăng ký tài khoản không thành công",
                                    Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });
    }
}