/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nomi.artwatch;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;

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
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import pl.droidsonroids.gif.GifImageView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ArtWatchFace extends CanvasWatchFaceService {

    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);
    private static final long HIDE_GIF_IMAGE_TIMER_MS = TimeUnit.SECONDS.toMillis(30);
    private static final int MSG_UPDATE_TIME = 0;
    private static final String PATH_OF_GIF = "/gif";
    private static final String KEY_GIF = "gif";

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        private Paint mHandPaint;
        private Time mTime;
        private Handler mUpdateTimeHandler = new EngineHandler(this);
        private Handler mHandler = new Handler();
        private boolean mAmbient;
        private boolean mRegisteredTimeZoneReceiver = false;
        private boolean mLowBitAmbient;
        private GifImageView mGifImageView;
        private GifDrawable mGifResource;
        private Runnable mRunTimer = () -> {
            // Hide gif image
            Log.d(this.getClass().getCanonicalName(), "Time has passed. Gif image is hidden.");
            stopGifAnimate();
            hideGifImage();
        };
        private boolean mShouldAnimateGif;

        private BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
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
                    if (event.getType() == DataEvent.TYPE_CHANGED &&
                            event.getDataItem().getUri().getPath().equals(PATH_OF_GIF)) {

                        DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                        Asset asset = dataMapItem.getDataMap().getAsset(KEY_GIF);
                        // Change Gif image.
                        changeGifWithAsset(asset);
                    }
                }
            }
        };

        private GoogleApiClient.ConnectionCallbacks mGoogleConnectionCallback = new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle connectionHint) {
                Wearable.DataApi.addListener(mGoogleApiClient, mDataListener);
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

            Resources resources = ArtWatchFace.this.getResources();

            mHandPaint = new Paint();
            mHandPaint.setColor(resources.getColor(R.color.analog_hands));
            mHandPaint.setStrokeWidth(resources.getDimension(R.dimen.analog_hand_stroke));
            mHandPaint.setAntiAlias(true);
            mHandPaint.setStrokeCap(Paint.Cap.ROUND);

            mTime = new Time();

            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            mGifImageView = (GifImageView)inflater.inflate(R.layout.gif_view, null).findViewById(R.id.gifView);
            mGifImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            mGifImageView.setBackgroundResource(R.drawable.image_2);

            // Enable onTapCommand.
            setWatchFaceStyle(new WatchFaceStyle.Builder(ArtWatchFace.this)
                    .setAcceptsTapEvents(true)
                    .build());
        }

        @Override
        public void onTapCommand(@TapType int tapType, int x, int y, long eventTime) {
            Log.d(this.getClass().getCanonicalName(), "onTapCommand, tapType : " + tapType);

            switch (tapType) {
                case WatchFaceService.TAP_TYPE_TAP:
                    // This will not be called if swiped.
                    if (mGifImageView.getVisibility() == View.GONE) {
                        showGifImage();
                        startGifAnimate();

                    } else {
                        toggleGifAnimateState();
                    }
                    break;

                case WatchFaceService.TAP_TYPE_TOUCH:
                    break;

                case WatchFaceService.TAP_TYPE_TOUCH_CANCEL:
                    break;

                default:
                    super.onTapCommand(tapType, x, y, eventTime);
                    break;
            }
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mLowBitAmbient) {
                    mHandPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }
            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            mTime.setToNow();

            int widthSpec = View.MeasureSpec.makeMeasureSpec(bounds.width(), View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(bounds.height(), View.MeasureSpec.EXACTLY);
            mGifImageView.measure(widthSpec, heightSpec);
            mGifImageView.layout(0, 0, bounds.width(), bounds.height());
            mGifImageView.draw(canvas);

            postInvalidate();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                mGoogleApiClient.connect();

                registerReceiver();
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();

            } else {
                unregisterReceiver();

                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    Wearable.DataApi.removeListener(mGoogleApiClient, mDataListener);
                    mGoogleApiClient.disconnect();
                }
            }
            updateTimer();
        }

        /**
         * Change Gif image with asset.
         *
         * @param asset
         */
        private void changeGifWithAsset(Asset asset) {
            getInputStreamFromAsset(asset)
                    .subscribe(this::changeGif, throwable -> {
                        Timber.e(throwable.getLocalizedMessage(), throwable);
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
                        Timber.e(throwable.getLocalizedMessage(), throwable);
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
                                mShouldAnimateGif = true;
                                mGifResource = resource;
                                mGifImageView.setBackground(mGifResource);
                                startGifAnimate();
                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                Timber.e(e.getLocalizedMessage(), e);
                            }
                        });
            } catch (IOException e) {
                Timber.e(e.getLocalizedMessage(), e);
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
            byte [] buffer = new byte[1024];
            while(true) {
                int len = inputStream.read(buffer);
                if(len < 0) {
                    break;
                }
                bout.write(buffer, 0, len);
            }
            return bout.toByteArray();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            filter.addAction(Intent.ACTION_LOCALE_CHANGED);
            ArtWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            ArtWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }

        /**
         * Toggle animate state for gif image.
         */
        private void toggleGifAnimateState() {
            mShouldAnimateGif = !mShouldAnimateGif;

            if (mShouldAnimateGif) {
                startGifAnimate();
            } else {
                stopGifAnimate();
            }
        }

        private void startGifAnimate() {
            if (mGifResource != null) {
                mShouldAnimateGif = true;
                mGifResource.start();
                startTimer();
            }
        }

        private void stopGifAnimate() {
            if (mGifResource != null) {
                mShouldAnimateGif = false;
                mGifResource.stop();
                stopTimer();
            }
        }

        private void showGifImage() {
            mGifImageView.setVisibility(View.VISIBLE);
            mGifImageView.setBackground(mGifResource);
            ValueAnimator animator = ValueAnimator.ofInt(0, 255).setDuration(300);
            animator.addUpdateListener(animation -> {
                int num = (int)animation.getAnimatedValue();
                mGifResource.setAlpha(num);
            });
            animator.start();
        }

        private void hideGifImage() {
            ValueAnimator animator = ValueAnimator.ofInt(255, 0).setDuration(300);
            animator.addUpdateListener(animation -> {
                int num = (int)animation.getAnimatedValue();
                // TODO：To be faded out because it's not faded out now.
                mGifResource.setAlpha(num);
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    // GifImageView is not hidden by setVisibility...
                    mGifImageView.setVisibility(View.GONE);
                    // Change background image to hide GifImageView.
                    mGifImageView.setBackground(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.black)));
                }
            });
            animator.start();
        }

        /**
         * Start timer to hide gif image.
         */
        private void startTimer() {
            stopTimer();
            mHandler.postDelayed(mRunTimer, HIDE_GIF_IMAGE_TIMER_MS);
        }

        private void stopTimer() {
            mHandler.removeCallbacks(mRunTimer);
        }
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<ArtWatchFace.Engine> mWeakReference;

        public EngineHandler(ArtWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            ArtWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }
}
