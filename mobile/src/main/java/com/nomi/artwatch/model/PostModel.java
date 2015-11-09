package com.nomi.artwatch.model;

import com.tumblr.jumblr.types.Blog;
import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.Post;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.AsyncSubject;
import timber.log.Timber;

/**
 * Created by Ryota Niinomi on 2015/11/08.
 */
@Singleton
public class PostModel extends BaseModel {

    private static final int LIMIT = 40;

    @Inject
    public PostModel() {

    }

    public Observable<Blog> getBlog(String blogName) {
        AsyncSubject<Blog> subject = AsyncSubject.create();

        Observable
                .just(null)
                .subscribeOn(Schedulers.io())
                .subscribe(aVoid -> {
                    Blog blog = getClient().blogInfo(blogName);

                    subject.onNext(blog);

                }, throwable -> {
                    Timber.w(throwable, throwable.getLocalizedMessage());
                    subject.onError(throwable);

                }, subject::onCompleted);

        return subject;
    }

    public Observable<Void> getPost(String blogName) {
        AsyncSubject<Void> subject = AsyncSubject.create();

        Observable
                .just(null)
                .subscribeOn(Schedulers.io())
                .subscribe(aVoid -> {
                    List<Post> blogPosts = getClient().blogPosts(blogName);

                    subject.onNext(null);

                }, throwable -> {
                    Timber.w(throwable, throwable.getLocalizedMessage());
                    subject.onError(throwable);

                }, subject::onCompleted);

        return subject;
    }

    public Observable<List<Photo>> getPhotoPost(String blogName) {
        AsyncSubject<List<Photo>> subject = AsyncSubject.create();

        Observable
                .just(null)
                .subscribeOn(Schedulers.io())
                .subscribe(aVoid -> {
                    List<PhotoPost> photoPosts = getClient ().photos(blogName, null);
                    List<Photo> photos = toPhotos(photoPosts);

                    subject.onNext(photos);

                }, throwable -> {
                    Timber.w(throwable, throwable.getLocalizedMessage());
                    subject.onError(throwable);

                }, subject::onCompleted);

        return subject;
    }

    private List<Photo> toPhotos(List<PhotoPost> photoPosts) {
        List<Photo> photos = new ArrayList<>();

        Observable
                .from(photoPosts)
                .forEach(item -> {
                    Observable
                            .from(item.getPhotos())
                            .filter(item2 -> photos.size() < LIMIT && isGif(item2))
                            .forEach(item3 -> {
                                Timber.d("photo url : " + item3.getOriginalSize().getUrl());
                                photos.add(item3);
                            });
                });

        return photos;
    }

    private boolean isGif(Photo photo) {
        return photo.getOriginalSize().getUrl().contains(".gif");
    }
}
