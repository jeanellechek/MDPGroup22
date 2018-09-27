package com.androiddeft.mdp;


import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;


public class RepeatListener implements View.OnTouchListener {

    private Handler handler = new Handler();
    private int initInterval;
    private final int normalInterval;
    private final View.OnClickListener clickListener;

    private Runnable handlerRunnable = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this, normalInterval);
            clickListener.onClick(downView);
        }
    };

    private View downView;

    public RepeatListener(int initInterval, int normalInterval, View.OnClickListener clickListener) {
        if (clickListener == null) {
            throw new IllegalArgumentException("null runnnable");
        }
        if (initInterval < 0 || normalInterval < 0) {
            throw new IllegalArgumentException("negative interval");
        }

        this.initInterval = initInterval;
        this.normalInterval = normalInterval;
        this.clickListener = clickListener;
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handler.removeCallbacks(handlerRunnable);
                handler.postDelayed(handlerRunnable, initInterval);
                downView = view;
                downView.setPressed(true);
                clickListener.onClick(view);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                handler.removeCallbacks(handlerRunnable);
                downView.setPressed(false);
                downView = null;
                return true;
        }
        return false;
    }
}