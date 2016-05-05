package com.nomi.artwatch.data.cache;

import android.content.ContentValues;
import android.database.Cursor;

import com.nomi.artwatch.data.Db;
import com.nomi.artwatch.data.entity.Gif;
import com.nomi.artwatch.util.JsonUtil;
import com.tumblr.jumblr.types.PhotoSize;

import java.util.List;

/**
 * Created by Ryota Niinomi on 2016/05/05.
 */
public abstract class GifCache {

    public static final String TABLE = "gif_cache";
    public static final String CAPTION = "caption";
    public static final String ORIGINAL_GIF_URL = "original_gif_url";
    public static final String PHOTO_SIZES = "photo_sizes";
    public static final String UPDATED_AT = "updated_at";

    public static Gif toGif(Cursor cursor) {
        String caption = Db.getString(cursor, CAPTION);
        String originalGifUrl = Db.getString(cursor, ORIGINAL_GIF_URL);
        String stringifiedPhotoSizes = Db.getString(cursor, PHOTO_SIZES);
        List<PhotoSize> photoSizes = JsonUtil.toList(stringifiedPhotoSizes, PhotoSize.class);
        return new Gif(originalGifUrl, photoSizes, caption);
    }

    public static final class Builder {
        private final ContentValues values = new ContentValues();

        public Builder caption(String caption) {
            values.put(CAPTION, caption);
            return this;
        }

        public Builder originalGifUrl(String originalGifUrl) {
            values.put(ORIGINAL_GIF_URL, originalGifUrl);
            return this;
        }

        public Builder photoSizes(List<PhotoSize> photoSizes) {
            values.put(PHOTO_SIZES, JsonUtil.toJsonArrayString(photoSizes, PhotoSize.class));
            return this;
        }

        public Builder updatedAt(long updatedAt) {
            values.put(UPDATED_AT, updatedAt);
            return this;
        }

        public ContentValues build() {
            return values;
        }
    }
}
