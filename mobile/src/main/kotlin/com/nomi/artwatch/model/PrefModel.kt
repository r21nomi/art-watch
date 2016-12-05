package com.nomi.artwatch.model

import android.content.Context
import android.content.SharedPreferences

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Ryota Niinomi on 15/12/06.
 */
@Singleton
class PrefModel
@Inject
constructor(private val mContext: Context) {

    companion object {
        val TOKEN_PREF = "token_pref"
        val TOKEN = "token"
        val TOKEN_SECRET = "token_secret"
    }

    fun getPref(name: String): SharedPreferences {
        return mContext.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    fun setTokens(token: String, tokenSecret: String) {
        this.token = token
        this.tokenSecret = tokenSecret
    }

    var token: String
        get() = getPref(TOKEN_PREF).getString(TOKEN, "")
        set(token) = getPref(TOKEN_PREF).edit().putString(TOKEN, token).apply()

    var tokenSecret: String
        get() = getPref(TOKEN_PREF).getString(TOKEN_SECRET, "")
        set(tokenSecret) = getPref(TOKEN_PREF).edit().putString(TOKEN_SECRET, tokenSecret).apply()

    fun clear() {
        getPref(TOKEN_PREF).edit().clear().commit()
    }
}
