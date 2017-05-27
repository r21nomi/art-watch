package com.nomi.artwatch.data

import android.database.Cursor

/**
 * Created by Ryota Niinomi on 2016/04/24.
 */
object Db {

    val BOOLEAN_FALSE = 0
    val BOOLEAN_TRUE = 1

    fun getString(cursor: Cursor, columnName: String): String {
        return cursor.getString(cursor.getColumnIndexOrThrow(columnName))
    }

    fun getBoolean(cursor: Cursor, columnName: String): Boolean {
        return getInt(cursor, columnName) == BOOLEAN_TRUE
    }

    fun getLong(cursor: Cursor, columnName: String): Long {
        return cursor.getLong(cursor.getColumnIndexOrThrow(columnName))
    }

    fun getInt(cursor: Cursor, columnName: String): Int {
        return cursor.getInt(cursor.getColumnIndexOrThrow(columnName))
    }
}
