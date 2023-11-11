package com.example.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialmediaapp.Model.ModelUser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DangnhapActivity extends AppCompatActivity {
    private static final  int RC_SIGN_IN = 100;
    FirebaseAuth mAuth;
    EditText edt_email;
    EditText edt_matkhau;
    TextView txv_startregister;
    TextView txv_quenmk;
    Button bt_dangnhap;
    SignInButton sibt_google;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangnhap);

        edt_email = findViewById(R.id.edt_email);
        edt_matkhau = findViewById(R.id.edt_matkhau);
        txv_startregister = findViewById(R.id.txv_startregister);
        txv_quenmk = findViewById(R.id.txv_quenmk);
        bt_dangnhap = findViewById(R.id.bt_dangnhap);
        sibt_google = findViewById(R.id.sibt_google);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Đang đăng nhập...");

        txv_startregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DangnhapActivity.this, DangkyActivity.class);
                startActivity(intent);
            }
        });

        txv_quenmk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DangnhapActivity.this);

                LinearLayout layout = new LinearLayout(DangnhapActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);

                EditText edt_email = new EditText(DangnhapActivity.this);
                edt_email.setHint("Email");

                int marginValue = 50;
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(marginValue, 20, marginValue, 20);
                edt_email.setLayoutParams(params);

                layout.addView(edt_email);

                builder.setView(layout);
                builder.setTitle("Quên mật khẩu");

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.sendPasswordResetEmail(edt_email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                ProgressDialog resetpassDialog = new ProgressDialog(DangnhapActivity.this);
                                resetpassDialog.setTitle("Đang gửi email...");
                                resetpassDialog.show();
                                if (task.isSuccessful()) {
                                    Toast.makeText(DangnhapActivity.this, "Bạn vui lòng kiểm tra email để đổi mật khẩu", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(DangnhapActivity.this, "Gửi email không thành công", Toast.LENGTH_SHORT).show();
                                }
                                resetpassDialog.dismiss();
                            }
                        });
                    }
                });

                builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        bt_dangnhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edt_email.getText().toString();
                String matkhau = edt_matkhau.getText().toString();
                loginUser(email, matkhau);
            }
        });

        sibt_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Khởi tạo GoogleSignInOptions
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                // Khởi tạo GoogleSignInClient với GoogleSignInOptions
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(DangnhapActivity.this, gso);

                // Bắt đầu quy trình đăng nhập bằng Google khi người dùng nhấn vào nút
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //Đăng nhập thành công, bạn có thể lấy thông tin người dùng tại đây
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String idToken = account.getIdToken();
                AuthCredential authCredential = GoogleAuthProvider.getCredential(idToken, null);
                loginUser(authCredential);
            } catch (ApiException e) {
                Log.w("Trạng thái", "Đăng nhập google thất bại: " + e.getStatusCode());
            }
        }
    }

    private void loginUser(String email, String matkhau) {
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, matkhau)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("Trang thai", "signInWithEmail:success");
                            Intent intent = new Intent(DangnhapActivity.this, DashboardActivity.class);
                            startActivity(intent);
                        } else {
                            Log.w("Trang thai", "signInWithEmail:failure", task.getException());
                            Toast.makeText(DangnhapActivity.this, "Đăng nhập không thành công",
                                    Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    private void loginUser(AuthCredential credential) {
        progressDialog.show();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("Trang thai", "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        ModelUser user1 = new ModelUser(user.getEmail(), user.getUid(), "", "", "", "");
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference reference = database.getReference("Users");
                        reference.child(user1.getUid()).setValue(user1);
                        Intent intent = new Intent(DangnhapActivity.this, DashboardActivity.class);
                        startActivity(intent);
                    } else {
                        Log.w("Trang thai", "signInWithEmail:failure", task.getException());
                        Toast.makeText(DangnhapActivity.this, "Đăng nhập không thành công",
                                Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
    }
}