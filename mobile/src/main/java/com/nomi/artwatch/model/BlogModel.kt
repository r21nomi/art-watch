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
 * Created by Ryota Niinomi on 2016/03/23.
 */
@Singleton
class BlogModel
@Inject
constructor(private val mContext: Context, prefModel: PrefModel) : BaseModel(prefModel) {

    var currentBlog: Blog? = null

    fun getAvatar(blogName: String): Observable<String> = Observable
            .fromCallable<String> {
                if (blogName.isNotBlank()) {
                    // This will throw an error.
                    //                        return getClient().blogAvatar(blogName);
                    // FIXME：Sometimes this throw 401.
                    return@fromCallable "https://api.tumblr.com/v2/blog/" + blogName + ".tumblr.com/avatar"
                } else {
                    return@fromCallable ""
                }
            }
            .onErrorReturn {
                Timber.e(it, it.message)
                throw RuntimeException(mContext.getString(R.string.error_common))
            }
            .subscribeOn(Schedulers.io())

    fun getBlogByUserOrCurrent(user: User): Blog? {
        val currentBlog = currentBlog

        if (currentBlog != null) {
            return currentBlog

        } else if (!user.blogs.isEmpty()) {
            return user.blogs[0]

        } else {
            return null
        }
    }
}
