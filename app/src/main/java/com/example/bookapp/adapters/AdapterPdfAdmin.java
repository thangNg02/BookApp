package com.example.bookapp.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.MyApplication;
import com.example.bookapp.activities.PdfDetailActivity;
import com.example.bookapp.activities.PdfEditActivity;
import com.example.bookapp.databinding.RowPdfAdminBinding;
import com.example.bookapp.filters.FilterPdfAdmin;
import com.example.bookapp.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

/** @noinspection ALL */
public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {
    //context
    private Context context;
    //
    public ArrayList<ModelPdf> pdfArrayList, filterList;
    //view binding row_pdf_admin.xml
    private RowPdfAdminBinding binding;
    private FilterPdfAdmin filter;
    private ProgressDialog progressDialog;
    private static final String TAG = "PDF_ADAPTER_TAG";
    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList = pdfArrayList;

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Vui lòng chờ...");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPdfAdmin.HolderPdfAdmin holder, int position) {
        //lấy dữ liệu
        ModelPdf model = pdfArrayList.get(position);
        String pdfId = model.getId();
        String categoryId = model.getCategoryId();
        String title = model.getTitle();
        String description = model.getDescription();
        String pdfUrl = model.getUrl();
        long timestamp = model.getTimestamp();

        //đổi sang dạng dd/MM/yyyy
        String formattedDate = MyApplication.formatTimestamp(timestamp);

        //set dữ liệu
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(formattedDate);

        MyApplication.loadCategory(
                ""+categoryId,
                holder.categoryTv
        );
//        loadCategory(model, holder);

        MyApplication.loadPdfFromUrlSinglePage(
                ""+pdfUrl,
                ""+title,
                holder.pdfView,
                holder.progressBar
        );
//        loadPdfFromUrl(model, holder);

        MyApplication.loadPdfSize(
                ""+pdfUrl,
                ""+title,
                holder.sizeTv
        );
//        loadPdfSize(model, holder);

        //more click
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionsDialog(model, holder);
            }
        });

        //chi tiết thông tin click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", pdfId);
                context.startActivity(intent);
            }
        });
    }

    private void moreOptionsDialog(ModelPdf model, HolderPdfAdmin holder) {
        String bookId = model.getId();
        String bookUrl = model.getUrl();
        String bookTitle = model.getTitle();

        String[] options = {"Chỉnh sửa", "Xóa"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Lựa chọn")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //lựa chọn
                        if (which == 0) {
                            //chỉnh sửa
                            Intent intent = new Intent(context, PdfEditActivity.class);
                            intent.putExtra("bookId", bookId);
                            context.startActivity(intent);
                        } else if (which == 1) {
                            //xóa
                            MyApplication.deleteBook(
                                    context,
                                    ""+bookId,
                                    ""+bookUrl,
                                    ""+bookTitle);
//                            deleteBook(model, holder);
                        }
                    }
                })
                .show();
    }

//    private void deleteBook(ModelPdf model, HolderPdfAdmin holder) {
//        String bookId = model.getId();
//        String bookUrl = model.getUrl();
//        String bookTitle = model.getTitle();
//
//        progressDialog.setMessage("Đang xóa "+bookTitle+" ...");
//        progressDialog.show();
//
//        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
//        ref.delete()
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
//                        reference.child(bookId)
//                                .removeValue()
//                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(Void unused) {
//                                        progressDialog.dismiss();
//                                        Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show();
//                                    }
//                                })
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        progressDialog.dismiss();
//                                        Toast.makeText(context, "Xóa thất bại", Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        progressDialog.dismiss();
//                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//    }

//    private void loadPdfSize(ModelPdf model, HolderPdfAdmin holder) {
//        //sử dụng url để lấy file
//
//        String pdfUrl = model.getUrl();
//
//        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
//        ref.getMetadata()
//                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
//                    @Override
//                    public void onSuccess(StorageMetadata storageMetadata) {
//                        //set size in byte
//                        double bytes = storageMetadata.getSizeBytes();
//
//
//                        //chuyển đổi byte to KB, MB
//                        double kb = bytes/1024;
//                        double mb = kb/1024;
//
//                        if (mb >= 1) {
//                            holder.sizeTv.setText(String.format("%.2f", mb)+" MB");
//                        } else if (kb >= 1){
//                            holder.sizeTv.setText(String.format("%.2f", kb)+" KB");
//                        } else {
//                            holder.sizeTv.setText(String.format("%.2f", bytes)+" bytes");
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        //lấy dữ liệu thất bại
//                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

//    private void loadPdfFromUrl(ModelPdf model, HolderPdfAdmin holder) {
//        String pdfUrl = model.getUrl();
//        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
//        ref.getBytes(MAX_BYTES_PDF)
//                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
//                    @Override
//                    public void onSuccess(byte[] bytes) {
//                        //set to pdf view
//                        holder.pdfView.fromBytes(bytes)
//                                .pages(0) //chỉ có ở trang đầu
//                                .spacing(0)
//                                .swipeHorizontal(false)
//                                .enableSwipe(false)
//                                .onError(new OnErrorListener() {
//                                    @Override
//                                    public void onError(Throwable t) {
//                                        holder.progressBar.setVisibility(View.INVISIBLE);
//                                    }
//                                })
//                                .onPageError(new OnPageErrorListener() {
//                                    @Override
//                                    public void onPageError(int page, Throwable t) {
//                                        holder.progressBar.setVisibility(View.INVISIBLE);
//                                    }
//                                })
//                                .onLoad(new OnLoadCompleteListener() {
//                                    @Override
//                                    public void loadComplete(int nbPages) {
//                                        holder.progressBar.setVisibility(View.INVISIBLE);
//                                    }
//                                })
//                                .load();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        holder.progressBar.setVisibility(View.INVISIBLE);
//                    }
//                });
//    }

//    private void loadCategory(ModelPdf model, HolderPdfAdmin holder) {
//        String categoryId = model.getCategoryId();
//
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
//        ref.child(categoryId)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        //get category
//                        String category = ""+snapshot.child("category").getValue();
//
//                        //set category
//                        holder.categoryTv.setText(category);
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterPdfAdmin(filterList, this);
        }
        return filter;
    }

    //view holder class cho row_pdf_admin.xml
    class HolderPdfAdmin extends RecyclerView.ViewHolder {

        //giao diện Views của row_pdf_admin.xml
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv, sizeTv, dateTv;
        ImageButton moreBtn;
        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);

            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            sizeTv = binding.sizeTv;
            dateTv = binding.dateTv;
            moreBtn = binding.moreBtn;
        }
    }
}
