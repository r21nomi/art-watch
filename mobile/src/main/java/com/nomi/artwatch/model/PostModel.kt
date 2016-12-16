package com.nomi.artwatch.model

import android.content.Context
import com.nomi.artwatch.R
import com.tumblr.jumblr.types.Blog
import com.tumblr.jumblr.types.Photo
import com.tumblr.jumblr.types.PhotoPost
import rx.Observable
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Ryota Niinomi on 2015/11/08.
 */
@Singleton
class PostModel
@Inject
constructor(private val mContext: Context, prefModel: PrefModel) : BaseModel(prefModel) {

    fun getBlog(blogName: String): Observable<Blog> = Observable
            .just(null)
            .subscribeOn(Schedulers.io())
            .map { return@map client.blogInfo(blogName) }

    fun getPost(blogName: String): Observable<Void> = Observable
            .just(null)
            .subscribeOn(Schedulers.io())
            .map{
                client.blogPosts(blogName)
                return@map null
            }

    fun getPhotoPost(blogName: String, offset: Int, limit: Int): Observable<List<Photo>> = Observable
            .just(null)
            .subscribeOn(Schedulers.io())
            .map {
                val param = mapOf(
                        "offset" to offset,
                        "limit" to limit
                )
                val photoPosts = client.photos(blogName, param)
                val photos = toPhotos(photoPosts, limit)
                return@map photos
            }
            .onErrorReturn {
                Timber.e(it, it.message)
                throw RuntimeException(mContext.getString(R.string.error_common))
            }

    private fun toPhotos(photoPosts: List<PhotoPost>, limit: Int): List<Photo> {
        val photos = ArrayList<Photo>()

        Observable
                .from(photoPosts)
                .forEach { item ->
                    Observable
                            .from(item.photos)
                            .filter { item2 -> photos.size < limit && isGif(item2) }
                            .forEach { item3 ->
                                Timber.d("photo url : " + item3.originalSize.url)
                                photos.add(item3)
                            }
                }

        return photos
    }

    private fun isGif(photo: Photo): Boolean {
        return photo.originalSize.url.contains(".gif")
    }
}
