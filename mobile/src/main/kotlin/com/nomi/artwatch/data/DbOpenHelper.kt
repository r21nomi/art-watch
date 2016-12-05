package com.nomi.artwatch.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import com.nomi.artwatch.data.cache.GifCache

import timber.log.Timber

/**
 * Created by Ryota Niinomi on 2016/04/18.
 */
class DbOpenHelper(context: Context) : SQLiteOpenHelper(context, "gif.db", null, DbOpenHelper.VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_GIF_CACHE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Timber.d("Migration is needed.")
    }

    companion object {
        private val VERSION = 1
        private val CREATE_GIF_CACHE = "" + "CREATE TABLE " +
                GifCache.TABLE + "(" +
                GifCache.ORIGINAL_GIF_URL + " TEXT NOT NULL PRIMARY KEY," +
                GifCache.PHOTO_SIZES + " TEXT NOT NULL DEFAULT 0," +
                GifCache.CAPTION + " TEXT NOT NULL DEFAULT ''," +
                GifCache.UPDATED_AT + " INTEGER NOT NULL DEFAULT 0" +
                ")"
    }
}
