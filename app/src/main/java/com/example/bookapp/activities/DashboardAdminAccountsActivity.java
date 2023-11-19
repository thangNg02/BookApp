package com.example.bookapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.MyApplication;
import com.example.bookapp.adapters.AdapterAccounts;
import com.example.bookapp.databinding.ActivityDashboardAdminAccountsBinding;
import com.example.bookapp.models.ModelAccounts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardAdminAccountsActivity extends AppCompatActivity {

    private ActivityDashboardAdminAccountsBinding binding;
    private FirebaseAuth firebaseAuth;

    //mảng lưu trữ categories
    private ArrayList<ModelAccounts> accountsArrayList;

    private TextView nameAccountTv, statusAccountTv;
    //adapter
    private AdapterAccounts adapterAccounts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardAdminAccountsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //firebase khai báo
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        loadCategories();

        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardAdminAccountsActivity.this, ProfileActivity.class));
            }
        });
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.updateStatus();
                firebaseAuth.signOut();
                // Khi người dùng đăng xuất
                checkUser();

            }
        });

    }



    private void loadCategories() {
        accountsArrayList = new ArrayList<>();
        //lấy tất cả users từ firebase > Users
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //dọn mảng trước khi thêm dữ liệu
                accountsArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    //lấy dữ liệu
                    ModelAccounts model = ds.getValue(ModelAccounts.class);

                    //Thêm vào mảng
                    accountsArrayList.add(model);
                }
                //setup adapter
                adapterAccounts = new AdapterAccounts(DashboardAdminAccountsActivity.this, accountsArrayList);
                //truyền adapter vào RecycleView
                binding.userAccountsRv.setAdapter(adapterAccounts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUser() {
        //Kiểm tra có user đang đăng nhập ko
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            //ko có user đăng nhập
            startActivity(new Intent(DashboardAdminAccountsActivity.this, MainActivity.class));
            finish();
        } else {
            String email = firebaseUser.getEmail();

            binding.subTitleTv.setText(email);
        }
    }
}