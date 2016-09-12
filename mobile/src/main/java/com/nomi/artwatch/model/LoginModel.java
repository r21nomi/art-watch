package com.nomi.artwatch.model;

import android.content.Context;
import android.net.Uri;

import com.nomi.artwatch.Application;
import com.nomi.artwatch.Config;
import com.nomi.artwatch.R;
import com.nomi.artwatch.ui.util.DeepLinkRouter;
import com.nomi.artwatch.util.StringUtil;

import javax.inject.Inject;
import javax.inject.Singleton;

import oauth.signpost.OAuth;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import rx.Observable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by Ryota Niinomi on 15/12/06.
 */
@Singleton
public class LoginModel {

    private static final String REQUEST_TOKEN_URL = "https://www.tumblr.com/oauth/request_token";
    private static final String ACCESS_TOKEN_URL = "https://www.tumblr.com/oauth/access_token";
    private static final String AUTH_URL = "https://www.tumblr.com/oauth/authorize";

    private Context mContext;
    private PrefModel mPrefModel;
    private CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(
            Config.CONSUMER_KEY,
            Config.CONSUMER_SECRET);
    private CommonsHttpOAuthProvider provider = new CommonsHttpOAuthProvider(
            REQUEST_TOKEN_URL,
            ACCESS_TOKEN_URL,
            AUTH_URL);

    @Inject
    public LoginModel(Context context, PrefModel prefModel) {
        mContext = context;
        mPrefModel = prefModel;
    }

    public boolean isAuthorized() {
        Timber.d("token : %s", mPrefModel.getToken());
        Timber.d("token secret : %s", mPrefModel.getTokenSecret());

        return StringUtil.isNotBlank(mPrefModel.getToken())
                && StringUtil.isNotBlank(mPrefModel.getTokenSecret());
    }

    public String getToken() {
        return mPrefModel.getToken();
    }

    public String getTokenSecret() {
        return mPrefModel.getTokenSecret();
    }

    public Observable<String> login() {
        return Observable
                .just(null)
                .subscribeOn(Schedulers.io())
                .flatMap(aVoid -> {
                    String authUrl = "";
                    try {
                        authUrl = provider.retrieveRequestToken(consumer, getCallbackUrl());

                    } catch (Throwable throwable) {
                        Timber.e(throwable, throwable.getLocalizedMessage());
                        return Observable.error(throwable);
                    }
                    return Observable.just(authUrl);
                });
    }

    public Observable<Void> saveToken(Uri uri) {
        return Observable
                .just(null)
                .subscribeOn(Schedulers.io())
                .flatMap(aVoid -> {
                    if (!uri.toString().startsWith(getCallbackUrl())) {
                        return Observable.error(new IllegalArgumentException("The callback should be started from " + getCallbackUrl()));
                    }
                    try {
                        String oauthVerifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
                        provider.retrieveAccessToken(consumer, oauthVerifier);
                        mPrefModel.setTokens(consumer.getToken(), consumer.getTokenSecret());

                    } catch (Throwable throwable) {
                        Timber.e(throwable, throwable.getLocalizedMessage());
                        return Observable.error(throwable);
                    }
                    return Observable.just(null);
                });
    }

    public Observable<Void> logout() {
        mPrefModel.clear();
        return Observable.just(null);
    }

    private String getCallbackUrl() {
        String url = mContext.getString(R.string.scheme)
                + "://"
                + DeepLinkRouter.Companion.getLOGIN()
                + "/"
                + Application.sPeerId;

        Timber.d("getCallbackUrl : %s", url);

        return url;
    }
}
