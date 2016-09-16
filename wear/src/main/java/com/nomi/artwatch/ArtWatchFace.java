package com.nomi.artwatch;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import pl.droidsonroids.gif.GifImageView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ArtWatchFace extends CanvasWatchFaceService {

    private static final long GIF_ANIMATE_DURATION = 100;
    private static final int HIDE_GIF_IMAGE_TIMER_MS = (int) TimeUnit.SECONDS.toMillis(30);
    private static final String PATH_OF_GIF = "/gif";
    private static final String PATH_OF_TIMEOUT = "/timeout";
    private static final String KEY_GIF = "gif";
    private static final String KEY_TIMEOUT = "timeout";
    private static final String COLON_STRING = " : ";

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        private Handler mSleepHandler = new Handler();
        private Handler mGifAnimateHandler = new Handler();
        private GifDrawable mGifResource;
        private boolean mIsSleeping;
        private long mTimeout = HIDE_GIF_IMAGE_TIMER_MS;
        private Calendar mCalendar;
        private Date mDate;
        private Paint mBackgroundPaint;

        private View mRootView;
        private GifImageView mGifImageView;
        private TextView mTimeView;

        private boolean mIsRound;

        private Runnable mSleepRunnable = () -> {
            // Hide gif image
            Log.d(this.getClass().getCanonicalName(), "Time has passed. Gif image is hidden.");
            stopGifAnimate();
            hideGifImage();
            invalidate();
        };

        private Runnable mGifAnimateRunnable = () -> {
            invalidate();
            startGifAnimateTimer();
        };

        private DataApi.DataListener mDataListener = new DataApi.DataListener() {
            /**
             * Called when the gif on the phone was selected.
             * Receive gif data as Asset.
             *
             * TODO：To be fixed cause it takes too long time to receiver the gif data successfully.
             */
            @Override
            public void onDataChanged(DataEventBuffer dataEvents) {
                for (DataEvent event : dataEvents) {
                    if (event.getType() == DataEvent.TYPE_CHANGED) {
                        if (event.getDataItem().getUri().getPath().equals(PATH_OF_GIF)) {
                            DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                            DataMap dataMap = dataMapItem.getDataMap();
                            applyGifDataMap(dataMap);

                        } else if (event.getDataItem().getUri().getPath().equals(PATH_OF_TIMEOUT)) {
                            DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                            DataMap dataMap = dataMapItem.getDataMap();
                            applyTimeoutDataMap(dataMap);
                            Log.d(this.getClass().getCanonicalName(), "Timeout : " + mTimeout);
                        }
                    }
                }
            }
        };

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            mIsRound = insets.isRound();

            Log.d(this.getClass().getCanonicalName(), "isRound : " + mIsRound);
        }

        private GoogleApiClient.ConnectionCallbacks mGoogleConnectionCallback = new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle connectionHint) {
                Wearable.DataApi.addListener(mGoogleApiClient, mDataListener);

                // Fetch gif image from storage.
                WatchFaceUtil.fetchGifDataMap(mGoogleApiClient, resultDataMap -> {
                    if (resultDataMap != null) {
                        applyGifDataMap(resultDataMap);

                    } else if (mGifImageView != null) {
                        // Set default image.
                        changeGif(getResources().openRawResource(R.raw.img_default));
                    }
                });

                // Fetch timeout count from storage.
                WatchFaceUtil.fetchTimeoutDataMap(mGoogleApiClient, resultDataMap -> {
                    if (resultDataMap != null) {
                        mTimeout = resultDataMap.getLong(KEY_TIMEOUT);
                    }
                });
            }

            @Override
            public void onConnectionSuspended(int cause) {
                // TODO：Handling
            }
        };

        private GoogleApiClient.OnConnectionFailedListener mGoogleConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
                // TODO：Error handling
            }
        };

        private GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(ArtWatchFace.this)
                .addConnectionCallbacks(mGoogleConnectionCallback)
                .addOnConnectionFailedListener(mGoogleConnectionFailedListener)
                .addApi(Wearable.API)
                .build();

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(ArtWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());

            // Enable onTapCommand.
            setWatchFaceStyle(new WatchFaceStyle.Builder(ArtWatchFace.this)
                    .setAcceptsTapEvents(true)
                    .build());

            initGif();
            initTime();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            Log.v(this.getClass().getCanonicalName(), "onAmbientModeChanged");

            if (!isInAmbientMode() && mGifImageView.getVisibility() == View.GONE) {
                showGifImage();
                startGifAnimate();
            }
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            Log.v(this.getClass().getCanonicalName(), "onDraw");

            int widthSpec = View.MeasureSpec.makeMeasureSpec(bounds.width(), View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(bounds.height(), View.MeasureSpec.EXACTLY);
            mRootView.measure(widthSpec, heightSpec);
            mRootView.layout(0, 0, bounds.width(), bounds.height());

            if (mGifResource != null) {
                // Adjust GifImageView size
                int width = bounds.width();
                int height = bounds.width() * mGifResource.getIntrinsicHeight() / mGifResource.getIntrinsicWidth();

                mGifImageView.measure(
                        View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
                );
                mGifImageView.layout(
                        (bounds.width() - width) / 2,
                        (bounds.height() - height) / 2,
                        (bounds.width() + width) / 2,
                        (bounds.height() + height) / 2
                );
            } else {
                mGifImageView.layout(0, 0, bounds.width(), bounds.height());
            }

            mGifImageView.setVisibility(mIsSleeping ? View.GONE : View.VISIBLE);

            setMarginToTimeView();

            if (isInAmbientMode()) {
                boolean is24Hour = DateFormat.is24HourFormat(ArtWatchFace.this);
                long now = System.currentTimeMillis();
                mCalendar.setTimeInMillis(now);
                mDate.setTime(now);

                // Draw the hours.
                String hourString;
                if (is24Hour) {
                    hourString = formatTwoDigitNumber((mCalendar.get(Calendar.HOUR_OF_DAY)));

                } else {
                    int hour = mCalendar.get(Calendar.HOUR);
                    if (hour == 0) {
                        hour = 12;
                    }
                    hourString = String.valueOf(hour);
                }

                if (mTimeView.getVisibility() != View.VISIBLE) {
                    mTimeView.setVisibility(View.VISIBLE);
                }

                String minuteString = formatTwoDigitNumber(mCalendar.get(Calendar.MINUTE));
                mTimeView.setText(hourString + COLON_STRING + minuteString);

            } else {
                mTimeView.setVisibility(View.GONE);
            }

            mRootView.draw(canvas);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            Log.v(this.getClass().getCanonicalName(), "onVisibilityChanged");

            if (visible) {
                mGoogleApiClient.connect();
                mCalendar.setTimeZone(TimeZone.getDefault());

            } else {
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    Wearable.DataApi.removeListener(mGoogleApiClient, mDataListener);
                    mGoogleApiClient.disconnect();
                }
            }
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            Log.v(this.getClass().getCanonicalName(), "onTimeTick");

            invalidate();
        }

        private void initGif() {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View rootView = inflater.inflate(R.layout.gif_view, null);
            mGifImageView = (GifImageView) rootView.findViewById(R.id.gifView);
            mGifImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            mRootView = rootView;
            mTimeView = (TextView) rootView.findViewById(R.id.time);
        }

        private void initTime() {
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
            mCalendar = Calendar.getInstance();
            mDate = new Date();
        }

        /**
         * Apply data map.
         * By this process, gif image will be shown.
         *
         * @param dataMap
         */
        private void applyGifDataMap(DataMap dataMap) {
            Asset asset = dataMap.getAsset(KEY_GIF);
            // Change Gif image.
            changeGifWithAsset(asset);
            // Save data onto storage.
            saveGifData(dataMap);
        }

        private void applyTimeoutDataMap(DataMap dataMap) {
            mTimeout = dataMap.getLong(KEY_TIMEOUT);

            showGifImage();
            startGifAnimate();
            saveTimeoutData(dataMap);
        }

        private void saveGifData(DataMap dataMap) {
            WatchFaceUtil.putGifData(mGoogleApiClient, dataMap, resultDataMap -> {
                if (resultDataMap != null) {
                    Log.d(this.getClass().getCanonicalName(), "Gif data item has successfully saved.");
                } else {
                    Log.d(this.getClass().getCanonicalName(), "Gif data item has not saved by any reason.");
                }
            });
        }

        private void saveTimeoutData(DataMap dataMap) {
            WatchFaceUtil.putTimeoutData(mGoogleApiClient, dataMap, resultDataMap -> {
                if (resultDataMap != null) {
                    Log.d(this.getClass().getCanonicalName(), "Timeout data item has successfully saved.");
                } else {
                    Log.d(this.getClass().getCanonicalName(), "Timeout data item has not saved by any reason.");
                }
            });
        }

        /**
         * Change Gif image with asset.
         *
         * @param asset
         */
        private void changeGifWithAsset(Asset asset) {
            getInputStreamFromAsset(asset)
                    .subscribe(this::changeGif, throwable -> {
                        Log.e(this.getClass().getCanonicalName(), throwable.getLocalizedMessage());
                    });
        }

        /**
         * Get inputStream from asset.
         *
         * @param asset
         */
        private Observable<InputStream> getInputStreamFromAsset(final Asset asset) {
            if (asset == null) {
                return Observable.error(new IllegalArgumentException("Asset must be non-null"));
            }
            return Observable
                    .just(null)
                    .subscribeOn(Schedulers.io())
                    .flatMap(aVoid -> {
                        // We must get inputStream on io thread.
                        // convert asset into a file descriptor and block until it's ready
                        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                                mGoogleApiClient, asset).await().getInputStream();
                        return Observable.just(assetInputStream);
                    })
                    .onErrorResumeNext(throwable -> {
                        Log.e(this.getClass().getCanonicalName(), throwable.getLocalizedMessage());
                        return Observable.error(throwable);
                    })
                    .observeOn(AndroidSchedulers.mainThread());
        }

        /**
         * Change current gif.
         *
         * @param inputStream
         */
        private void changeGif(InputStream inputStream) {
            try {
                Glide.with(mGifImageView.getContext())
                        .load(toByteArray(inputStream))
                        .asGif()
                        .into(new SimpleTarget<GifDrawable>() {
                            @Override
                            public void onResourceReady(com.bumptech.glide.load.resource.gif.GifDrawable resource,
                                                        GlideAnimation<? super GifDrawable> glideAnimation) {
                                mGifResource = resource;
                                if (mGifImageView.getVisibility() == View.GONE) {
                                    showGifImage();
                                } else {
                                    mGifImageView.setBackground(mGifResource);
                                }
                                startGifAnimate();
                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                Log.e(this.getClass().getCanonicalName(), e.getLocalizedMessage());
                            }
                        });
            } catch (IOException e) {
                Log.e(this.getClass().getCanonicalName(), e.getLocalizedMessage());
            }
        }

        /**
         * Convert InputStream to byte[].
         *
         * @param inputStream
         * @return
         * @throws IOException
         */
        private byte[] toByteArray(InputStream inputStream) throws IOException {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            while (true) {
                int len = inputStream.read(buffer);
                if (len < 0) {
                    break;
                }
                bout.write(buffer, 0, len);
            }
            return bout.toByteArray();
        }

        private void startGifAnimate() {
            if (mGifResource != null) {
                mIsSleeping = false;
                mGifResource.start();
                startSleepTimer();
                // Start drawing.
                startGifAnimateTimer();
            }
        }

        private void stopGifAnimate() {
            if (mGifResource != null) {
                mIsSleeping = true;
                mGifResource.stop();
                stopSleepTimer();
                stopGifAnimateTimer();
            }
        }

        private void showGifImage() {
            mGifImageView.setVisibility(View.VISIBLE);
            mGifImageView.setBackground(mGifResource);
            ValueAnimator animator = ValueAnimator.ofInt(0, 255).setDuration(300);
            animator.addUpdateListener(animation -> {
                int num = (int) animation.getAnimatedValue();
                mGifImageView.setImageAlpha(num);
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                }
            });
            animator.start();
        }

        private void hideGifImage() {
            ValueAnimator animator = ValueAnimator.ofInt(255, 0).setDuration(300);
            animator.addUpdateListener(animation -> {
                int num = (int) animation.getAnimatedValue();
                // TODO：To be faded out because it's not faded out now.
                mGifImageView.setImageAlpha(num);
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    // GifImageView is not hidden by setVisibility...
                    mGifImageView.setVisibility(View.GONE);
                    // Change background image to hide GifImageView.
                    mGifImageView.setBackground(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.black)));
                    // Apply changes.
//                    invalidate();
                    Log.d(this.getClass().getCanonicalName(), " Gif image has been hidden.");
                }
            });
            animator.start();
        }

        private void setMarginToTimeView() {
            int leftRes;

            if (mIsRound) {
                leftRes = R.dimen.time_x_round;
            } else {
                leftRes = R.dimen.time_x;
            }

            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mTimeView.getLayoutParams();
            layoutParams.setMargins(
                    getResources().getDimensionPixelSize(leftRes),
                    getResources().getDimensionPixelSize(R.dimen.time_y),
                    0,
                    0
            );
            mTimeView.setLayoutParams(layoutParams);
        }

        private String formatTwoDigitNumber(int hour) {
            return String.format("%02d", hour);
        }

        /**
         * Start timer to hide gif image.
         */
        private void startSleepTimer() {
            stopSleepTimer();

            if (mTimeout > 0) {
                mSleepHandler.postDelayed(mSleepRunnable, mTimeout);
            }
        }

        private void stopSleepTimer() {
            mSleepHandler.removeCallbacks(mSleepRunnable);
        }

        private void startGifAnimateTimer() {
            stopGifAnimateTimer();
            mGifAnimateHandler.postDelayed(mGifAnimateRunnable, GIF_ANIMATE_DURATION);
        }

        private void stopGifAnimateTimer() {
            mGifAnimateHandler.removeCallbacks(mGifAnimateRunnable);
        }
    }
}
