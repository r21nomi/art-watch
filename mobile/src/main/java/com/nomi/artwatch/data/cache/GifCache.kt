package com.nomi.artwatch.data.cache

import android.content.ContentValues
import android.database.Cursor

import com.nomi.artwatch.data.Db
import com.nomi.artwatch.data.entity.Gif
import com.nomi.artwatch.util.JsonUtil
import com.tumblr.jumblr.types.PhotoSize

/**
 * Created by Ryota Niinomi on 2016/05/05.
 */
object GifCache {

    val TABLE = "gif_cache"
    val CAPTION = "caption"
    val ORIGINAL_GIF_URL = "original_gif_url"
    val PHOTO_SIZES = "photo_sizes"
    val UPDATED_AT = "updated_at"

    fun toGif(cursor: Cursor): Gif {
        val caption = Db.getString(cursor, CAPTION)
        val originalGifUrl = Db.getString(cursor, ORIGINAL_GIF_URL)
        val stringifiedPhotoSizes = Db.getString(cursor, PHOTO_SIZES)
        val photoSizes = JsonUtil.toList(stringifiedPhotoSizes, PhotoSize::class.java)
        return Gif(originalGifUrl, photoSizes, caption)
    }

    class Builder {
        private val values = ContentValues()

        fun caption(caption: String): Builder {
            values.put(CAPTION, caption)
            return this
        }

        fun originalGifUrl(originalGifUrl: String): Builder {
            values.put(ORIGINAL_GIF_URL, originalGifUrl)
            return this
        }

        fun photoSizes(photoSizes: List<PhotoSize>): Builder {
            values.put(PHOTO_SIZES, JsonUtil.toJsonArrayString(photoSizes, PhotoSize::class.java))
            return this
        }

        fun updatedAt(updatedAt: Long): Builder {
            values.put(UPDATED_AT, updatedAt)
            return this
        }

        fun build(): ContentValues {
            return values
        }
    }
}
