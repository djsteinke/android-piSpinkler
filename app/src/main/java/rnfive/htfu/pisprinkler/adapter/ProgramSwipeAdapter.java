package rnfive.htfu.pisprinkler.adapter;

import rnfive.htfu.pisprinkler.ProgramFragment;
import rnfive.htfu.pisprinkler.listener.CreateListener;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ProgramSwipeAdapter extends FragmentStateAdapter {
    private static int NUM_PAGES = 5;
    private final CreateListener listener;


    public ProgramSwipeAdapter(FragmentActivity activity, int p, CreateListener listener) {
        super(activity);
        this.listener = listener;
        NUM_PAGES = p;
    }

    @Override
    @NonNull
    public Fragment createFragment(int position) {
        return new ProgramFragment().withPos(position).withListener(listener);
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}
