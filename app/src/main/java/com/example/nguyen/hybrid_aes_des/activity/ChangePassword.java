package com.example.nguyen.hybrid_aes_des.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.nguyen.hybrid_aes_des.R;
import com.example.nguyen.hybrid_aes_des.Utilities;
import com.example.nguyen.hybrid_aes_des.model.AES;
import com.example.nguyen.hybrid_aes_des.model.Hybrid_AES_DES;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {

    private EditText edtOldPw, edt_stNewPw, edt_ndNewPw;
    private Button btnChange, btnCancel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changepw);

        edtOldPw = findViewById(R.id.edt_OldPassword);
        edt_stNewPw = findViewById(R.id.edt_stPassword);
        edt_ndNewPw = findViewById(R.id.edt_ndPassword);
        btnChange = findViewById(R.id.btnChangePw);
        btnCancel = findViewById(R.id.btnCancel);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChangePassword.this, HomePage.class));
                finish();
            }
        });

        final Drawable icon = getResources().getDrawable(R.mipmap.ic_error);
        if (icon != null) {
            icon.setBounds(0, 0,
                    icon.getIntrinsicWidth(),
                    icon.getIntrinsicHeight());
        }
        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldPw = edtOldPw.getText().toString();
                final String stNewPw = edt_stNewPw.getText().toString();
                String ndNewPw = edt_ndNewPw.getText().toString();
                if(oldPw.isEmpty())
                    edtOldPw.setError("Vui lòng nhập mật khẩu hiện tại", icon);
                else if(stNewPw.isEmpty())
                    edt_stNewPw.setError("Vui lòng nhập mật khẩu mới", icon);
                else if(!Utilities.isValidPassword(stNewPw))
                    edt_stNewPw.setError("Mật khẩu phải từ 6 ký tự trở lên", icon);
                else if(ndNewPw.isEmpty())
                    edt_ndNewPw.setError("Vui lòng nhập xác thực mật khẩu mới", icon);
                else if(!ndNewPw.equals(stNewPw))
                    edt_ndNewPw.setError("Mật khẩu xác thực không khớp", icon);
                else {
                    final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    currentUser.reauthenticate(EmailAuthProvider.getCredential(currentUser.getEmail(), oldPw)).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Utilities.showProgressDialog("Đang đổi mật khẩu", ChangePassword.this);
                                currentUser.updatePassword(stNewPw).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Utilities.dismissProgressDialog();
                                        if(task.isSuccessful()){
                                            SharedPreferences pref = getSharedPreferences("sharedSettings", 0);
                                            SharedPreferences.Editor editor = pref.edit();
                                            editor.putString("password", Hybrid_AES_DES.encrypt("TruongXuanNguyen", edt_stNewPw.getText().toString()));
                                            editor.putInt("length", stNewPw.length());
                                            editor.commit();
                                            Login.pwd = stNewPw;
                                            edtOldPw.setText("");
                                            edt_stNewPw.setText("");
                                            edt_ndNewPw.setText("");
                                            Utilities.showAlertDialog("Thông báo", "Đổi mật khẩu thành công", ChangePassword.this, true);
                                        }
                                        else {
                                            Utilities.showAlertDialog("Thông báo", "Đổi mật khẩu thất bại\nCó vẻ đã xảy ra lỗi gì đó!", ChangePassword.this, false);
                                        }
                                    }
                                });
                            } else {
                                if(Utilities.isOnline(getApplicationContext()))
                                    Utilities.showAlertDialog("Đổi mật khẩu thất bại", "Mật khẩu cũ không đúng", ChangePassword.this, false);
                                else
                                    Utilities.showAlertDialog("Đổi mật khẩu thất bại", "Thiết bị của bạn chưa được kết nối internet", ChangePassword.this, false);
                            }
                        }
                    });
                }
            }
        });
    }

}