package com.example.nguyen.hybrid_aes_des.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.nguyen.hybrid_aes_des.activity.ForgetPassword;
import com.example.nguyen.hybrid_aes_des.activity.Login;
import com.example.nguyen.hybrid_aes_des.activity.SignUp;

public class UserPager extends FragmentStatePagerAdapter {
    private int numOfTabs;
    public UserPager(FragmentManager fm, int numOfTabs) {
        super(fm);
        this.numOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0:
                Login loginFragment = new Login();
                return loginFragment;
            case 1:
                SignUp signUpFragment = new SignUp();
                return signUpFragment;
            case 2:
                ForgetPassword resetFragment = new ForgetPassword();
                return resetFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
