package com.nomi.artwatch.model

import android.content.Context
import com.nomi.artwatch.R
import com.tumblr.jumblr.types.Blog
import com.tumblr.jumblr.types.User
import rx.Observable
import rx.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Ryota Niinomi on 2015/11/03.
 */
@Singleton
class UserModel
@Inject
constructor(private val mContext: Context, prefModel: PrefModel) : BaseModel(prefModel) {

    val user: Observable<User> = Observable
            .just(null)
            .subscribeOn(Schedulers.io())
            .map { client.user() }
            .onErrorReturn {
                Timber.e(it, it.message)
                throw RuntimeException(mContext.getString(R.string.error_common))
            }

    private fun posts(blog: Blog) {
        val posts = blog.posts()

        Observable
                .from(posts)
                .forEach { item ->
                    Timber.d("Post Url : %1\$s, Short Url : %2\$s, Source Url : %3\$s, Type : %4\$s, Slug : %5\$s",
                            item.postUrl,
                            item.shortUrl,
                            item.sourceUrl,
                            item.type,
                            item.slug)
                }
    }
}
