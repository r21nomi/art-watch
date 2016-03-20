package com.nomi.artwatch.ui.util;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Map;
import java.util.WeakHashMap;

import timber.log.Timber;

/**
 * Created by Ryota Niinomi on 2016/03/16.
 */
public class TypefaceCache {

    private static final TypefaceCache sSingleton = new TypefaceCache();
    private Map<String, Typeface> cache;
    private TypefaceCache() {
        cache = new WeakHashMap<>();
    }

    public static TypefaceCache getInstance() {
        return sSingleton;
    }

    public Typeface getTypeface(int resourceId, Context context) {
        String fontPath = context.getString(resourceId);
        return getTypeface(fontPath, context);
    }

    public Typeface getTypeface(String fontPath, Context context) {
        Typeface typeface = cache.get(fontPath);
        if (typeface == null) {
            try {
                typeface = Typeface.createFromAsset(context.getAssets(), fontPath);
                cache.put(fontPath, typeface);
            } catch (RuntimeException e) {
                Timber.e(e, "Failed to load font. Use system font");
                typeface = Typeface.DEFAULT;
            }
        }
        return typeface;
    }
}
