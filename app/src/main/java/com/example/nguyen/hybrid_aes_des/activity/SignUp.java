package com.example.nguyen.hybrid_aes_des.activity;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.nguyen.hybrid_aes_des.R;
import com.example.nguyen.hybrid_aes_des.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public SignUp() {
    }

    public static SignUp newInstance(String param1, String param2) {
        SignUp fragment = new SignUp();
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

    private EditText edtEmail, edt_stPassword, edt_ndPassword;
    private Button btnSignUp;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        mAuth = FirebaseAuth.getInstance();
        edtEmail = view.findViewById(R.id.edtEmail);
        edt_stPassword = view.findViewById(R.id.edt_stPassword);
        edt_ndPassword = view.findViewById(R.id.edt_ndPassword);
        btnSignUp = view.findViewById(R.id.btnSignUp);
        final Drawable icon = getResources().getDrawable(R.mipmap.ic_error);
        if (icon != null) {
            icon.setBounds(0, 0,
                    icon.getIntrinsicWidth(),
                    icon.getIntrinsicHeight());
        }

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edtEmail.getText().toString();
                String stPassword = edt_stPassword.getText().toString();
                String ndPassword = edt_ndPassword.getText().toString();
                if (email.isEmpty())
                    edtEmail.setError("Vui lòng nhập email", icon);
                else if (!Utilities.isValidEmail(email))
                    edtEmail.setError("Email không hợp lệ", icon);
                else if (stPassword.isEmpty())
                    edt_stPassword.setError("Vui lòng nhập mật khẩu", icon);
                else if (!Utilities.isValidPassword(stPassword))
                    edt_stPassword.setError("Mật khẩu phải từ 6 ký tự trở lên", icon);
                else if (ndPassword.isEmpty())
                    edt_ndPassword.setError("Vui lòng nhập xác thực mật khẩu", icon);
                else if (!ndPassword.equals(stPassword))
                    edt_ndPassword.setError("Mật khẩu xác thực không khớp", icon);
                else
                    Start_SignUp(email, stPassword);
            }
        });

        return view;
    }

    private void Start_SignUp(String email, String password){
        Utilities.showProgressDialog("Đang đăng ký", getContext());
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Utilities.dismissProgressDialog();
                        if(task.isSuccessful())
                        {
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            DatabaseReference mData = FirebaseDatabase.getInstance().getReference();
                            mData.child("users").child(currentUser.getUid()).setValue("");
                            Utilities.showAlertDialog("Đăng ký thành công", "Bạn đã có thể tiến hành đăng nhập bằng tài khoản này", getContext());
                            edtEmail.setText("");
                            edt_stPassword.setText("");
                            edt_ndPassword.setText("");
                        }
                        else {
                            if(Utilities.isOnline(getContext()))
                                Utilities.showAlertDialog("Đăng ký thất bại", "Email này đã được sử dụng\nVui lòng sử dụng 1 email khác", getContext());
                            else
                                Utilities.showAlertDialog("Đăng ký thất bại", "Thiết bị của bạn chưa được kết nối internet", getContext());
                        }
                    }
                });
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
