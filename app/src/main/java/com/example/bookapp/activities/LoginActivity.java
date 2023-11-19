package com.example.bookapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/** @noinspection ALL*/
public class LoginActivity extends AppCompatActivity {

    //view Binding
    private ActivityLoginBinding binding;

    //Firebase Auth
    private FirebaseAuth firebaseAuth;

    //hộp thoại thông báo
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //firebase khai báo
        firebaseAuth = FirebaseAuth.getInstance();

        //setup hộp thoại thông báo
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Vui lòng chờ!");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

    }

    private String email="", password="";
    private void validateData() {

        //Lấy dữ liệu
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();

        //Xác nhận thông tin
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Mẫu email không hợp lệ!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Nhập mật khẩu...", Toast.LENGTH_SHORT).show();
        } else {
            loginUser();
        }
    }

    private void loginUser() {
        progressDialog.setMessage("Đang đăng nhập...");
        progressDialog.show();

        //login user
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //Đăng nhập thành công
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Đăng nhập thất bại
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void checkUser() {
        progressDialog.setMessage("Đang kiểm tra user...");

        //kiểm tra xem user đang là admin hay user
        //lấy user hiện tại
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        //Kiểm tra trong db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String currentUserId = firebaseAuth.getCurrentUser().getUid();
                        DatabaseReference currentUserRef = ref.child(currentUserId);

                        currentUserRef.child("status").setValue("Online");

                        progressDialog.dismiss();
                        //lấy kiểu user
                        String userType = ""+snapshot.child("userType").getValue();
                        //KIểm tra user type
                        if (userType.equals("user")) {
                            startActivity(new Intent(LoginActivity.this, DashboardUserActivity.class));
                            finish();
                            finish();
                        } else if (userType.equals("admin")) {
                            startActivity(new Intent(LoginActivity.this, DashboardAdminActivity.class));
                            finish();
                            finish();
                        } else if (userType.equals("adminAccounts")) {
                            startActivity(new Intent(LoginActivity.this, DashboardAdminAccountsActivity.class));
                            finish();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {

                    }
                });
    }
}