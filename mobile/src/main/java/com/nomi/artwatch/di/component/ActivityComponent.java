package com.nomi.artwatch.di.component;

import com.nomi.artwatch.di.ActivityScope;
import com.nomi.artwatch.di.module.ActivityModule;
import com.nomi.artwatch.ui.activity.HistoryActivity;
import com.nomi.artwatch.ui.activity.LoginActivity;
import com.nomi.artwatch.ui.activity.MainActivity;
import com.nomi.artwatch.ui.activity.SettingActivity;
import com.nomi.artwatch.ui.activity.SplashActivity;

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
    void inject(SplashActivity activity);
    void inject(LoginActivity activity);
    void inject(MainActivity activity);
    void inject(HistoryActivity activity);
    void inject(SettingActivity activity);
}
