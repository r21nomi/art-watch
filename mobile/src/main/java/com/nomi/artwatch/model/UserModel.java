package com.nomi.artwatch.model;

import com.tumblr.jumblr.types.Blog;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.User;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by Ryota Niinomi on 2015/11/03.
 */
@Singleton
public class UserModel extends BaseModel {

    @Inject
    public UserModel(PrefModel prefModel) {
        super(prefModel);
    }

    public Observable<User> getUser() {
        return Observable
                .just(null)
                .subscribeOn(Schedulers.io())
                .map(aVoid -> getClient().user());
    }

    private void posts(Blog blog) {
        List<Post> posts = blog.posts();

        Observable
                .from(posts)
                .forEach(item -> {
                    Timber.d("Post Url : %1$s, Short Url : %2$s, Source Url : %3$s, Type : %4$s, Slug : %5$s",
                            item.getPostUrl(),
                            item.getShortUrl(),
                            item.getSourceUrl(),
                            item.getType(),
                            item.getSlug());
                });
    }
}
