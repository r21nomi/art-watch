package com.nomi.artwatch.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.nomi.artwatch.Application;
import com.nomi.artwatch.di.component.ActivityComponent;
import com.nomi.artwatch.di.component.DaggerActivityComponent;
import com.nomi.artwatch.di.module.ActivityModule;

/**
 * Created by Ryota Niinomi on 2015/11/03.
 */
public abstract class InjectActivity extends AppCompatActivity {

    private ActivityComponent mActivityComponent;

    protected abstract void injectDependency(ActivityComponent component);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityComponent = DaggerActivityComponent.builder()
                .applicationComponent(((Application)getApplication()).getApplicationComponent())
                .activityModule(new ActivityModule(this))
                .build();

        injectDependency(mActivityComponent);
    }
}
