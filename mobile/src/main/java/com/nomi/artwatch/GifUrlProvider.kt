package com.nomi.artwatch

import com.tumblr.jumblr.types.PhotoSize

/**
 * Created by Ryota Niinomi on 2016/05/05.
 */
object GifUrlProvider {

    enum class Type {
        MOBILE,
        WEAR
    }

    /**
     * Get gif url suit for the device which the gif image is shown.
     *
     * 0: size = 500
     * 1: size = 400
     * 2: size = 250
     * 3: size = 100
     * 4: size = 75
     *
     * @param photoSizes
     * @param type
     * @return
     */
    fun getUrl(photoSizes: List<PhotoSize>, type: Type): String {
        var index = 0

        when (type) {
            Type.WEAR -> if (photoSizes.size > 1) {
                index = photoSizes.size - 2
            }

            else -> if (photoSizes.size > 2) {
                // If you set index = 1(size = 400), the animation of gif will be slow.
                index = 2
            }
        }

        return photoSizes[index].url
    }
}
