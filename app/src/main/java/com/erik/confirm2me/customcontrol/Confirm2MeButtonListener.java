package com.erik.confirm2me.customcontrol;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by happiness on 10/20/2015.
 */
public class Confirm2MeButtonListener implements View.OnTouchListener {

    static Confirm2MeButtonListener globalInstance;

    public Confirm2MeButtonListener() {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            v.setAlpha(0.5f);
        }
        else {
            v.setAlpha(1.0f);
        }
        return false;
    }

    public static Confirm2MeButtonListener getInstance() {
        if (globalInstance == null)
            globalInstance = new Confirm2MeButtonListener();
        return globalInstance;
    }
}
