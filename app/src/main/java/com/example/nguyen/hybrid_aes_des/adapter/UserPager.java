package com.example.nguyen.hybrid_aes_des.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.nguyen.hybrid_aes_des.activity.FragmentForget;
import com.example.nguyen.hybrid_aes_des.activity.FragmentLogin;
import com.example.nguyen.hybrid_aes_des.activity.FragmentSignUp;

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
                FragmentLogin fragmentLoginFragment = new FragmentLogin();
                return fragmentLoginFragment;
            case 1:
                FragmentSignUp fragmentSignUpFragment = new FragmentSignUp();
                return fragmentSignUpFragment;
            case 2:
                FragmentForget resetFragment = new FragmentForget();
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
