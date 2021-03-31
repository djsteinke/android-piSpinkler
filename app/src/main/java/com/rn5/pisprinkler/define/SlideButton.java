package com.rn5.pisprinkler.define;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Log;
import android.widget.ImageButton;

import com.rn5.pisprinkler.R;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;

public class SlideButton {
    private final static String TAG = SlideButton.class.getSimpleName();
    private List<ImageButton> buttons;
    private final ImageButton main;
    private float offset = -125f;
    private final int duration = 300;
    private boolean expanded = false;
    private int animating = 0;
    private Context context;

    public SlideButton(Context context, ImageButton bt) {
        this.main = bt;
        this.context = context;
        buttons = new ArrayList<>();
    }

    public SlideButton withButtons(List<ImageButton> bts) {
        this.buttons = bts;
        return this;
    }

    public SlideButton withButton(ImageButton bt) {
        this.buttons.add(bt);
        return this;
    }

    public void expand() {
        Log.d(TAG, "build() " + expanded);
        int i = 1;
        animating = 0;

        offset = main.getWidth()*0.3f;
        float totOff = 0f;
        for (ImageButton ib : buttons) {
            if (i == 1) {
                totOff = main.getWidth() + offset;
                offset += (main.getWidth()-ib.getWidth())/2f;
            } else
                totOff += ib.getWidth()+offset;
            ib.animate().translationX((expanded?0:-totOff)).alpha((expanded?0:255)).setDuration(duration);
            i++;
        }

        if (expanded)
            main.animate().rotation(75).setDuration(duration/3).setListener(getListener(2));
        else
            main.animate().rotation(45).setDuration(duration).setListener(getListener(1));
    }

    private Animator.AnimatorListener getListener(int pos) {
        return new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                main.setActivated(pos >= 1);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (pos != 2)
                    expanded = pos >= 1;
                else
                    main.animate().rotation(0).setDuration(duration).setListener(getListener(0));
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        };
    }
}
