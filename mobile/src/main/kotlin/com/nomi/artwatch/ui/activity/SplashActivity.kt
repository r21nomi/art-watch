package com.nomi.artwatch.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.wearable.companion.WatchFaceCompanion
import com.nomi.artwatch.Application
import com.nomi.artwatch.R
import com.nomi.artwatch.di.component.ActivityComponent
import com.nomi.artwatch.model.LoginModel
import com.nomi.artwatch.ui.util.DeepLinkRouter
import com.nomi.artwatch.util.StringUtil
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

        if (intent.hasExtra(WatchFaceCompanion.EXTRA_PEER_ID)) {
            Application.setPeerId(intent.getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID))
        }

        Timber.d("Application.sPeerId : " + Application.sPeerId)

        val subscription = setToken()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ aVoid ->
                    if (mLoginModel.isAuthorized) {
                        val data = intent.data
                        if (data != null && StringUtil.isNotEmpty(data.host)) {
                            // Deep link
                            startDeepLink(data)
                        } else {
                            // To Main
                            moveToMain()
                        }
                    } else {
                        // Login
                        moveToLogin()
                    }
                }, { throwable ->
                    Timber.e(throwable, throwable.message)
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
        finish()
    }

    private fun startDeepLink(uri: Uri) {
        val subscription = DeepLinkRouter
                .createIntent(this, uri)
                .subscribe({
                    intent -> startActivity(intent)
                }, {
                    throwable -> Timber.e(throwable, throwable.message)
                })
        mSubscriptionsOnDestroy.add(subscription)
    }
}