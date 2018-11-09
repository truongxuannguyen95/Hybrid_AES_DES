package com.example.nguyen.hybrid_aes_des.adapter;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.content.Context;
import android.widget.TextView;

import com.example.nguyen.hybrid_aes_des.R;
import com.example.nguyen.hybrid_aes_des.Utilities;
import com.example.nguyen.hybrid_aes_des.model.Keys;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.ArrayList;

public class ListViewKeys extends BaseAdapter {

    private Context myContext;
    private ArrayList<Keys> listKeys;

    public ListViewKeys(Context myContext, ArrayList<Keys> listKeys) {
        this.myContext = myContext;
        this.listKeys = listKeys;
    }

    @Override
    public int getCount() {
        return listKeys.size();
    }

    @Override
    public Object getItem(int i) {
        return listKeys.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rowview = view;
        if(rowview == null)
        {
            LayoutInflater inflater = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowview = inflater.inflate(R.layout.row_keys, null);
        }
        TextView tvFileName = rowview.findViewById(R.id.tvFileName);
        final String key = listKeys.get(i).getKey();
        tvFileName.setText(key);
        Button btnDelete = rowview.findViewById(R.id.btnDelete);
        final int position = i;

        tvFileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.showAlertDialog("Key", key, myContext);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(myContext);
                builder.setTitle("Bạn có chắc muốn xóa key");
                builder.setMessage(listKeys.get(position).getKey());
                builder.setCancelable(false);
                builder.setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(Utilities.isOnline(myContext)) {
                            FirebaseAuth mAuth = FirebaseAuth.getInstance();
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            final String owner = currentUser.getUid();
                            final DatabaseReference mData = FirebaseDatabase.getInstance().getReference();
                            mData.child("users").child(owner).child(listKeys.get(position).getChild()).removeValue();
                            listKeys.remove(position);
                            dialogInterface.dismiss();
                            notifyDataSetChanged();
                        } else {
                            Utilities.showAlertDialog("Xóa thất bại", "Thiết bị của bạn chưa được kết nối internet", myContext);
                        }
                    }
                });
                builder.setNegativeButton("Hủy bỏ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
                    }
                });
                alertDialog.show();
            }
        });
        return rowview;
    }
}