package com.nomi.artwatch.data.entity;

import com.tumblr.jumblr.types.PhotoSize;

import java.util.List;

/**
 * Created by Ryota Niinomi on 2016/05/05.
 */
public class Gif {

    private String caption;
    private String originalGifUrl;
    private List<PhotoSize> photoSizes;

    public Gif(String originalGifUrl, List<PhotoSize> photoSizes, String caption) {
        this.originalGifUrl = originalGifUrl;
        this.photoSizes = photoSizes;
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public String getOriginalGifUrl() {
        return originalGifUrl;
    }

    public List<PhotoSize> getPhotoSizes() {
        return photoSizes;
    }
}