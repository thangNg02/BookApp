package com.example.bookapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.databinding.ActivityCategoryAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

/** @noinspection ALL*/
public class CategoryAddActivity extends AppCompatActivity {

    //view Binding
    private ActivityCategoryAddBinding binding;

    //Firebase Auth
    private FirebaseAuth firebaseAuth;

    //hộp thoại thông báo
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //firebase khai báo
        firebaseAuth = FirebaseAuth.getInstance();

        //setup hộp thoại thông báo
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Vui lòng chờ!");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private String category="";
    private void validateData() {
        //Lấy dữ liệu
        category = binding.categoryEt.getText().toString().trim();

        if (TextUtils.isEmpty(category)) {
            Toast.makeText(this, "Nhập tên trường...", Toast.LENGTH_SHORT).show();
        } else {
            addCategoryFirebase();
        }
    }

    private void addCategoryFirebase() {
        //gọi ra hộp thoại thông báo
        progressDialog.setMessage("Đang thêm...");
        progressDialog.show();

        //Lấy thời gian
        long timestamp = System.currentTimeMillis();

        //set up dữ liệu vào db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", ""+timestamp);
        hashMap.put("category", ""+category);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", ""+firebaseAuth.getUid());

        //thêm vào db Batabase Root > Categories > categoryId > category info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //dữ liệu thêm vào db thành công
                        progressDialog.dismiss();
                        Toast.makeText(CategoryAddActivity.this, "Thêm thành công...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //dữ liệu thêm vào db ko thành công
                        progressDialog.dismiss();
                        Toast.makeText(CategoryAddActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}