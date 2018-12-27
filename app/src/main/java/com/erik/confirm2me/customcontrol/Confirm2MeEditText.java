package com.erik.confirm2me.customcontrol;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import com.erik.confirm2me.typeface.TypefaceUtils;


public class Confirm2MeEditText extends EditText {
    public Confirm2MeEditText(Context context) {
        super(context);
        if (!isInEditMode()) {
            TypefaceUtils.initTypeface(this, context, null);
        }
    }

    public Confirm2MeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            TypefaceUtils.initTypeface(this, context, attrs);
        }
    }

    public Confirm2MeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            TypefaceUtils.initTypeface(this, context, attrs);
        }
    }
}
