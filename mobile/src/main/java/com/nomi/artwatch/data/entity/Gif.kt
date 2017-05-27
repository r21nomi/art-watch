package com.nomi.artwatch.data.entity

import com.tumblr.jumblr.types.PhotoSize

/**
 * Created by Ryota Niinomi on 2016/05/05.
 */
data class Gif(
        val originalGifUrl: String,
        val photoSizes: List<PhotoSize>,
        val caption: String
)