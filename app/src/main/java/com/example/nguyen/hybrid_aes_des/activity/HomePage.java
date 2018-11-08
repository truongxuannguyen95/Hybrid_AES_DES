package com.example.nguyen.hybrid_aes_des.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.nguyen.hybrid_aes_des.R;
import com.example.nguyen.hybrid_aes_des.Utilities;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomePage extends AppCompatActivity {

    public static ArrayList<String> listKeys;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_signup);
        showActionBar();
    }

    private void showActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        if(Login_SignUp.isOffile) {
            MenuItem itemChange = menu.findItem(R.id.item_change);
            MenuItem itemLogout = menu.findItem(R.id.item_logout);
            itemChange.setVisible(false);
            itemLogout.setVisible(false);
        } else {
            MenuItem itemLogin = menu.findItem(R.id.item_login);
            itemLogin.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.item_login) {
            startActivity(new Intent(HomePage.this, Login_SignUp.class));
        }
        if(item.getItemId() == R.id.item_change) {
            startActivity(new Intent(HomePage.this, ChangePassword.class));
        }
        if(item.getItemId() == R.id.item_logout) {
            FirebaseAuth.getInstance().signOut();
            SharedPreferences pref = getSharedPreferences("sharedSettings", 0);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("password", "");
            editor.putInt("length", 0);
            editor.commit();
            finish();
            startActivity(new Intent(HomePage.this, Login_SignUp.class));
        }
        if(item.getItemId() == R.id.item_exit) {
            System.exit(0);
        }
        return super.onOptionsItemSelected(item);
    }

    public void getKeys(){
        listKeys = new ArrayList<>();
        DatabaseReference mData = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        String userID = currentUser.getUid();
        mData.child("users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot data : dataSnapshot.getChildren()){
                        String key = data.getValue().toString();
                        listKeys.add(key);
                    }
                    //showTab();
                } else {
                    //showTab();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Utilities.showAlertDialog("Thông báo", "Đã xảy ra lỗi trong quá trình kiểm tra dữ liệu. Vui lòng thử lại sau", HomePage.this);
            }
        });
    }

    public void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder( HomePage.this);
        builder.setTitle("Lỗi kết nối");
        builder.setMessage("Thiết bị của bạn chưa được kết nối internet. Vui lòng kiểm tra lại");
        builder.setCancelable(false);
        builder.setNegativeButton("Xác nhận", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                if(!Utilities.isOnline(HomePage.this))
                    showAlertDialog();
                else
                    getKeys();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
