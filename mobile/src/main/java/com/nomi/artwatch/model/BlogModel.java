package com.nomi.artwatch.model;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by Ryota Niinomi on 2016/03/23.
 */
@Singleton
public class BlogModel extends BaseModel {

    @Inject
    public BlogModel(PrefModel prefModel) {
        super(prefModel);
    }

    public Observable<String> getAvatar(String blogName) {
        return Observable
                .just(null)
                .subscribeOn(Schedulers.io())
                .map(aVoid -> {
                    // FIXME: Use this method (now this will throw error).
//                    return getClient().blogAvatar(blogName);
                    return "https://api.tumblr.com/v2/blog/" + blogName + ".tumblr.com/avatar";
                })
                .onErrorResumeNext(throwable -> {
                    return Observable.just("");
                });
    }
}
