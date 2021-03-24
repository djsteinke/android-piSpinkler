package com.rn5.pisprinkler.adapter;

import com.rn5.pisprinkler.ProgramFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ProgramSwipeAdapter extends FragmentStateAdapter {
    private static int NUM_PAGES = 5;

    public ProgramSwipeAdapter(FragmentActivity fa, int p) {
        super(fa);
        NUM_PAGES = p;
    }

    @Override
    @NonNull
    public Fragment createFragment(int position) {
        return new ProgramFragment().withPos(position);
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}
