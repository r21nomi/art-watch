package com.nomi.artwatch.di.module;

import com.nomi.artwatch.Application;

/**
 * Created by Ryota Niinomi on 2015/11/03.
 */
public class Modules {
    public static ApplicationModule applicationModule(Application application) {
        return new ApplicationModule(application);
    }
}
