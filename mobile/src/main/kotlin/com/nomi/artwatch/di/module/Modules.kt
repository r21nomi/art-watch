package com.nomi.artwatch.di.module

import com.nomi.artwatch.App

/**
 * Created by Ryota Niinomi on 2015/11/03.
 */
object Modules {
    fun applicationModule(app: App): ApplicationModule {
        return ApplicationModule(app)
    }
}
