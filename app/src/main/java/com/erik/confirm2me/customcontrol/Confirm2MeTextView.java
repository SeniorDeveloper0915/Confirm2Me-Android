package com.erik.confirm2me.customcontrol;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.erik.confirm2me.typeface.TypefaceUtils;


public class Confirm2MeTextView extends TextView {
    public Confirm2MeTextView(Context context) {
        this(context, null, 0);
    }

    public Confirm2MeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Confirm2MeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
            TypefaceUtils.initTypeface(this, context, attrs);
        }
    }
}
