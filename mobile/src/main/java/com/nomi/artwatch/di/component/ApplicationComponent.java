package com.nomi.artwatch.di.component;

import com.nomi.artwatch.Application;
import com.nomi.artwatch.di.module.ApplicationModule;
import com.nomi.artwatch.model.PostModel;
import com.nomi.artwatch.model.UserModel;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Ryota Niinomi on 2015/11/03.
 */
@Singleton
@Component(modules = {
        ApplicationModule.class
})
public interface ApplicationComponent {
    void inject(Application application);

    UserModel userModel();
    PostModel postModel();
}
