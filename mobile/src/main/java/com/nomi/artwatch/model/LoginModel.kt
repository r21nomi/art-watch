package com.nomi.artwatch.model

import android.content.Context
import android.net.Uri
import com.nomi.artwatch.Config
import com.nomi.artwatch.R
import com.nomi.artwatch.ui.util.DeepLinkRouter
import oauth.signpost.OAuth
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider
import rx.Observable
import rx.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Ryota Niinomi on 15/12/06.
 */
@Singleton
class LoginModel
@Inject
constructor(private val mContext: Context, private val mPrefModel: PrefModel) {

    companion object {
        private val REQUEST_TOKEN_URL = "https://www.tumblr.com/oauth/request_token"
        private val ACCESS_TOKEN_URL = "https://www.tumblr.com/oauth/access_token"
        private val AUTH_URL = "https://www.tumblr.com/oauth/authorize"
    }

    private val consumer = CommonsHttpOAuthConsumer(
            Config.CONSUMER_KEY,
            Config.CONSUMER_SECRET
    )

    private val provider = CommonsHttpOAuthProvider(
            REQUEST_TOKEN_URL,
            ACCESS_TOKEN_URL,
            AUTH_URL
    )

    private val callbackUrl: String
        get() {
            val url = mContext.getString(R.string.scheme) + "://" + DeepLinkRouter.LOGIN

            Timber.d("getCallbackUrl : %s", url)

            return url
        }

    val isAuthorized: Boolean
        get() {
            Timber.d("token : %s", mPrefModel.token)
            Timber.d("token secret : %s", mPrefModel.tokenSecret)

            return mPrefModel.token.isNotBlank() && mPrefModel.tokenSecret.isNotBlank()
        }

    fun login(): Observable<String> {
        return Observable
                .just(null)
                .subscribeOn(Schedulers.io())
                .flatMap {
                    var authUrl = ""
                    try {
                        authUrl = provider.retrieveRequestToken(consumer, callbackUrl)

                    } catch (throwable: Throwable) {
                        Timber.e(throwable, throwable.message)
                        throw RuntimeException(mContext.getString(R.string.error_common))
                    }

                    Observable.just(authUrl)
                }
                .onErrorReturn {
                    Timber.e(it, it.message)
                    throw RuntimeException(mContext.getString(R.string.error_common))
                }
    }

    fun saveToken(uri: Uri): Observable<Void> = Observable
            .just(null)
            .subscribeOn(Schedulers.io())
            .flatMap {
                if (!uri.toString().startsWith(callbackUrl)) {
                    throw IllegalArgumentException("The callback should be started from " + callbackUrl)
                }
                try {
                    val oauthVerifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER)
                    provider.retrieveAccessToken(consumer, oauthVerifier)
                    mPrefModel.setTokens(consumer.token, consumer.tokenSecret)

                } catch (throwable: Throwable) {
                    Timber.e(throwable, throwable.message)
                    throw RuntimeException(mContext.getString(R.string.error_common))
                }

                Observable.just<Void>(null)
            }
            .onErrorReturn {
                Timber.e(it, it.message)
                throw RuntimeException(mContext.getString(R.string.error_common))
            }

    fun logout(): Observable<Void> {
        mPrefModel.clear()
        return Observable.just<Void>(null)
    }
}
