package com.example.nguyen.hybrid_aes_des.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
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

import com.example.nguyen.hybrid_aes_des.R;
import com.example.nguyen.hybrid_aes_des.Utilities;
import com.example.nguyen.hybrid_aes_des.model.Hybrid_AES_DES;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class FragmentLogin extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public FragmentLogin() {
    }

    public static FragmentLogin newInstance(String param1, String param2) {
        FragmentLogin fragment = new FragmentLogin();
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

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private CheckBox ckbRemember;
    private FirebaseAuth mAuth;
    public static String pwd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        mAuth = FirebaseAuth.getInstance();
        edtEmail = view.findViewById(R.id.edtEmail);
        edtPassword = view.findViewById(R.id.edtPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        ckbRemember = view.findViewById(R.id.ckbRemember);

        final SharedPreferences pref = getActivity().getSharedPreferences("sharedSettings", 0);
        String email = pref.getString("email", "");
        String password = pref.getString("password", "");
        Boolean remember = pref.getBoolean("remember", false);
        int length = pref.getInt("length", 0);

        ckbRemember.setChecked(remember);

        if(ckbRemember.isChecked()) {
            if(email.length() > 1 && password.length() > 1) {
                edtEmail.setText(email);
                edtPassword.setText(Hybrid_AES_DES.decrypt("TruongXuanNguyen", password).substring(0, length));
            } else if(email.length() > 1) {
                edtEmail.setText(email);
            }
        }

        ckbRemember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = pref.edit();
                if(isChecked) {
                    editor.putBoolean("remember", true);
                } else {
                    editor.putBoolean("remember", false);
                }
                editor.commit();
            }
        });

        final Drawable icon = getResources().getDrawable(R.mipmap.ic_error);
        if (icon != null) {
            icon.setBounds(0, 0,
                    icon.getIntrinsicWidth(),
                    icon.getIntrinsicHeight());
        }
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edtEmail.getText().toString();
                String password = edtPassword.getText().toString();
                if(email.isEmpty())
                    edtEmail.setError("Vui lòng nhập email", icon);
                else if(!Utilities.isValidEmail(email))
                    edtEmail.setError("Email không hợp lệ", icon);
                else if(password.isEmpty())
                    edtPassword.setError("Vui lòng nhập mật khẩu", icon);
                else {
                    StartLogin(email, password);
                }
            }
        });

        return view;
    }

    private void StartLogin(String email, final String password){
        Utilities.showProgressDialog("Đang đăng nhập", getContext());
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Utilities.dismissProgressDialog();
                        if(task.isSuccessful()) {
                            pwd = edtPassword.getText().toString();
                            SharedPreferences pref = getActivity().getSharedPreferences("sharedSettings", 0);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("email", edtEmail.getText().toString());
                            if(ckbRemember.isChecked()) {
                                editor.putString("password", Hybrid_AES_DES.encrypt("TruongXuanNguyen", pwd));
                                editor.putInt("length", pwd.length());
                            } else {
                                editor.putString("password", "");
                                editor.putInt("length", 0);
                            }
                            editor.commit();
                            UserPage.isOffile = false;
                            getActivity().finish();
                            startActivity(new Intent(getContext(), HomePage.class));
                        } else {
                            if(Utilities.isOnline(getContext()))
                                Utilities.showAlertDialog("Đăng nhập thất bại", "Email hoặc mật khẩu sai!", getContext(), false);
                            else
                                Utilities.showAlertDialog("Thông báo", "Thiết bị của bạn chưa được kết nối internet", getContext(), false);
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
