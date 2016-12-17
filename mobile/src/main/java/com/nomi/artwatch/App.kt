package com.nomi.artwatch

import android.app.Application
import com.bumptech.glide.Glide
import com.bumptech.glide.MemoryCategory
import com.bumptech.glide.integration.okhttp.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.nomi.artwatch.di.component.ApplicationComponent
import com.nomi.artwatch.di.component.DaggerApplicationComponent
import com.nomi.artwatch.di.module.ApplicationModule
import com.nomi.artwatch.ui.util.CrashReportingTree
import com.squareup.okhttp.OkHttpClient
import timber.log.Timber
import java.io.InputStream
import javax.inject.Inject

/**
 * Created by Ryota Niinomi on 2015/11/03.
 */
open class App : Application() {

    val applicationComponent: ApplicationComponent by lazy {
        DaggerApplicationComponent.builder()
                .applicationModule(ApplicationModule(this))
                .build()
    }

    @Inject
    lateinit var mOkHttpClient: OkHttpClient

    override fun onCreate() {
        super.onCreate()

        applicationComponent.inject(this)

        init()
    }

    private fun init() {
        Config.init(this)
        Timber.plant(CrashReportingTree())

        val glide = Glide.get(this)
        glide.setMemoryCategory(MemoryCategory.LOW)
        glide.register(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(mOkHttpClient))
    }
}
