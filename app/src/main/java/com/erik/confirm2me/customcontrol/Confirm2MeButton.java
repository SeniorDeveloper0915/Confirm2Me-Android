package com.erik.confirm2me.customcontrol;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import com.erik.confirm2me.typeface.TypefaceUtils;


public class Confirm2MeButton extends Button {

    public Confirm2MeButton(Context context) {
        super(context);
        if (!isInEditMode()) {
            TypefaceUtils.initTypeface(this, context, null);
        }
    }

    public Confirm2MeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            TypefaceUtils.initTypeface(this, context, attrs);
        }
    }

    public Confirm2MeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            TypefaceUtils.initTypeface(this, context, attrs);
        }
    }
}
