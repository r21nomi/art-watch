package com.nomi.artwatch;

import com.tumblr.jumblr.types.PhotoSize;

import java.util.List;

/**
 * Created by Ryota Niinomi on 2016/05/05.
 */
public class GifUrlProvider {

    public enum Type {
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
    public static String getUrl(List<PhotoSize> photoSizes, Type type) {
        int index = 0;

        switch (type) {
            case WEAR:
                if (photoSizes.size() > 1) {
                    index = photoSizes.size() - 2;
                }
                break;

            default:
                if (photoSizes.size() > 2) {
                    // If you set index = 1(size = 400), the animation of gif will be slow.
                    index = 2;
                }
                break;
        }

        return photoSizes.get(index).getUrl();
    }
}
