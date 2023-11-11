package com.example.socialmediaapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaapp.Model.ModelComment;
import com.example.socialmediaapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterComment extends RecyclerView.Adapter<AdapterComment.CommentHolder> {
    ArrayList<ModelComment> arrlsComments;
    Context context;
    String myUid;
    String postId;
    int pComments;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    public AdapterComment() {

    }
    public AdapterComment(Context context,ArrayList<ModelComment> arrlsComments, String myUid, String postId) {
        this.context = context;
        this.arrlsComments = arrlsComments;
        this.myUid = myUid;
        this.postId = postId;
    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_comment, parent, false);
        return new CommentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentHolder holder, int position) {
        ModelComment modelComment = arrlsComments.get(position);

        try {
            Picasso.get().load(modelComment.getuDp()).into(holder.civ_uAvatar);
        } catch (Exception e) {
            holder.civ_uAvatar.setImageResource(R.drawable.ic_default_avatar);
        }
        holder.txv_uName.setText(modelComment.getuName());
        holder.txv_comment.setText(modelComment.getComment());

        //Định dạng kiểu dữ liệu thời gian
        Date date = new Date(Long.parseLong(modelComment.getTimestamp()));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm aa", Locale.getDefault());
        String formattedDate = simpleDateFormat.format(date);

        holder.txv_time.setText(formattedDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myUid.equals(modelComment.getUid())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder((v.getRootView().getContext()));
                    builder.setTitle("Thông báo");
                    builder.setMessage("Bạn có muốn xóa bình luận này không?");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DatabaseReference reference = database.getReference("Post").child(postId);
                            reference.child("Comments").child(modelComment.getcId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    reference.child("pComments").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            pComments = snapshot.getValue(Integer.class);
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                    reference.child("pComments").setValue(pComments - 1);
                                    Toast.makeText(context, "Bạn đã xóa bình luận", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Xóa bình luận không thành công", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (arrlsComments.isEmpty()) {
            return 0;
        }
        return arrlsComments.size();
    }

    public static class CommentHolder extends RecyclerView.ViewHolder {
        CircleImageView civ_uAvatar;
        TextView txv_uName;
        TextView txv_comment;
        TextView txv_time;
        public CommentHolder(View view) {
            super(view);
            civ_uAvatar = view.findViewById(R.id.civ_uAvatar);
            txv_uName = view.findViewById(R.id.txv_uName);
            txv_comment = view.findViewById(R.id.txv_comment);
            txv_time = view.findViewById(R.id.txv_time);
        }
    }
}
