package com.nomi.artwatch.di.component;

import com.nomi.artwatch.di.ActivityScope;
import com.nomi.artwatch.di.module.ActivityModule;
import com.nomi.artwatch.ui.activity.MainActivity;

import dagger.Component;

/**
 * Created by Ryota Niinomi on 2015/11/03.
 */
@ActivityScope
@Component(
        dependencies = ApplicationComponent.class,
        modules = {
                ActivityModule.class
        }
)
public interface ActivityComponent {
    void inject(MainActivity mainActivity);
}
