package com.example.nguyen.hybrid_aes_des.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.nguyen.hybrid_aes_des.activity.Decrypt;
import com.example.nguyen.hybrid_aes_des.activity.Encrypt;
import com.example.nguyen.hybrid_aes_des.activity.ListKeys;

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
                Encrypt encryptFragment = new Encrypt();
                return encryptFragment;
            case 1:
                Decrypt decryptFragment = new Decrypt();
                return decryptFragment;
            case 2:
                ListKeys listKeysFragment = new ListKeys();
                return listKeysFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
