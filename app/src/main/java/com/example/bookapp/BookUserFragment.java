package com.example.bookapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.bookapp.adapters.AdapterPdfUser;
import com.example.bookapp.databinding.FragmentBookUserBinding;
import com.example.bookapp.models.ModelPdf;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BookUserFragment extends Fragment {

    private String categoryId;
    private String category;
    private String uid;
    private ArrayList<ModelPdf> pdfArrayList;
    private AdapterPdfUser adapterPdfUser;
    private FragmentBookUserBinding binding;
    private static final String TAG = "BOOKS_USER_TAG";
    public static BookUserFragment newInstance(String categoryId, String category, String uid) {
        BookUserFragment fragment = new BookUserFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", categoryId);
        args.putString("category", category);
        args.putString("uid", uid);

        fragment.setArguments(args);
        return fragment;
    }

    public BookUserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
            category = getArguments().getString("category");
            uid = getArguments().getString("uid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBookUserBinding.inflate(LayoutInflater.from(getContext()), container, false);

        if (category.equals("Tất cả")) {
            loadAllBooks();
        } else if (category.equals("Nhiều lượt truy cập nhất")) {
            loadMostViewedDownLoadedBooks("viewsCount");
        } else if (category.equals("Nhiều lượt tải xuống nhất")) {
            loadMostViewedDownLoadedBooks("downloadsCount");
        } else {
            loadCategorizedBooks();
        }

        //search
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterPdfUser.getFilter().filter(s);
                }
                catch (Exception e) {

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return binding.getRoot();
    }

    private void loadCategorizedBooks() {
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            //get data
                            ModelPdf model = ds.getValue(ModelPdf.class);
                            //add to list
                            pdfArrayList.add(model);
                        }
                        //setup adapter
                        adapterPdfUser = new AdapterPdfUser(getContext(), pdfArrayList);
                        //set adapter to recycle view
                        binding.bookRv.setAdapter(adapterPdfUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadMostViewedDownLoadedBooks(String orderBy) {
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild(orderBy).limitToLast(10) //load 10 nhieu view va download nhat
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    //get data
                    ModelPdf model = ds.getValue(ModelPdf.class);
                    //add to list
                    pdfArrayList.add(model);
                }
                //setup adapter
                adapterPdfUser = new AdapterPdfUser(getContext(), pdfArrayList);
                //set adapter to recycle view
                binding.bookRv.setAdapter(adapterPdfUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadAllBooks() {
        pdfArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    //get data
                    ModelPdf model = ds.getValue(ModelPdf.class);
                    //add to list
                    pdfArrayList.add(model);
                }
                //setup adapter
                adapterPdfUser = new AdapterPdfUser(getContext(), pdfArrayList);
                //set adapter to recycle view
                binding.bookRv.setAdapter(adapterPdfUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}