package com.example.bookapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.bookapp.BookUserFragment;
import com.example.bookapp.databinding.ActivityDashboardUserBinding;
import com.example.bookapp.models.ModelCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/** @noinspection ALL*/
public class DashboardUserActivity extends AppCompatActivity {

    private ActivityDashboardUserBinding binding;
    private FirebaseAuth firebaseAuth;
    public ArrayList<ModelCategory> categoryArrayList;
    public ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //firebase khai báo
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        setupViewPagerAdapter(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                checkUser();
            }
        });

        binding.profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardUserActivity.this, ProfileActivity.class));
            }
        });
    }

    private void setupViewPagerAdapter(ViewPager viewPager) {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, this);

        categoryArrayList = new ArrayList<>();

        //load categories from firebase
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryArrayList.clear();

                //thêm dữ liệu vào models
                ModelCategory modelAll = new ModelCategory("01", "Tất cả", "", 1);
                ModelCategory modelMostViewed = new ModelCategory("02", "Nhiều lượt truy cập nhất", "", 1);
                ModelCategory modelMostDownloaded = new ModelCategory("03", "Nhiều lượt tải xuống nhất", "", 1);

                //thêm models vao list
                categoryArrayList.add(modelAll);
                categoryArrayList.add(modelMostViewed);
                categoryArrayList.add(modelMostDownloaded);

                //thêm dữ liệu vào view pager adapter
                viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                        ""+modelAll.getId(),
                        ""+modelAll.getCategory(),
                        ""+modelAll.getUid()
                ), modelAll.getCategory());
                viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                        ""+modelMostViewed.getId(),
                        ""+modelMostViewed.getCategory(),
                        ""+modelMostViewed.getUid()
                ), modelMostViewed.getCategory());
                viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                        ""+modelMostDownloaded.getId(),
                        ""+modelMostDownloaded.getCategory(),
                        ""+modelMostDownloaded.getUid()
                ), modelMostDownloaded.getCategory());

                //refresh list
                viewPagerAdapter.notifyDataSetChanged();

                //load tu database
                for (DataSnapshot ds: snapshot.getChildren()) {
                    //get du lieu
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    //them du lieu vao list
                    categoryArrayList.add(model);
                    //them du lieu vao viewPagerAdapter
                    viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                            ""+model.getId(),
                            ""+model.getCategory(),
                            ""+model.getUid()), model.getCategory());

                    //refresh list
                    viewPagerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //set adapter to view pager
        viewPager.setAdapter(viewPagerAdapter);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<BookUserFragment> fragmentList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private Context context;
        public ViewPagerAdapter(FragmentManager fm, int behavior, Context context) {
            super(fm, behavior);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        private void addFragment(BookUserFragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }
    private void checkUser() {
        //Kiểm tra có user đang đăng nhập ko
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            //ko có user đăng nhập
            startActivity(new Intent(DashboardUserActivity.this, MainActivity.class));
            finish();
        } else {
            String email = firebaseUser.getEmail();

            binding.subTitleTv.setText(email);
        }
    }

}