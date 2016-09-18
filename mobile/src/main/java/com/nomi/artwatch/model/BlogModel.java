package com.nomi.artwatch.model;

import android.support.annotation.Nullable;

import com.nomi.artwatch.util.StringUtil;
import com.tumblr.jumblr.types.Blog;
import com.tumblr.jumblr.types.User;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by Ryota Niinomi on 2016/03/23.
 */
@Singleton
public class BlogModel extends BaseModel {

    private Blog mCurrentBlog;

    @Inject
    public BlogModel(PrefModel prefModel) {
        super(prefModel);
    }

    public Observable<String> getAvatar(String blogName) {
        return Observable
                .fromCallable(() -> {
                    if (StringUtil.isNotBlank(blogName)) {
                        // This will throw an error.
//                        return getClient().blogAvatar(blogName);
                        // FIXME：Sometimes this throw 401.
                        return "https://api.tumblr.com/v2/blog/" + blogName + ".tumblr.com/avatar";
                    } else {
                        return "";
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    public Blog getCurrentBlog() {
        return mCurrentBlog;
    }

    @Nullable
    public Blog getBlogByUserOrCurrent(User user) {
        Blog currentBlog = getCurrentBlog();

        if (currentBlog != null) {
            return currentBlog;

        } else if (!user.getBlogs().isEmpty()) {
            return user.getBlogs().get(0);

        } else {
            return null;
        }
    }

    public void setCurrentBlog(Blog blog) {
        this.mCurrentBlog = blog;
    }
}
