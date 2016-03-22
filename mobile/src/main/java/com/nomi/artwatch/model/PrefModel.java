package com.nomi.artwatch.model;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Ryota Niinomi on 15/12/06.
 */
@Singleton
public class PrefModel {

    public static final String TOKEN_PREF = "token_pref";
    public static final String TOKEN = "token";
    public static final String TOKEN_SECRET = "token_secret";

    private Context mContext;

    @Inject
    public PrefModel(Context context) {
        mContext = context;
    }

    public SharedPreferences getPref(String name) {
        return mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public void setTokens(String token, String tokenSecret) {
        setToken(token);
        setTokenSecret(tokenSecret);
    }

    public void setToken(String token) {
        getPref(TOKEN_PREF).edit().putString(TOKEN, token).apply();
    }

    public void setTokenSecret(String tokenSecret) {
        getPref(TOKEN_PREF).edit().putString(TOKEN_SECRET, tokenSecret).apply();
    }

    public String getToken() {
        return getPref(TOKEN_PREF).getString(TOKEN, "");
    }

    public String getTokenSecret() {
        return getPref(TOKEN_PREF).getString(TOKEN_SECRET, "");
    }

    public void clear() {
        getPref(TOKEN_PREF).edit().clear().commit();
    }
}
