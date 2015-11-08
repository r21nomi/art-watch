package com.nomi.artwatch.model;

import com.tumblr.jumblr.types.Blog;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.User;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.AsyncSubject;
import timber.log.Timber;

/**
 * Created by Ryota Niinomi on 2015/11/03.
 */
@Singleton
public class UserModel extends BaseModel {

    @Inject
    public UserModel() {

    }

    public Observable<User> getUser() {
        AsyncSubject<User> subject = AsyncSubject.create();

        Observable
                .just(null)
                .subscribeOn(Schedulers.io())
                .subscribe(aVoid -> {
                    User user = getClient().user();

                    Timber.d("name : " + user.getName());

                    List<Blog> blog = user.getBlogs();

                    Observable
                            .from(blog)
                            .forEach(item -> {
                                Timber.d("Blog name : %1$s, Title : %2$s, Count : %3$s",
                                        item.getName(),
                                        item.getTitle(),
                                        item.getPostCount());

                                posts(item);
                            });

                    subject.onNext(user);

                }, throwable -> {
                    Timber.w(throwable, throwable.getLocalizedMessage());
                    subject.onError(throwable);

                }, subject::onCompleted);

        return subject;
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
