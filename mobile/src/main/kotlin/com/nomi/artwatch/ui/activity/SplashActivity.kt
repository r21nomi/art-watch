package com.nomi.artwatch.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.wearable.companion.WatchFaceCompanion
import com.nomi.artwatch.R
import com.nomi.artwatch.di.component.ActivityComponent
import com.nomi.artwatch.model.LoginModel
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

        if (mLoginModel.isAuthorized) {
            moveToMain()
        } else {
            moveToLogin()
        }
    }

    private fun moveToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun moveToMain() {
        val intent = MainActivity.createIntent(this, intent.getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID))
        startActivity(intent)
        finish();
    }
}