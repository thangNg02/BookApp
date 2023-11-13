package com.example.bookapp.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.bookapp.R;
import com.example.bookapp.databinding.ActivityProfileEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

/** @noinspection ALL*/
public class ProfileEditActivity extends AppCompatActivity {

    private ActivityProfileEditBinding binding;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "PROFILE_EDIT_TAG";
    private Uri imageUri = null;
    private String name = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //setup firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();
        loadUserInfo();

        //dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Vui lòng chờ!");
        progressDialog.setCanceledOnTouchOutside(false);

        //quay lại
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //chọn ảnh
        binding.profileTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageAttachMenu();
            }
        });

        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private void validateData() {
        //get data
        name = binding.nameEt.getText().toString().trim();

        //validate
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Nhập tên...", Toast.LENGTH_SHORT).show();
        } else {
            if (imageUri == null) {
                updateProfile("");
            } else {
                uploadImage();
            }
        }
    }

    private void uploadImage() {
        progressDialog.setMessage("Đang tải...");
        progressDialog.show();

        String filePathAndName = "ProfileImages/"+firebaseAuth.getUid();

        StorageReference reference = FirebaseStorage.getInstance().getReference(filePathAndName);
        reference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadImageUrl = ""+uriTask.getResult();

                        updateProfile(uploadImageUrl);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Tải lên thất bại "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateProfile(String imageUrl) {
        progressDialog.setMessage("Đang tải...");
        progressDialog.show();

        //setup data đã update vào db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", ""+name);
        if (imageUri != null) {
            hashMap.put("profileImage", ""+imageUrl);
        }

        //update data vào db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Cập nhật thành công...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditActivity.this, "Cập nhật thất bại "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showImageAttachMenu() {
        PopupMenu popupMenu = new PopupMenu(this, binding.profileTv);
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Gallery");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int which = item.getItemId();
                if (which == 0) {
                    pickImageCamera();
                } else if (which == 1) {
                    pickImageGallery();
                }

                return false;
            }
        });
    }

    private void pickImageGallery() {
        //chọn ảnh từ camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Chọn mới");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Mô tả ảnh");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
         cameraActivityResultLauncher.launch(intent);

    }

    private void pickImageCamera() {
        //chọn ảnh từ thư viện
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //get uri của ảnh
                    if (result.getResultCode() == Activity.RESULT_OK ) {
                        Intent data = result.getData(); //ko cần máy ảnh mà chỉ cần ảnh trong imageuri variable

                        binding.profileTv.setImageURI(imageUri);
                    } else {
                        Toast.makeText(ProfileEditActivity.this, "Hủy", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    //get uri của ảnh
                    if (result.getResultCode() == Activity.RESULT_OK ) {
                        Intent data = result.getData();
                        imageUri = data.getData();

                        binding.profileTv.setImageURI(imageUri);
                    } else {
                        Toast.makeText(ProfileEditActivity.this, "Hủy", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );


    private void loadUserInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //lấy toàn bộ dữ liệu của user từ snapshot
                        String email = ""+snapshot.child("email").getValue();
                        String name = ""+snapshot.child("name").getValue();
                        String profileImage = ""+snapshot.child("profileImage").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();
                        String userType = ""+snapshot.child("userType").getValue();


                        //lấy dữ liệu để hiện thị
                        binding.nameEt.setText(name);

                        //set ảnh
                        Glide.with(ProfileEditActivity.this)
                                .load(profileImage)
                                .placeholder(R.drawable.ic_person_gray)
                                .into(binding.profileTv);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}