package com.nomi.artwatch;

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
public class GIFView2 extends View {

    private static final int DEFAULT_MOVIE_DURATION = 1000;
    private Movie mMovie;
    private long mMovieStart;
    private int mCurrentAnimationTime = 0;

    public GIFView2(Context context) throws IOException {
        this(context, null);
    }

    public GIFView2(Context context, AttributeSet attrs) throws IOException {
        this(context, attrs, 0);
    }

    public GIFView2(Context context, AttributeSet attrs, int defStyle) throws IOException {
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
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//        }
    }
}

//public class GIFView2
//        extends View {
//    private static final String TAG = "gif_view";
//    private long DRAW_SLEEP = 100L;
//    private Context context;
//    private Movie currentMovie;
//    private int drawCount;
//    private boolean isDimmed = false;
//    private boolean isFullscreen;
//    private long movieCursor;
//    private long movieStart;
//    private Movie overlay;
//
//    public GIFView2(Context paramContext) {
//        super(paramContext);
//        init(paramContext);
//    }
//
//    public GIFView2(Context paramContext, AttributeSet paramAttributeSet) {
//        super(paramContext, paramAttributeSet);
//        init(paramContext);
//    }
//
//    public GIFView2(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
//        super(paramContext, paramAttributeSet, paramInt);
//        init(paramContext);
//    }
//
//    public void init(Context paramContext) {
//        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//        setFullscreen(true);
//        this.context = paramContext;
//    }
//
//    public boolean isDimmed() {
//        return this.isDimmed;
//    }
//
//    public boolean isFullscreen() {
//        return this.isFullscreen;
//    }
//
//    protected void onDraw(Canvas paramCanvas) {
//        Object localObject = new Paint();
//        ((Paint) localObject).setAntiAlias(true);
//        paramCanvas.drawColor(-1);
//        long l = SystemClock.uptimeMillis();
////        ((Paint)localObject).setColor(-16777216);
//        paramCanvas.drawRect(new Rect(0, 0, paramCanvas.getWidth(), paramCanvas.getHeight()), (Paint) localObject);
//        localObject = this.currentMovie;
//        if (this.overlay != null) {
//            localObject = this.overlay;
//        }
//        if ((!this.isDimmed) && (localObject != null) && (((Movie) localObject).duration() > 0)) {
//            if (this.movieStart == 0L) {
//                this.movieStart = l;
//            }
//            float f1 = 1.0F;
//            float f2 = f1;
//            if (isFullscreen()) {
//                if ((((Movie) localObject).height() < paramCanvas.getHeight()) || (((Movie) localObject).width() < paramCanvas.getWidth())) {
//                    f1 = Math.max(paramCanvas.getHeight() / ((Movie) localObject).height(), paramCanvas.getWidth() / ((Movie) localObject).width());
//                }
//                paramCanvas.scale(f1, f1);
//                f2 = f1;
//            }
//            paramCanvas.translate((paramCanvas.getWidth() / f2 - ((Movie) localObject).width()) / 2.0F, (paramCanvas.getHeight() / f2 - ((Movie) localObject).height()) / 2.0F);
//            if (this.overlay == null) {
////                break label333;
//            }
//            l = Math.min(((Movie) localObject).duration(), l - this.movieStart);
//        }
////        for (; ; ) {
////        if (localObject != null) {
//            if (l != this.movieCursor) {
//                this.movieCursor = l;
//                ((Movie) localObject).setTime((int) this.movieCursor);
//                ((Movie) localObject).draw(paramCanvas, 0, 0);
//                invalidate();
//                if ((this.overlay != null) && (l >= ((Movie) localObject).duration())) {
////                    this.overlay = null;
//                }
//            }
//            try {
//                Thread.currentThread();
//                Thread.sleep(this.DRAW_SLEEP);
//                this.drawCount += 1;
//                super.onDraw(paramCanvas);
////                return;
////                label333:
//                l = (int) ((l - this.movieStart) % ((Movie) localObject).duration());
//            } catch (InterruptedException localInterruptedException) {
////                for (; ; ) {
////                    localInterruptedException.printStackTrace();
////                }
//            }
////        }
//    }
//
//    public void setDimmed(boolean paramBoolean) {
//        this.isDimmed = paramBoolean;
//        if (paramBoolean) {
//            this.currentMovie = null;
//            System.gc();
//        }
//    }
//
//    public void setFullscreen(boolean paramBoolean) {
//        this.isFullscreen = paramBoolean;
//    }
//
//    public void setMovie(Movie paramMovie, boolean paramBoolean) {
//        Log.i(TAG, "Last draw count is " + this.drawCount);
//        if (paramBoolean) {
//            this.overlay = paramMovie;
//        }
////        for (; ; ) {
//            this.movieStart = 0L;
//            this.movieCursor = -1L;
//            this.drawCount = 0;
//            invalidate();
////            return;
//            this.currentMovie = paramMovie;
////        }
//    }
//
////    public void setMovieFromData(byte[] paramArrayOfByte, boolean paramBoolean) {
//    public void setMovieFromData(boolean paramBoolean) {
////        Log.v(TAG, "loading from data (" + paramArrayOfByte.length + " bytes)");
////        setMovie(Movie.decodeByteArray(paramArrayOfByte, 0, paramArrayOfByte.length), paramBoolean);
//
//        try {
////            currentMovie = Movie.decodeStream(getResources().getAssets().open("image_1.gif"));
////            overlay = Movie.decodeStream(getResources().getAssets().open("image_1.gif"));
//
//            setMovie(Movie.decodeStream(getResources().getAssets().open("image_1.gif")), paramBoolean);
//            Toast.makeText(getContext(), "Movie Success 3", Toast.LENGTH_SHORT).show();
//        } catch (IOException e) {
//            Toast.makeText(getContext(), "Movie Failed 3", Toast.LENGTH_SHORT).show();
//        }
//    }
//
////    public void setMovieFromFile(String paramString)
////    {
////        try
////        {
////            File localFile = this.context.getFilesDir();
////            Log.v(TAG, "loading " + paramString);
////            setMovieFromData(Utils.readBytes(new FileInputStream(new File(localFile, paramString))), false);
////            return;
////        }
////        catch (IOException paramString)
////        {
////            paramString.printStackTrace();
////        }
////    }
//}
