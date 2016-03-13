package com.nomi.artwatch.model;

import com.nomi.artwatch.Config;
import com.nomi.artwatch.util.StringUtil;

import javax.inject.Inject;
import javax.inject.Singleton;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
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
    private static final String CALLBACK_URL = "artwatch://com.nomi.artwatch";

    private PrefModel mPrefModel;
    private CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(
            Config.CONSUMER_KEY,
            Config.CONSUMER_SECRET);
    private CommonsHttpOAuthProvider provider = new CommonsHttpOAuthProvider(
            REQUEST_TOKEN_URL,
            ACCESS_TOKEN_URL,
            AUTH_URL);

    @Inject
    public LoginModel(PrefModel prefModel) {
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
                .map(aVoid -> {
                    String authUrl = "";
                    try {
                        authUrl = provider.retrieveRequestToken(consumer, CALLBACK_URL);

                        mPrefModel.setTokens(consumer.getToken(), consumer.getTokenSecret());
                    } catch (OAuthMessageSignerException e) {
                        e.printStackTrace();
                    } catch (OAuthNotAuthorizedException e) {
                        e.printStackTrace();
                    } catch (OAuthExpectationFailedException e) {
                        e.printStackTrace();
                    } catch (OAuthCommunicationException e) {
                        e.printStackTrace();
                    }
                    return authUrl;
                });
    }
}
