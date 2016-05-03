package com.nomi.artwatch.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.wearable.companion.WatchFaceCompanion
import com.nomi.artwatch.Application
import com.nomi.artwatch.R
import com.nomi.artwatch.di.component.ActivityComponent
import com.nomi.artwatch.model.LoginModel
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Ryota Niinomi on 16/03/15.
 */
class SplashActivity : InjectActivity() {

    @Inject
    lateinit var mLoginModel: LoginModel

    override fun injectDependency(component: ActivityComponent) {
        component.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Application.sPeerId = intent.getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID)

        val subscription = setToken()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({aVoid ->
                    if (mLoginModel.isAuthorized) {
                        moveToMain()
                    } else {
                        moveToLogin()
                    }
                }, {throwable ->
                    Timber.e(throwable.message, throwable)
                })
        mSubscriptionsOnDestroy.add(subscription)
    }

    /**
     * Set token and token secret.
     */
    private fun setToken(): Observable<Void> {
        val uri = intent.data
        if (uri != null) {
            return mLoginModel.saveToken(uri)

        } else {
            return Observable.just(null)
        }
    }

    private fun moveToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun moveToMain() {
        val intent = MainActivity.createIntent(this)
        startActivity(intent)
        finish();
    }
}