package com.example.bookapp.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookapp.databinding.ActivityPdfAddBinding;
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

import java.util.ArrayList;
import java.util.HashMap;

/** @noinspection deprecation*/
public class PdfAddActivity extends AppCompatActivity {

    private ActivityPdfAddBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;
    private Uri pdfUri = null;
    private static final int PDF_PICK_CODE = 2000;
    private static final String TAG = "ADD_PDF_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        loadPdfCategories();

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

        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfPickIntent();
            }
        });

        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryPickDialog();
            }
        });


        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

    }


    private String title="", description="";
    private void validateData() {
        Log.d(TAG, "validateData: đang kiểm tra dữ liệu...");


        //get data
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();


        //validate data
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Nhập tên...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Nhập mô tả...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(selectedCategoryTitle)) {
            Toast.makeText(this, "Chọn thể loại...", Toast.LENGTH_SHORT).show();
        } else if (pdfUri == null) {
            Toast.makeText(this, "Chọn pdf...", Toast.LENGTH_SHORT).show();
        } else {
            upLoadPdfToStorage();
        }
    }

    private void upLoadPdfToStorage() {
        Log.d(TAG, "upLoadPdfToStorage: Đang tải lên...");

        progressDialog.setMessage("Đang tải lên...");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();

        //đường dẫn pdf trong firebase storage
        String filePathAndName = "Books/" + timestamp;

        //storage reference
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: Đang tải PDF lên kho lưu trữ...");
                        Log.d(TAG, "onSuccess: Lấy pdf url");

                        //lấy đường dẫn pdf
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedPdfUrl = ""+uriTask.getResult();

                        //upload đến firebase db
                        uploadPdfInfoToDb(uploadedPdfUrl, timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: tải lên PDF thất bại "+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "tải lên PDF thất bại "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadPdfInfoToDb(String uploadedPdfUrl, long timestamp) {
        Log.d(TAG, "upLoadPdfToStorage: Đang tải lên...");

        progressDialog.setMessage("Đang tải lên...");

        String uid = firebaseAuth.getUid();

        //đẩy dữ liệu upload, đếm số lượng xem, đếm số lượng download khi thêm pdf/book
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", ""+uid);
        hashMap.put("id", ""+timestamp);
        hashMap.put("title", ""+title);
        hashMap.put("description", ""+description);
        hashMap.put("categoryId", ""+selectedCategoryId);
        hashMap.put("url", ""+uploadedPdfUrl);
        hashMap.put("timestamp", timestamp);
        hashMap.put("viewsCount", 0);
        hashMap.put("downloadsCount", 0);

        //Db > book
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onSuccess: Tải lên thành công...");
                        Toast.makeText(PdfAddActivity.this, "tải lên thành công...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: Tải lên thất bại "+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "tải lên thất bại "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Đang tải...");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        //db > categories
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();
                categoryIdArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    //lay id va the loai tu categories
                    String categoryId = ""+ds.child("id").getValue();
                    String categoryTitle = ""+ds.child("category").getValue();

                    //them vao mang
                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String selectedCategoryId, selectedCategoryTitle;
    private void categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: Hiện để chọn");

        //lấy kí tự từ categories trong mảng
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i=0; i<categoryTitleArrayList.size(); i++) {
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        //hộp thoại thông báo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn Thể Loại")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //lấy item đã chọn từ list
                        selectedCategoryTitle = categoryTitleArrayList.get(which);
                        selectedCategoryId = categoryIdArrayList.get(which);

                        binding.categoryTv.setText(selectedCategoryTitle);

                        Log.d(TAG, "onClick: Chọn Category: "+selectedCategoryId+""+selectedCategoryTitle);
                    }
                })
                .show();
    }

    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn Pdf"), PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PDF_PICK_CODE) {
                Log.d(TAG, "onActivityResult: Đã chọn PDF");

                pdfUri = data.getData();

                Log.d(TAG, "onActivityResult: URI: "+pdfUri);
            }
        } else {
            Log.d(TAG, "onActivityResult: Hủy chọn pdf");
            Toast.makeText(this, "Hủy chọn pdf", Toast.LENGTH_SHORT).show();
        }
    }
}