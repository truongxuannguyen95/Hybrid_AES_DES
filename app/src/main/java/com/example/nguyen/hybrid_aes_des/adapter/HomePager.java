package com.example.nguyen.hybrid_aes_des.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.nguyen.hybrid_aes_des.activity.FragmentDecrypt;
import com.example.nguyen.hybrid_aes_des.activity.FragmentEncrypt;
import com.example.nguyen.hybrid_aes_des.activity.FragmentListKeys;

public class HomePager extends FragmentStatePagerAdapter {

    private int numOfTabs;
    public HomePager(FragmentManager fm, int numOfTabs) {
        super(fm);
        this.numOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0:
                FragmentEncrypt fragmentEncryptFragment = new FragmentEncrypt();
                return fragmentEncryptFragment;
            case 1:
                FragmentDecrypt fragmentDecryptFragment = new FragmentDecrypt();
                return fragmentDecryptFragment;
            case 2:
                FragmentListKeys fragmentListKeysFragment = new FragmentListKeys();
                return fragmentListKeysFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
