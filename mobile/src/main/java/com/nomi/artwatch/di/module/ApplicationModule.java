package com.nomi.artwatch.di.module;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Ryota Niinomi on 2015/11/03.
 */
@Module
public class ApplicationModule {

    protected Application mApplication;

    public ApplicationModule(Application application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return mApplication;
    }

    @Provides
    @Singleton
    Context provideApplicationContext() {
        return mApplication.getApplicationContext();
    }

    @Provides
    @Singleton
    Handler provideHandler() {
        return new Handler();
    }
}
