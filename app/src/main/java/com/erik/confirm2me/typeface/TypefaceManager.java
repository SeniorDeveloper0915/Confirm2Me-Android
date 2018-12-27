package com.erik.confirm2me.typeface;

import android.content.Context;
import android.graphics.Typeface;
import android.util.SparseArray;


/**
 * The manager of roboto typefaces.
 *
 * @author Evgeny Shishkin
 */
public class TypefaceManager {

    public static final int DEFAULT = Typefaces.LATO;
    /**
     * Array of created typefaces for later reused.
     */
    private final static SparseArray<Typeface> mTypefaces = new SparseArray<Typeface>(3);

    /**
     * Obtain typeface.
     *
     * @param context       The Context the widget is running in, through which it can access the current theme,
     *                      resources, etc.
     * @param typefaceValue The value of "typeface" attribute
     * @return specify {@link android.graphics.Typeface}
     * @throws IllegalArgumentException if unknown `typeface` attribute value.
     */
    public static Typeface obtainTypeface(Context context, int typefaceValue)
            throws IllegalArgumentException {
        Typeface typeface = mTypefaces.get(typefaceValue);
        if (typeface == null) {
            typeface = createTypeface(context, typefaceValue);
            mTypefaces.put(typefaceValue, typeface);
        }
        return typeface;
    }

    /**
     * Create typeface from assets.
     *
     * @param context       The Context the widget is running in, through which it can
     *                      access the current theme, resources, etc.
     * @param typefaceValue The value of "typeface" attribute
     * @return Roboto {@link android.graphics.Typeface}
     * @throws IllegalArgumentException if unknown `typeface` attribute value.
     */
    private static Typeface createTypeface(Context context, int typefaceValue)
            throws IllegalArgumentException {
        String typefacePath;
        switch (typefaceValue) {
            case Typefaces.LATO:
                typefacePath = "fonts/lato/Lato-Regular.ttf";
                break;
            case Typefaces.LATO_BOLD:
                typefacePath = "fonts/lato/Lato-Bold.ttf";
                break;
            case Typefaces.OPEN_SANS:
                typefacePath = "fonts/opensans/OpenSans-Regular.ttf";
                break;
            case Typefaces.OPEN_SANS_SEMI_BOLD:
                typefacePath = "fonts/opensans/OpenSans-Semibold.ttf";
                break;
            default:
                throw new IllegalArgumentException("Unknown `typeface` attribute value " + typefaceValue);
        }

        return Typeface.createFromAsset(context.getAssets(), typefacePath);
    }

    /**
     * Available values ​​for the "typeface" attribute.
     */
    public class Typefaces {
        public final static int LATO = 0;
        public final static int LATO_BOLD = 1;
        public final static int OPEN_SANS = 2;
        public final static int OPEN_SANS_SEMI_BOLD = 3;
    }
}