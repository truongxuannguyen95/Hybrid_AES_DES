package com.example.nguyen.hybrid_aes_des.activity;

import android.app.Dialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.nguyen.hybrid_aes_des.R;
import com.example.nguyen.hybrid_aes_des.Utilities;
import com.example.nguyen.hybrid_aes_des.adapter.ListViewKeys;
import com.example.nguyen.hybrid_aes_des.model.Hybrid_AES_DES;
import com.example.nguyen.hybrid_aes_des.model.Keys;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FragmentListKeys extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public FragmentListKeys() {
    }

    public static FragmentListKeys newInstance(String param1, String param2) {
        FragmentListKeys fragment = new FragmentListKeys();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private TextView tvEmpty;
    private ListView lvKeys;
    private CheckBox ckbShowKey;
    private DatabaseReference mData;
    private ArrayList<Keys> listKeys, listKeysEncrypt;
    private ListViewKeys listViewKeys;
    private Dialog dialog;
    public static boolean allowDelete = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_keys, container, false);
        mData = FirebaseDatabase.getInstance().getReference();
        ckbShowKey = view.findViewById(R.id.ckbShowKey);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        lvKeys = view.findViewById(R.id.lvKeys);
        listKeys = new ArrayList<>();
        listKeysEncrypt = new ArrayList<>();
        listViewKeys = new ListViewKeys(getContext(), listKeysEncrypt);
        lvKeys.setAdapter(listViewKeys);
        getListKeys();

        ckbShowKey.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isPressed()) {
                    if (ckbShowKey.isChecked()) {
                        showDialog();
                    } else {
                        listViewKeys = new ListViewKeys(getContext(), listKeysEncrypt);
                        lvKeys.setAdapter(listViewKeys);
                        listViewKeys.notifyDataSetChanged();
                        allowDelete = false;
                    }
                } else {
                    if(ckbShowKey.isChecked()) {
                        listViewKeys = new ListViewKeys(getContext(), listKeys);
                        lvKeys.setAdapter(listViewKeys);
                        listViewKeys.notifyDataSetChanged();
                        allowDelete = true;
                    }
                }
            }
        });
        return view;
    }

    private void getListKeys() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        final String owner = currentUser.getUid();
        mData.child("users").child(owner).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listKeysEncrypt.clear();
                listKeys.clear();
                HomePage.listKeys.clear();
                if (dataSnapshot.exists()) {
                    tvEmpty.setText("Chưa có key nào");
                    tvEmpty.setTextColor(Color.RED);
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        String key = data.getValue().toString();
                        String child = data.getKey().toString();
                        HomePage.listKeys.add(key);
                        listKeysEncrypt.add(new Keys(key, child));
                        String keyDecrypt = Hybrid_AES_DES.decrypt("TruongXuanNguyen", key);
                        keyDecrypt = keyDecrypt.trim();
                        keyDecrypt = keyDecrypt.substring(0, keyDecrypt.length() - 11);
                        listKeys.add(new Keys(keyDecrypt, child));
                        listViewKeys.notifyDataSetChanged();
                        tvEmpty.setVisibility(View.GONE);
                        lvKeys.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvEmpty.setText("Chưa có key nào");
                    tvEmpty.setTextSize(24);
                    tvEmpty.setTextColor(Color.RED);
                    lvKeys.setEmptyView(tvEmpty);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                try {
                    Utilities.showAlertDialog("Thông báo", "Đã xảy ra lỗi trong quá trình kiểm tra dữ liệu\nVui lòng thử lại sau", getContext(), false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showDialog() {
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_input_password);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        Button btnOK = dialog.findViewById(R.id.btnOK);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        final TextView tvReport = dialog.findViewById(R.id.tvReport);
        final EditText edtCheckPwd = dialog.findViewById(R.id.edtCheckPwd);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtCheckPwd.getText().toString().equals(FragmentLogin.pwd)) {
                    listViewKeys = new ListViewKeys(getContext(), listKeys);
                    lvKeys.setAdapter(listViewKeys);
                    listViewKeys.notifyDataSetChanged();
                    allowDelete = true;
                    dialog.dismiss();
                } else {
                    if (edtCheckPwd.getText().toString().equals(""))
                        tvReport.setText("Vui lòng nhập mật khẩu để show key");
                    else
                        tvReport.setText("Mật khẩu không đúng");
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ckbShowKey.setChecked(false);
                allowDelete = false;
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
