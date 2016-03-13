package com.nomi.artwatch;

import com.nomi.artwatch.di.component.ApplicationComponent;
import com.nomi.artwatch.di.component.DaggerApplicationComponent;
import com.nomi.artwatch.di.module.ApplicationModule;

import timber.log.Timber;

/**
 * Created by Ryota Niinomi on 2015/11/03.
 */
public class Application extends android.app.Application {

    private ApplicationComponent mApplicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        init();

        mApplicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();

        mApplicationComponent.inject(this);
    }

    public ApplicationComponent getApplicationComponent() {
        return mApplicationComponent;
    }

    private void init() {
        Config.init(this);
        Timber.plant(new Timber.DebugTree());
    }
}
