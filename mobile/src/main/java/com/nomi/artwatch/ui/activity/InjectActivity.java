package com.nomi.artwatch.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.nomi.artwatch.Application;
import com.nomi.artwatch.di.component.ActivityComponent;
import com.nomi.artwatch.di.component.DaggerActivityComponent;
import com.nomi.artwatch.di.module.ActivityModule;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by Ryota Niinomi on 2015/11/03.
 */
public abstract class InjectActivity extends AppCompatActivity {

    private ActivityComponent mActivityComponent;
    protected CompositeSubscription mSubscriptionsOnDestroy;

    protected abstract void injectDependency(ActivityComponent component);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSubscriptionsOnDestroy = new CompositeSubscription();
        mActivityComponent = DaggerActivityComponent.builder()
                .applicationComponent(((Application)getApplication()).getApplicationComponent())
                .activityModule(new ActivityModule(this))
                .build();

        injectDependency(mActivityComponent);
    }

    @Override
    protected void onDestroy() {
        mSubscriptionsOnDestroy.unsubscribe();
        super.onDestroy();
    }
}
