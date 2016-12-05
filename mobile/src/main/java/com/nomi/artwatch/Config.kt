package com.nomi.artwatch

import android.content.Context

/**
 * Created by Ryota Niinomi on 2015/11/03.
 */
object Config {

    lateinit var CONSUMER_KEY: String
    lateinit var CONSUMER_SECRET: String

    fun init(context: Context) {
        CONSUMER_KEY = context.getString(R.string.consumer_key)
        CONSUMER_SECRET = context.getString(R.string.consumer_secret)
    }
}
