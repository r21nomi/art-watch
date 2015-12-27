package com.nomi.artwatch;

import android.content.Context;

/**
 * Created by Ryota Niinomi on 2015/11/03.
 */
public class Config {

    public static String CONSUMER_KEY;
    public static String CONSUMER_SECRET;

    public static void init(Context context) {
        CONSUMER_KEY = context.getString(R.string.consumer_key);
        CONSUMER_SECRET = context.getString(R.string.consumer_secret);
    }
}
