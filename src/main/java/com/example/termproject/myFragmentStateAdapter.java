package com.example.termproject;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class myFragmentStateAdapter extends FragmentStateAdapter{
    private Bundle bundle;
    Fragment fragment1;

    public myFragmentStateAdapter(@NonNull FragmentActivity fragmentActivity, Bundle bundle) {
        super(fragmentActivity);
        this.bundle = bundle;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position){
            case 0:
                fragment1 = new TcpFragment();
                fragment1.setArguments(bundle);
                return fragment1;
            case 1:
                return new UdpFragment();

        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}