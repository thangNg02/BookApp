package com.example.bookapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

/** @noinspection ALL*/
public class RegisterActivity extends AppCompatActivity {

    //view Binding
    private ActivityRegisterBinding binding;

    //Firebase Auth
    private FirebaseAuth firebaseAuth;

    //hộp thoại thông báo
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
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

        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

    }

    private String name="", email="", password="", cPassword="";
    private void validateData() {

        //Lấy dữ liệu
        name = binding.nameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();
        cPassword = binding.cPasswordEt.getText().toString().trim();

        //Xác nhận thông tin
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Nhập tên...", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Mẫu email không hợp lệ!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Nhập mật khẩu...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(cPassword)) {
            Toast.makeText(this, "Xác nhận mật khẩu...", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(cPassword)) {
            Toast.makeText(this, "Mật khẩu không trùng khớp!", Toast.LENGTH_SHORT).show();
        } else {
            createUserAccount();
        }
    }

    private void createUserAccount() {
        //gọi ra hộp thoại thông báo
        progressDialog.setMessage("Đang tạo tài khoản...");
        progressDialog.show();

        //tạo user trên firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //tạo tài khoản thành công, thêm vào realtime database
                        updateUserInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //tạo tài khoản thất bại
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void updateUserInfo() {
        progressDialog.setMessage("Đang lưu thông tin tài khoản!");

        //Lấy thời gian
        long timestamp = System.currentTimeMillis();

        //Lấy uid của user hiện tại
        String uid = firebaseAuth.getUid();

        //set up dữ liệu vào db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("email", email);
        hashMap.put("name", name);
        hashMap.put("profileImage", "");
        hashMap.put("userType", "user");
        hashMap.put("timestamp", timestamp);

        //set dữ liệu vào db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //dữ liệu thêm vào db thành công
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Tạo thành công...", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(RegisterActivity.this, DashboardUserActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //dữ liệu thêm vào db ko thành công
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}