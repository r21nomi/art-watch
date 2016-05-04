package com.nomi.artwatch.data.entity;

import android.content.ContentValues;
import android.database.Cursor;

import com.nomi.artwatch.data.Db;
import com.tumblr.jumblr.types.PhotoSize;

/**
 * Created by Ryota Niinomi on 2016/04/18.
 */
public abstract class PhotoSizeEntity {

    public static final String TABLE = "photo_size_entity";
    public static final String URL = "url";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String UPDATED_AT = "updated_at";

    public static PhotoSize toPhotoSize(Cursor cursor) {
        String url = Db.getString(cursor, URL);
        int width = Db.getInt(cursor, WIDTH);
        int height = Db.getInt(cursor, HEIGHT);
        return new PhotoSize(url, width, height);
    }

    public static final class Builder {
        private final ContentValues values = new ContentValues();

        public Builder url(String url) {
            values.put(URL, url);
            return this;
        }

        public Builder width(int width) {
            values.put(WIDTH, width);
            return this;
        }

        public Builder height(int height) {
            values.put(HEIGHT, height);
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
