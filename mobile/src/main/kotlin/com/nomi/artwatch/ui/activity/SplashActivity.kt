package com.nomi.artwatch.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.nomi.artwatch.R
import com.nomi.artwatch.di.component.ActivityComponent
import com.nomi.artwatch.model.LoginModel
import com.nomi.artwatch.ui.util.DeepLinkRouter
import com.nomi.artwatch.ui.util.SnackbarUtil
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

    lateinit var mMainIntent: Intent

    override fun injectDependency(component: ActivityComponent) {
        component.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // FIXME：DeepLinkRouter内でIntentを作成すると、たまにNoClassDefFoundErrorが発生するので
        // MainActivityの起動Intentはここで作成する。
        mMainIntent = MainActivity.createIntent(this)

        setToken()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
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
                }, {
                    SnackbarUtil.showAlert(this, it.message ?: return@subscribe)
                    moveToLoginWithMessage(getString(R.string.error_failed_to_login))
                })
                .apply { mSubscriptionsOnDestroy.add(this) }
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        finish()
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
        val intent = LoginActivity.createIntent(this)
        startActivity(intent)
    }

    private fun moveToLoginWithMessage(message: String) {
        val intent = LoginActivity.createIntentWithMessage(this, message)
        startActivity(intent)
    }

    private fun moveToMain() {
        val intent = MainActivity.createIntent(this)
        startActivity(intent)
    }

    private fun startDeepLink(uri: Uri) {
        DeepLinkRouter
                .createIntent(mMainIntent, uri)
                .subscribe({
                    startActivity(it)
                }, {
                    Timber.e(it, it.message)
                })
                .apply { mSubscriptionsOnDestroy.add(this) }
    }
}