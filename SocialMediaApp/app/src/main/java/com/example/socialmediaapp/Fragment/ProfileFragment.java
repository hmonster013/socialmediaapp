package com.example.socialmediaapp.Fragment;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.Manifest;
import android.widget.Toast;

import com.example.socialmediaapp.Adapter.AdapterPost;
import com.example.socialmediaapp.AddPostActivity;
import com.example.socialmediaapp.Model.ModelPost;
import com.example.socialmediaapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfileFragment extends Fragment {
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    String cameraPermissions[];
    String storagePermissions[];

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseDatabase database;
    FirebaseStorage storage;
    DatabaseReference userReference;
    StorageReference storageReference;

    ImageView imgv_avatar;
    ImageView imgv_cover;
    TextView txv_name;
    TextView txv_email;
    TextView txv_phone;
    FloatingActionButton fab_edit;
    RecyclerView rcv_post;

    ProgressDialog progressDialog;
    Uri image_uri;
    //Phân loại ảnh
    String isCoverorAvatar;
    //Đường dẫn của Image trên firebase storage
    String storagePath = "Users_Profile_Cover_Imgs/";
    ArrayList<ModelPost> arrls_post;
    AdapterPost adapterPost;
    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imgv_avatar = view.findViewById(R.id.imgv_avatar);
        imgv_cover = view.findViewById(R.id.imgv_cover);
        txv_name = view.findViewById(R.id.txv_name);
        txv_email = view.findViewById(R.id.txv_email);
        txv_phone = view.findViewById(R.id.txv_phone);
        fab_edit = view.findViewById(R.id.fab_edit);
        rcv_post = view.findViewById(R.id.rcv_post);

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //Setting firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        userReference = database.getReference("Users");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        arrls_post = new ArrayList<>();
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Đang tải bài viết...");

        //Setting recyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, true);
        layoutManager.setStackFromEnd(true);
        rcv_post.setLayoutManager(layoutManager);

        // Read post
        readPost();

        Query query = userReference.child(currentUser.getUid());
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

        fab_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        // Inflate the layout for this fragment
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.it_addpost) {
            Intent intent = new Intent(getActivity(), AddPostActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    // Check quyền truy cập vào bộ nhớ
    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    // Yêu cầu cấp truyền truy cập vào bộ nhớ
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(getActivity(), storagePermissions, STORAGE_REQUEST_CODE);
    }
    // Check quyền truy cập vào camera
    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    // Yêu cầu cấp quền truy cập camera
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(getActivity(), cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialog() {
        String option[] = {"Ảnh đại diện", "Ảnh bìa", "Tên người dùng", "Số điện thoại"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Chỉnh sửa trang cá nhân");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    progressDialog.setMessage("Cập nhật ảnh đại diện");
                    isCoverorAvatar = "image";
                    showImagePicDialog();
                } else if (which == 1) {
                    progressDialog.setMessage("Cập nhật ảnh bìa");
                    isCoverorAvatar = "cover";
                    showImagePicDialog();
                } else if (which == 2) {
                    progressDialog.setMessage("Cập nhật tên người dùng");
                    //Setup dialog
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());

                    LinearLayout layout = new LinearLayout(getActivity());
                    layout.setOrientation(LinearLayout.VERTICAL);

                    EditText edt_name = new EditText(getActivity());
                    edt_name.setHint("Tên người dùng");

                    int marginValue = 50;
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(marginValue, 20, marginValue, 20);
                    edt_name.setLayoutParams(params);

                    layout.addView(edt_name);

                    builder1.setView(layout);
                    builder1.setTitle("Cập nhật tên người dùng");

                    builder1.setPositiveButton("Lưu", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            progressDialog.show();
                            userReference.child(currentUser.getUid()).child("name").setValue(edt_name.getText().toString())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            // Sau khi cập nhật xong tên người dùng, cập nhật lại tên người dùng ở các Post
                                            DatabaseReference postReference = database.getReference("Post");
                                            postReference.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                        if (dataSnapshot.child("uid").getValue(String.class).equals(currentUser.getUid())) {
                                                            postReference.child(dataSnapshot.getKey()).child("uName").setValue(edt_name.getText().toString());
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                            progressDialog.dismiss();
                                            Toast.makeText(getActivity(), "Cập nhật tên người dùng thành công", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(getActivity(), "Cập nhật tên người dùng thât bại", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });

                    builder1.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder1.create().show();
                } else {
                    progressDialog.setMessage("Cập nhật số điện thoại");
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());

                    LinearLayout layout = new LinearLayout(getActivity());
                    layout.setOrientation(LinearLayout.VERTICAL);

                    EditText edt_phone = new EditText(getActivity());
                    edt_phone.setHint("Số điện thoại");

                    int marginValue = 50;
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(marginValue, 20, marginValue, 20);
                    edt_phone.setLayoutParams(params);

                    layout.addView(edt_phone);

                    builder1.setView(layout);

                    builder1.setTitle("Cập nhật số điện thoại");

                    builder1.setPositiveButton("Lưu", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            progressDialog.show();
                            userReference.child(currentUser.getUid()).child("phone").setValue(edt_phone.getText().toString())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            progressDialog.dismiss();
                                            Toast.makeText(getActivity(), "Cập nhật số điện thoại thành công", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(getActivity(), "Cập nhật số điện thoại thất bại", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });

                    builder1.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder1.create().show();
                }
            }
        });
        builder.create().show();
    }

    private void showImagePicDialog() {
        String option[] = {"Máy ảnh", "Thư viện ảnh"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Chọn nơi tải lên");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }
                } else if (which == 1) {
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();
    }

    //Lỗi không thấy gọi hàm khi đã cấp quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if (cameraAccepted && writeStorageAccepted) {
                    pickFromCamera();
                } else {
                    Toast.makeText(getActivity(), "Vui cấp quyền truy cập camera và bộ nhớ", Toast.LENGTH_SHORT);
                }
            }
        }

        if (requestCode == STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if (writeStorageAccepted) {
                    pickFromGallery();
                } else {
                    Toast.makeText(getActivity(), "Vui cấp quyền truy cập bộ nhớ", Toast.LENGTH_SHORT);
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                uploadProfileCoverPhoto(image_uri);
            }
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                image_uri = data.getData();
                uploadProfileCoverPhoto(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri imageUri) {
        progressDialog.setTitle("Thông báo");
        progressDialog.show();
        //Đường dẫn của image trên firebase storage
        String filePathAndName = storagePath + "" + isCoverorAvatar + "_" + currentUser.getUid();

        StorageReference storageReference1 = storageReference.child(filePathAndName);
        storageReference1.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getStorage().getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri downloadUri) {
                                        HashMap<String, Object> results = new HashMap<>();
                                        results.put(isCoverorAvatar, downloadUri.toString());
                                        //Sau khi cập nhật ảnh đại diện, cập nhật lại ảnh đại diện cho các post khác
                                        if (isCoverorAvatar.equals("image")) {
                                            DatabaseReference postReference = database.getReference("Post");
                                            postReference.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                        if (dataSnapshot.child("uid").getValue(String.class).equals(currentUser.getUid())) {
                                                            postReference.child(dataSnapshot.getKey()).child("uDp").setValue(downloadUri.toString());
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }
                                        userReference.child(currentUser.getUid()).updateChildren(results)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(getActivity(), "Cập nhật ảnh thành công", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(getActivity(), "Cập nhật ảnh thất bại", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getActivity(), "Lỗi khi lấy đường dẫn tải về", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
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
                    if (dataSnapshot.child("uid").getValue(String.class).equals(currentUser.getUid().toString())) {
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
                adapterPost = new AdapterPost(getActivity() ,arrls_post);
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
    private void searchPost(String query) {
        DatabaseReference reference = database.getReference("Post");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrls_post.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child("uid").getValue(String.class).equals(currentUser.getUid().toString())) {
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
}