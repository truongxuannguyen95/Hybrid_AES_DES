package com.example.nguyen.hybrid_aes_des.activity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.nguyen.hybrid_aes_des.R;
import com.example.nguyen.hybrid_aes_des.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPassword extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ForgetPassword() {
    }

    public static ForgetPassword newInstance(String param1, String param2) {
        ForgetPassword fragment = new ForgetPassword();
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

    private EditText edtEmail;
    private Button btnReset;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_forget_password, container, false);
        edtEmail = view.findViewById(R.id.edtEmail);
        btnReset = view.findViewById(R.id.btnReset);
        final Drawable icon = getResources().getDrawable(R.mipmap.ic_error);
        if (icon != null) {
            icon.setBounds(0, 0,
                    icon.getIntrinsicWidth(),
                    icon.getIntrinsicHeight());
        }
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Utilities.isOnline(getContext())) {
                    String email = edtEmail.getText().toString();
                    if (Utilities.isValidEmail(email)) {
                        Utilities.showProgressDialog("Đang gửi mật khẩu mới", getContext());
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Utilities.dismissProgressDialog();
                                        if (task.isSuccessful()) {
                                            Utilities.showAlertDialog("Thông báo", "Kiểm tra email để nhận mật khẩu mới", getContext());
                                        } else {
                                            Utilities.showAlertDialog("Thông báo", "Email của bạn chưa được đăng ký", getContext());
                                        }
                                    }
                                });
                    } else {
                        Utilities.showAlertDialog("Thông báo", "Bạn cần cung cấp email hợp lệ", getContext());
                        edtEmail.setError("Bạn cần cung cấp email hợp lệ", icon);
                    }
                } else {
                    Utilities.showAlertDialog("Thông báo", "Thiết bị của bạn chưa được kết nối internet", getContext());
                }
            }
        });
        return view;
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
