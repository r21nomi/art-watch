package com.nomi.artwatch.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import java.io.IOException;

/**
 * Created by Ryota Niinomi on 15/10/13.
 */
public class GifView extends View {

    private static final int DEFAULT_MOVIE_DURATION = 1000;
    private Movie mMovie;
    private long mMovieStart;
    private int mCurrentAnimationTime = 0;

    public GifView(Context context) throws IOException {
        this(context, null);
    }

    public GifView(Context context, AttributeSet attrs) throws IOException {
        this(context, attrs, 0);
    }

    public GifView(Context context, AttributeSet attrs, int defStyle) throws IOException {
        super(context, attrs, defStyle);
        mMovie = Movie.decodeStream(getResources().getAssets().open("image_1.gif"));

        setViewAttributes();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint p = new Paint();
        p.setAntiAlias(true);

        updateAnimationTime();
        drawMovieFrame(canvas);
        invalidateView();
    }

    private void updateAnimationTime() {
        long now = android.os.SystemClock.uptimeMillis();
        int dur = mMovie.duration();

        if (mMovieStart == 0) {
            mMovieStart = now;
        }
        if (dur == 0) {
            dur = DEFAULT_MOVIE_DURATION;
        }

        mCurrentAnimationTime = (int) ((now - mMovieStart) % dur);
    }

    private void drawMovieFrame(Canvas canvas) {
        mMovie.setTime(mCurrentAnimationTime);
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        mMovie.draw(canvas, 0, 0);
        canvas.restore();
    }

    @SuppressLint("NewApi")
    private void invalidateView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            postInvalidateOnAnimation();
        } else {
            invalidate();
        }
    }

    @SuppressLint("NewApi")
    private void setViewAttributes() {
        // これがないとAndroid 6.0でアニメーションされなかった
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }
}
