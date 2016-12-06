package com.nomi.artwatch.di.component

import com.nomi.artwatch.App
import com.nomi.artwatch.di.module.ApplicationModule
import com.nomi.artwatch.model.*
import com.squareup.sqlbrite.BriteDatabase
import dagger.Component
import javax.inject.Singleton

/**
 * Created by Ryota Niinomi on 2015/11/03.
 */
@Singleton
@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationComponent {
    fun inject(app: App)

    fun briteDatabase(): BriteDatabase
    fun prefModel(): PrefModel
    fun loginModel(): LoginModel
    fun userModel(): UserModel
    fun postModel(): PostModel
    fun blogModel(): BlogModel
}
