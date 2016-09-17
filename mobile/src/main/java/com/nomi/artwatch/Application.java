package com.nomi.artwatch;

import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import com.bumptech.glide.integration.okhttp.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.nomi.artwatch.di.component.ApplicationComponent;
import com.nomi.artwatch.di.component.DaggerApplicationComponent;
import com.nomi.artwatch.di.module.ApplicationModule;
import com.nomi.artwatch.ui.util.CrashReportingTree;
import com.squareup.okhttp.OkHttpClient;

import java.io.InputStream;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by Ryota Niinomi on 2015/11/03.
 */
public class Application extends android.app.Application {

    private ApplicationComponent mApplicationComponent;

    @Inject
    OkHttpClient mOkHttpClient;

    @Override
    public void onCreate() {
        super.onCreate();

        mApplicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
        mApplicationComponent.inject(this);

        init();
    }

    public ApplicationComponent getApplicationComponent() {
        return mApplicationComponent;
    }

    private void init() {
        Config.init(this);
        Timber.plant(new Timber.DebugTree());
        Timber.plant(new CrashReportingTree());

        Glide glide = Glide.get(this);
        glide.setMemoryCategory(MemoryCategory.LOW);
        glide.register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(mOkHttpClient));
    }
}
