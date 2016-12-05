package com.nomi.artwatch.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.nomi.artwatch.App

import com.nomi.artwatch.di.component.ActivityComponent
import com.nomi.artwatch.di.component.DaggerActivityComponent
import com.nomi.artwatch.di.module.ActivityModule

import rx.subscriptions.CompositeSubscription

/**
 * Created by Ryota Niinomi on 2015/11/03.
 */
abstract class InjectActivity : AppCompatActivity() {

    private val mActivityComponent: ActivityComponent by lazy {
        DaggerActivityComponent.builder()
                .applicationComponent((application as App).applicationComponent)
                .activityModule(ActivityModule(this))
                .build()
    }
    protected var mSubscriptionsOnDestroy: CompositeSubscription = CompositeSubscription()

    protected abstract fun injectDependency(component: ActivityComponent)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mSubscriptionsOnDestroy = CompositeSubscription()

        injectDependency(mActivityComponent)
    }

    override fun onDestroy() {
        mSubscriptionsOnDestroy.unsubscribe()
        super.onDestroy()
    }
}
