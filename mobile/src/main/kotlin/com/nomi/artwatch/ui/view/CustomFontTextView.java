package com.nomi.artwatch.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.nomi.artwatch.R;
import com.nomi.artwatch.ui.util.TypefaceCache;

/**
 * Created by Ryota Niinomi on 2016/03/16.
 */
public class CustomFontTextView extends TextView {

    private static int[] FONT_FAMILIES = {
            R.string.font_light
    };

    public CustomFontTextView(final Context context) {
        super(context);
    }

    public CustomFontTextView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setFont(context, attrs);
    }

    public CustomFontTextView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        setFont(context, attrs);
    }

    private void setFont(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomFontTextView);

        int font = typedArray.getInt(R.styleable.CustomFontTextView_fontType, 0);
        int fontFamily = FONT_FAMILIES[font];

        Typeface typeface = TypefaceCache.getInstance().getTypeface(fontFamily, context);
        setTypeface(typeface);
        typedArray.recycle();
    }
}