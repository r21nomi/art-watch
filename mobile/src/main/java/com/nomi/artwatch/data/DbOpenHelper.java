package com.nomi.artwatch.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nomi.artwatch.data.entity.PhotoSizeEntity;

import timber.log.Timber;

/**
 * Created by Ryota Niinomi on 2016/04/18.
 */
public class DbOpenHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String CREATE_ITEM = ""
            + "CREATE TABLE " + PhotoSizeEntity.TABLE + "("
            + PhotoSizeEntity.URL + " TEXT NOT NULL PRIMARY KEY,"
            + PhotoSizeEntity.WIDTH + " INTEGER NOT NULL DEFAULT 0,"
            + PhotoSizeEntity.HEIGHT + " INTEGER NOT NULL DEFAULT 0,"
            + PhotoSizeEntity.UPDATED_AT + " INTEGER NOT NULL DEFAULT 0"
            + ")";

    public DbOpenHelper(Context context) {
        super(context, "gif.db", null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ITEM);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.d("Migration is needed.");
    }
}
