package com.example.bookapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.databinding.RowAccountsStatusBinding;
import com.example.bookapp.filters.FilterCategory;
import com.example.bookapp.models.ModelAccounts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterAccounts extends RecyclerView.Adapter<AdapterAccounts.HolderCategory> {

    private Context context;
    private FirebaseAuth firebaseAuth;
    public ArrayList<ModelAccounts> accountsArrayList, filterList;

    //view bnding
    private RowAccountsStatusBinding binding;

    private FilterCategory filter;
    public AdapterAccounts(Context context, ArrayList<ModelAccounts> accountsArrayList) {
        this.context = context;
        this.accountsArrayList = accountsArrayList;
        this.filterList = accountsArrayList;
    }

    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowAccountsStatusBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderCategory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCategory holder, int position) {
        //lấy dữ liệu
        ModelAccounts model = accountsArrayList.get(position);
        String email = model.getEmail();
        String name = model.getName();
        String profileImage = model.getProfileImage();
        String uid = model.getUid();
        String userType = model.getUserType();
        String status = model.getStatus();
        long timestamp = model.getTimestamp();

        //set dữ liệu
        holder.nameAccountTv.setText(name);


        //hiện trang thái ng dùng
        if (status.equals("Online")) {
            holder.nameAccountTv.setTextColor(Color.parseColor("#00FF00"));
            holder.statusAccountIb.setColorFilter(Color.parseColor("#00FF00"));
        } else {
            holder.nameAccountTv.setTextColor(Color.parseColor("#FF0000"));
            holder.statusAccountIb.setColorFilter(Color.parseColor("#FF0000"));
        }

        firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        String currentUserId = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference currentUserRef = ref.child(currentUserId);

        currentUserRef.child("loginTime").setValue(System.currentTimeMillis());
        currentUserRef.child("loginTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long loginTime = dataSnapshot.getValue(Long.class);

                // Tính toán thời gian đã trôi qua từ khi đăng nhập
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - loginTime;

                holder.statusAccountTv.setText(formatElapsedTime(elapsedTime));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public String formatElapsedTime(long elapsedTime) {
        long hours = elapsedTime / 3600000; // 1 giờ = 3600000 miligiây
        long minutes = (elapsedTime % 3600000) / 60000; // 1 phút = 60000 miligiây
        long seconds = ((elapsedTime % 3600000) % 60000) / 1000; // 1 giây = 1000 miligiây

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }





//        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //Hộp thoại xác nhận xóa
//                AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                builder.setTitle("Xóa")
//                        .setMessage("Bạn có chắc chắn muốn xóa trường này không?")
//                        .setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                //bắt đầu xóa
//                                Toast.makeText(context, "Đang xóa...", Toast.LENGTH_SHORT).show();
//                                deleteCategory(model, holder);
//                            }
//                        })
//                        .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        })
//                        .show();
//            }
//        });

        //khi click vào item, chuyển đến PdfListAdminActivity và chuyền đến pdf category và categoryId
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(context, PdfListAdminActivity.class);
//                intent.putExtra("categoryId", id);
//                intent.putExtra("categoryTitle", category);
//                context.startActivity(intent);
//            }
//        });
//    }


//    private void deleteCategory(ModelCategory model, HolderCategory holder) {
//        //lấy id của category để xóa
//        String id = model.getId();
//        //Firebase DB > Categories > categoryId
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
//        ref.child(id)
//                .removeValue()
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//                        //xóa thành công
//                        Toast.makeText(context, "Xóa thành công...", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        //xóa thất bại
//                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }


//    @Override
//    public Filter getFilter() {
//        if (filter == null) {
//            filter = new FilterCategory(filterList, this);
//        }
//        return filter;
//    }

    @Override
    public int getItemCount() {
        return accountsArrayList.size();
    }


    class HolderCategory extends RecyclerView.ViewHolder {
        TextView nameAccountTv, statusAccountTv, timeUsedTv;
        ImageButton timeUsedIb, statusAccountIb;
        public HolderCategory(@NonNull View itemView) {
            super(itemView);

            nameAccountTv = binding.nameAccountTv;
            statusAccountTv = binding.statusAccountTv;
            timeUsedIb = binding.timeUsedIb;
            statusAccountIb = binding.statusAccountIb;
        }
    }
}
