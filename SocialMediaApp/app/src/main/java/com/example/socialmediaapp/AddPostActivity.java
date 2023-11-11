package com.example.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.socialmediaapp.Model.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    String cameraPermissions[];
    String storagePermissions[];
    Toolbar toolbar;
    EditText edt_pTitle, edt_pDescription;
    ImageView iv_pImage;
    Button bt_pUpload;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseDatabase database;
    FirebaseStorage firebaseStorage;

    String uId, uName, uEmail, uDp;
    Uri image_uri;
    ProgressDialog progressDialog;
    ModelPost isPost;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        toolbar = findViewById(R.id.toolbar);
        edt_pTitle = findViewById(R.id.edt_pTitle);
        edt_pDescription = findViewById(R.id.edt_pDescription);
        iv_pImage = findViewById(R.id.iv_pImage);
        bt_pUpload = findViewById(R.id.bt_pUpload);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Đăng bài");

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        getDataCurrentUser();

        progressDialog = new ProgressDialog(AddPostActivity.this);
        progressDialog.setTitle("Đang đăng bài...");

        if (checkCurrentPost()) {
            isPost = (ModelPost) getIntent().getExtras().get("isPost");
            setDataIsPost(isPost);
        }

        iv_pImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] item = {"Máy ảnh", "Thư viện"};
                AlertDialog.Builder builder = new AlertDialog.Builder(AddPostActivity.this);
                builder.setTitle("Chọn nơi tải ảnh");
                builder.setItems(item, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            if (checkCameraPermission()) {
                                pickFromCamera();
                            } else {
                                requestCameraPermission();
                            }
                        } else {
                            if (checkStoragePermission()) {
                                pickFromGallery();
                            } else {
                                requestStoragePermission();
                            }
                        }
                    }
                });
                builder.create().show();
            }
        });
        bt_pUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!edt_pTitle.getText().toString().isEmpty()) {
                    if (!edt_pDescription.getText().toString().isEmpty()) {
                        // Xử lý đăng bài hoặc sửa bài viết
                        if (isPost != null) {
                            editPost();
                        } else {
                            addPost();
                        }
                    } else {
                        Toast.makeText(AddPostActivity.this, "Vui lòng nhập nội dung...", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AddPostActivity.this, "Vui lòng nhập tiêu đề...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    // Sửa bài viết
    private void editPost() {
        progressDialog.setTitle("Đang cập nhật bài viết...");
        progressDialog.show();
        DatabaseReference databaseReference = database.getReference("Post/" + isPost.getpId());

        HashMap<String, Object> uploadData = new HashMap<>();
        uploadData.put("pTitle", edt_pTitle.getText().toString());
        uploadData.put("pDescr", edt_pDescription.getText().toString());

        //Nếu ảnh bị thay đổi thì tải ảnh mới lên storage
        if (!image_uri.toString().startsWith("https://firebasestorage.googleapis.com") && !image_uri.toString().equals("")) {
            // Xóa ảnh cũ đi
            if (!isPost.getpImage().equals("")) {
                String fileDeletePath = "Post/" + "post_" + isPost.getpTime();
                StorageReference storageReference_delete = firebaseStorage.getReference(fileDeletePath);
                storageReference_delete.delete().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Thông báo", e.getMessage().toString());
                    }
                });
            }

            // Thêm ảnh vào storage
            String timestamp = String.valueOf(System.currentTimeMillis());
            String filePathAndName = "Post/" + "post_" + timestamp;
            StorageReference storageReference_add = firebaseStorage.getReference().child(filePathAndName);
            storageReference_add.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Lấy đường dẫn của ảnh
                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            uploadData.put("pImage", uri.toString());
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Log.e("Thông báo", e.getMessage().toString());
                }
            });
        }

        // Cập nhật post trên realtime database
        databaseReference.updateChildren(uploadData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.dismiss();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AddPostActivity.this, "Sửa bài viết không thành công, vui lòng thử lại...", Toast.LENGTH_SHORT);
            }
        });
    }

    // Xét xem có post nào được truyền vào k, dùng xét trạng thái edt or add
    private boolean checkCurrentPost() {
        if (getIntent().getExtras() != null) {
            return true;
        }
        return false;
    }

    // Cập nhật post nếu là edit
    private void setDataIsPost(ModelPost isPost) {
        edt_pTitle.setText(isPost.getpTitle());
        edt_pDescription.setText(isPost.getpDescr());
        image_uri = Uri.parse(isPost.getpImage());
        Picasso.get().load(image_uri).into(iv_pImage);
    }

    //Xử lý dữ liệu sau khi đăng bài
    private void addPost() {
        progressDialog.show();
        String timestamp = String.valueOf(System.currentTimeMillis());

        String filePathAndName = "Post/" + "post_" + timestamp;
        StorageReference storageReference = firebaseStorage.getReference().child(filePathAndName);

        DatabaseReference databaseReference = database.getReference();

        if (image_uri != null) {
            storageReference.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            ModelPost modelPost = new ModelPost(uId,
                                    uName,
                                    uEmail,
                                    uDp,
                                    timestamp,
                                    uri.toString(),
                                    timestamp,
                                    edt_pTitle.getText().toString(),
                                    edt_pDescription.getText().toString(),
                                    0,
                                    0);
                            databaseReference.child("Post").child(timestamp).setValue(modelPost).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    progressDialog.dismiss();
                                    Toast.makeText(AddPostActivity.this, "Đăng bài thành công", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(AddPostActivity.this, "Đăng bài thất bại", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            ModelPost modelPost = new ModelPost(uId,
                    uName,
                    uEmail,
                    uDp,
                    timestamp,
                    "",
                    timestamp,
                    edt_pTitle.getText().toString(),
                    edt_pDescription.getText().toString(),
                    0,
                    0);
            databaseReference.child("Post").child(timestamp).setValue(modelPost).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    progressDialog.dismiss();
                    Toast.makeText(AddPostActivity.this, "Đăng bài thành công", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(AddPostActivity.this, "Đăng bài thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    // Mở thư viện ảnh
    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }
    // Mở máy ảnh
    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        image_uri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }
    // Xử lý kết quả trả về sau khi chọn ảnh
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                iv_pImage.setImageURI(image_uri);
            }
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                image_uri = data.getData();
                iv_pImage.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getDataCurrentUser() {
        DatabaseReference reference = database.getReference("Users");
        Query query = reference.orderByChild("uid").equalTo(currentUser.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    uId = dataSnapshot.child("uid").getValue(String.class);
                    uName = dataSnapshot.child("name").getValue(String.class);
                    uEmail = dataSnapshot.child("email").getValue(String.class);
                    uDp = dataSnapshot.child("image").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Check quyền truy cập vào bộ nhớ
    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    // Yêu cầu cấp truyền truy cập vào bộ nhớ
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }
    // Check quyền truy cập vào camera
    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    // Yêu cầu cấp quền truy cập camera
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if (cameraAccepted && writeStorageAccepted) {
                    pickFromCamera();
                } else {
                    Toast.makeText(this, "Vui cấp quyền truy cập camera và bộ nhớ", Toast.LENGTH_SHORT);
                }
            }
        }

        if (requestCode == STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (writeStorageAccepted) {
                    pickFromGallery();
                } else {
                    Toast.makeText(this, "Vui cấp quyền truy cập bộ nhớ", Toast.LENGTH_SHORT);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}