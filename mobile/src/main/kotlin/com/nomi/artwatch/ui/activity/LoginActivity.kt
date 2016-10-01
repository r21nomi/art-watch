package com.nomi.artwatch.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import butterknife.bindView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.nomi.artwatch.R
import com.nomi.artwatch.di.component.ActivityComponent
import com.nomi.artwatch.model.LoginModel
import com.nomi.artwatch.ui.util.SnackbarUtil
import pl.droidsonroids.gif.GifImageView
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Ryota Niinomi on 16/03/15.
 */
class LoginActivity : InjectActivity() {

    companion object {
        val MESSAGE: String = "message"
        fun createIntentWithMessage(context: Context, message: String): Intent {
            val intent = Intent(context, LoginActivity::class.java)
            intent.putExtra(MESSAGE, message)
            return intent
        }
        fun createIntent(context: Context): Intent {
            val intent = Intent(context, LoginActivity::class.java)
            return intent
        }
    }

    @Inject
    lateinit var mLoginModel: LoginModel

    val mGifView : GifImageView by bindView(R.id.gifView)
    val mLoginText : TextView by bindView(R.id.loginText)

    override fun injectDependency(component: ActivityComponent) {
        component.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        Glide.with(this).load(R.raw.gen_art_1).asGif().into(object : SimpleTarget<GifDrawable>() {
            override fun onResourceReady(resource: GifDrawable, glideAnimation: GlideAnimation<in GifDrawable>) {
                mGifView.setImageDrawable(resource)
                resource.start()
            }
        })

        mLoginText.setOnClickListener({
            authorize()
        })

        if (intent.hasExtra(MESSAGE)) {
            SnackbarUtil.showAlert(this, intent.getStringExtra(MESSAGE))
        }
    }

    private fun authorize() {
        val subscription = mLoginModel
                .login()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ authUrl ->
                    Timber.d("Auth URL : " + authUrl)
                    startActivity(Intent("android.intent.action.VIEW", Uri.parse(authUrl)))

                }, { throwable ->
                    Timber.e(throwable, throwable.message)
                })
        mSubscriptionsOnDestroy.add(subscription)
    }
}