package com.example.nguyen.hybrid_aes_des.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.example.nguyen.hybrid_aes_des.R;
import com.example.nguyen.hybrid_aes_des.Utilities;
import com.example.nguyen.hybrid_aes_des.adapter.HomePager;
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
    private LinearLayout linearLoad;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);
        linearLoad = findViewById(R.id.linearLoad);
        showActionBar();
        if (UserPage.isOffile) {
            showTab();
        } else {
            if(!Utilities.isOnline(HomePage.this)) {
                showAlertDialog();
            } else {
                getKeys();
            }
        }
    }

    private void showTab(){
        linearLoad.setVisibility(View.GONE);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("Mã hóa"));
        tabLayout.addTab(tabLayout.newTab().setText("Giải mã"));
        if(!UserPage.isOffile) {
            tabLayout.addTab(tabLayout.newTab().setText("Keys"));
        }
        final ViewPager homePager = findViewById(R.id.homePager);
        View root = tabLayout.getChildAt(0);
        if (root instanceof LinearLayout) {
            ((LinearLayout) root).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(getResources().getColor(R.color.colorLine));
            drawable.setSize(1, 1);
            ((LinearLayout) root).setDividerPadding(10);
            ((LinearLayout) root).setDividerDrawable(drawable);
        }
        HomePager homePagerFragment = new HomePager(
                getSupportFragmentManager(),
                tabLayout.getTabCount()
        );
        homePager.setAdapter(homePagerFragment);
        homePager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                homePager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
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
                    showTab();
                } else {
                    showTab();
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

    private void showActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        if(UserPage.isOffile) {
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
            finish();
            startActivity(new Intent(HomePage.this, UserPage.class));
        }
        if(item.getItemId() == R.id.item_change) {
            finish();
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
            startActivity(new Intent(HomePage.this, UserPage.class));
        }
        if(item.getItemId() == R.id.item_exit) {
            System.exit(0);
        }
        return super.onOptionsItemSelected(item);
    }
}
