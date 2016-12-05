package com.nomi.artwatch.di.component

import com.nomi.artwatch.di.ActivityScope
import com.nomi.artwatch.di.module.ActivityModule
import com.nomi.artwatch.ui.activity.*
import dagger.Component

/**
 * Created by Ryota Niinomi on 2015/11/03.
 */
@ActivityScope
@Component(dependencies = arrayOf(ApplicationComponent::class), modules = arrayOf(ActivityModule::class))
interface ActivityComponent {
    fun inject(activity: SplashActivity)
    fun inject(activity: LoginActivity)
    fun inject(activity: MainActivity)
    fun inject(activity: HistoryActivity)
    fun inject(activity: SettingActivity)
    fun inject(activity: OssActivity)
}
