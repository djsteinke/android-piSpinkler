package rnfive.htfu.pisprinkler.adapter;

import java.util.Map;

import rnfive.htfu.pisprinkler.ProgramFragment;
import rnfive.htfu.pisprinkler.define.ProgramFB;
import rnfive.htfu.pisprinkler.listener.CreateListener;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import static rnfive.htfu.pisprinkler.MainActivityPiSprinkler.setupFB;

public class ProgramSwipeAdapter extends FragmentStateAdapter {
    private static int NUM_PAGES = 0;
    private final CreateListener listener;


    public ProgramSwipeAdapter(FragmentActivity activity, CreateListener listener) {
        super(activity);
        this.listener = listener;
        if (setupFB != null)
            NUM_PAGES = setupFB.getPrograms().size();
    }

    @Override
    @NonNull
    public Fragment createFragment(int position) {
        int i = 0;
        ProgramFB programFB= new ProgramFB();
        for (Map.Entry<String, ProgramFB> entry : setupFB.getPrograms().entrySet()) {
            if (i++ == position)
                programFB = setupFB.getPrograms().get(entry.getKey());
        }
        return new ProgramFragment(programFB).withPos(position).withListener(listener);
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}
