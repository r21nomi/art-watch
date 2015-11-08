package com.nomi.artwatch.di.module;

import android.app.Activity;

import dagger.Module;

/**
 * Created by Ryota Niinomi on 2015/11/03.
 */
@Module
public class ActivityModule {

    protected Activity mActivity;

    public ActivityModule(Activity activity) {
        mActivity = activity;
    }
}
