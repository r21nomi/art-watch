package com.nomi.artwatch.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nomi.artwatch.data.cache.GifCache;

import timber.log.Timber;

/**
 * Created by Ryota Niinomi on 2016/04/18.
 */
public class DbOpenHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String CREATE_GIF_CACHE = ""
            + "CREATE TABLE " + GifCache.TABLE + "("
            + GifCache.ORIGINAL_GIF_URL + " TEXT NOT NULL PRIMARY KEY,"
            + GifCache.PHOTO_SIZES + " TEXT NOT NULL DEFAULT 0,"
            + GifCache.CAPTION+ " TEXT NOT NULL DEFAULT '',"
            + GifCache.UPDATED_AT + " INTEGER NOT NULL DEFAULT 0"
            + ")";

    public DbOpenHelper(Context context) {
        super(context, "gif.db", null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_GIF_CACHE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.d("Migration is needed.");
    }
}
