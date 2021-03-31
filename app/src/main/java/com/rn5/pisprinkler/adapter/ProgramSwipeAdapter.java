package com.rn5.pisprinkler.adapter;

import android.content.Context;

import com.rn5.pisprinkler.ProgramFragment;
import com.rn5.pisprinkler.listener.CreateListener;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ProgramSwipeAdapter extends FragmentStateAdapter {
    private static int NUM_PAGES = 5;
    private final FragmentActivity fa;
    private final Context appContext;
    private final CreateListener listener;

    public ProgramSwipeAdapter(FragmentActivity fa, Context context, CreateListener listener, int p) {
        super(fa);
        this.fa = fa;
        this.appContext = context;
        this.listener = listener;
        NUM_PAGES = p;
    }

    @Override
    @NonNull
    public Fragment createFragment(int position) {
        return new ProgramFragment(appContext).withPos(position).withListener(listener);
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}
