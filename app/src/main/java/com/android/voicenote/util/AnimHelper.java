package com.android.voicenote.util;

import android.animation.ObjectAnimator;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by lvjinhua on 6/1/2016.
 */
public class AnimHelper {

    public static void showLayout(LinearLayout layout){
        layout.setVisibility(View.VISIBLE);
    }
    public static void hidelayout(LinearLayout layout){
        layout.setVisibility(View.GONE);
    }
    public static void showFabBtn(FloatingActionButton fab){
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(fab, "scaleX", 0, 1.0f);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(fab, "scaleY", 0, 1.0f);
        animatorX.setDuration(200);
        animatorY.setDuration(200);
        fab.setVisibility(View.VISIBLE);
        animatorX.start();
        animatorY.start();


    }

    public static void hideFabBtn(final FloatingActionButton fab){
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(fab, "scaleX", 1.0f, 0);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(fab, "scaleY", 1.0f, 0);
        animatorX.setDuration(200);
        animatorY.setDuration(200);
        animatorX.start();
        animatorY.start();


    }
}
