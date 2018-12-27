package com.erik.confirm2me.typeface;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.erik.confirm2me.R;


public class TypefaceUtils {
    /**
     * Typeface initialization using the attributes. Used in RobotoTextView constructor.
     *
     * @param textView The roboto text view
     * @param context  The context the widget is running in, through which it can
     *                 access the current theme, resources, etc.
     * @param attrs    The attributes of the XML tag that is inflating the widget.
     */
    public static void initTypeface(TextView textView, Context context, AttributeSet attrs) {
        Typeface typeface;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Confirm2MeTextView);

            int typefaceValue = a.getInt(R.styleable.Confirm2MeTextView_typeface, TypefaceManager.Typefaces.LATO);
            typeface = TypefaceManager.obtainTypeface(context, typefaceValue);

            a.recycle();
        } else {
            typeface = TypefaceManager.obtainTypeface(context, TypefaceManager.Typefaces.LATO);
        }

        setTypeface(textView, typeface);
    }

    /**
     * Setup typeface for TextView. Wrapper over {@link android.widget.TextView#setTypeface(android.graphics.Typeface)}
     * for making the font anti-aliased.
     *
     * @param textView The text view
     * @param typeface The specify typeface
     */
    public static void setTypeface(TextView textView, Typeface typeface) {
        //For making the font anti-aliased.
        textView.setPaintFlags(textView.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        textView.setTypeface(typeface);
    }
}
