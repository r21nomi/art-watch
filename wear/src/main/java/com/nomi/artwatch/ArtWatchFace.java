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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.lang.ref.WeakReference;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import pl.droidsonroids.gif.GifImageView;

/**
 * Analog watch face with a ticking second hand. In ambient mode, the second hand isn't shown. On
 * devices with low-bit ambient mode, the hands are drawn without anti-aliasing in ambient mode.
 */
public class ArtWatchFace extends CanvasWatchFaceService {
    /**
     * Update rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine implements DataApi.DataListener,
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
        Paint mBackgroundPaint;
        Paint mHandPaint;
        boolean mAmbient;
        Time mTime;

        final Handler mUpdateTimeHandler = new EngineHandler(this);
        int mInteractiveBackgroundColor =
                DigitalWatchFaceUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_BACKGROUND;

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };
        boolean mRegisteredTimeZoneReceiver = false;

        private GifImageView mGifImageView;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(ArtWatchFace.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
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

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(mInteractiveBackgroundColor);

            mHandPaint = new Paint();
            mHandPaint.setColor(resources.getColor(R.color.analog_hands));
            mHandPaint.setStrokeWidth(resources.getDimension(R.dimen.analog_hand_stroke));
            mHandPaint.setAntiAlias(true);
            mHandPaint.setStrokeCap(Paint.Cap.ROUND);

            mTime = new Time();

            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            mGifImageView = (GifImageView)inflater.inflate(R.layout.gif_view, null).findViewById(R.id.gifView);
            mGifImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            mGifImageView.setImageResource(R.drawable.image_2);
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

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            mTime.setToNow();

//            int width = bounds.width();
//            int height = bounds.height();
//
//            // Draw the background.
//            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mBackgroundPaint);
//
//            // Find the center. Ignore the window insets so that, on round watches with a
//            // "chin", the watch face is centered on the entire screen, not just the usable
//            // portion.
//            float centerX = width / 2f;
//            float centerY = height / 2f;
//
//            float secRot = mTime.second / 30f * (float) Math.PI;
//            int minutes = mTime.minute;
//            float minRot = minutes / 30f * (float) Math.PI;
//            float hrRot = ((mTime.hour + (minutes / 60f)) / 6f) * (float) Math.PI;
//
//            float secLength = centerX - 20;
//            float minLength = centerX - 40;
//            float hrLength = centerX - 80;
//
//            if (!mAmbient) {
//                float secX = (float) Math.sin(secRot) * secLength;
//                float secY = (float) -Math.cos(secRot) * secLength;
//                canvas.drawLine(centerX, centerY, centerX + secX, centerY + secY, mHandPaint);
//            }
//
//            float minX = (float) Math.sin(minRot) * minLength;
//            float minY = (float) -Math.cos(minRot) * minLength;
//            canvas.drawLine(centerX, centerY, centerX + minX, centerY + minY, mHandPaint);
//
//            float hrX = (float) Math.sin(hrRot) * hrLength;
//            float hrY = (float) -Math.cos(hrRot) * hrLength;
//            canvas.drawLine(centerX, centerY, centerX + hrX, centerY + hrY, mHandPaint);


//            Object localObject = new Paint();
//            ((Paint)localObject).setAntiAlias(true);
//////            paramCanvas.drawColor(-1);
//            long l = SystemClock.uptimeMillis();
////            ((Paint)localObject).setColor(-16777216);
////            paramCanvas.drawRect(new Rect(0, 0, paramCanvas.getWidth(), paramCanvas.getHeight()), (Paint)localObject);
//            localObject = this.mMovie;
////            if (this.overlay != null) {
////                localObject = this.overlay;
////            }
////            if ((!this.isDimmed) && (localObject != null) && (((Movie)localObject).duration() > 0))
////            {
////                if (this.movieStart == 0L) {
////                    this.movieStart = l;
////                }
////                float f1 = 1.0F;
////                float f2 = f1;
////                if (isFullscreen())
////                {
////                    if ((((Movie)localObject).height() < paramCanvas.getHeight()) || (((Movie)localObject).width() < paramCanvas.getWidth())) {
////                        f1 = Math.max(paramCanvas.getHeight() / ((Movie)localObject).height(), paramCanvas.getWidth() / ((Movie)localObject).width());
////                    }
////                    paramCanvas.scale(f1, f1);
////                    f2 = f1;
////                }
////                paramCanvas.translate((paramCanvas.getWidth() / f2 - ((Movie)localObject).width()) / 2.0F, (paramCanvas.getHeight() / f2 - ((Movie)localObject).height()) / 2.0F);
////                if (this.overlay == null) {
////                    break label333;
////                }
////                l = Math.min(((Movie)localObject).duration(), l - this.movieStart);
////            }
////            for (;;)
//            {
//                if (l != this.mCurrentAnimationTime)
//                {
//                    this.mCurrentAnimationTime = (int)l;
//                    ((Movie)localObject).setTime((int)this.mCurrentAnimationTime);
//                    ((Movie)localObject).draw(canvas, 0, 0);
//                    invalidate();
////                    if ((this.overlay != null) && (l >= ((Movie)localObject).duration())) {
////                        this.overlay = null;
////                    }
//                }
//                try
//                {
//                    Thread.currentThread();
//                    Thread.sleep(this.DRAW_SLEEP);
//                    this.drawCount += 1;
////                    super.onDraw(canvas);
//                    return;
//                    label333:
//                    l = (int)((l - this.mMovieStart) % ((Movie)localObject).duration());
//                }
//                catch (InterruptedException localInterruptedException)
//                {
//                    for (;;)
//                    {
//                        localInterruptedException.printStackTrace();
//                    }
//                }
////            }

//            mGifImageView.draw(canvas);

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

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();

                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    Wearable.DataApi.removeListener(mGoogleApiClient, this);
                    mGoogleApiClient.disconnect();
                }
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override // DataApi.DataListener
        public void onDataChanged(DataEventBuffer dataEvents) {
            for (DataEvent dataEvent : dataEvents) {
                if (dataEvent.getType() != DataEvent.TYPE_CHANGED) {
                    continue;
                }

                DataItem dataItem = dataEvent.getDataItem();
                if (!dataItem.getUri().getPath().equals(
                        DigitalWatchFaceUtil.PATH_WITH_FEATURE)) {
                    continue;
                }

                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                DataMap config = dataMapItem.getDataMap();
                updateUiForConfigDataMap(config);
            }
        }

        @Override  // GoogleApiClient.ConnectionCallbacks
        public void onConnected(Bundle connectionHint) {
            Wearable.DataApi.addListener(mGoogleApiClient, Engine.this);
            updateConfigDataItemAndUiOnStartup();
        }

        @Override  // GoogleApiClient.ConnectionCallbacks
        public void onConnectionSuspended(int cause) {

        }

        @Override  // GoogleApiClient.OnConnectionFailedListener
        public void onConnectionFailed(ConnectionResult result) {

        }

        private void updateConfigDataItemAndUiOnStartup() {
            DigitalWatchFaceUtil.fetchConfigDataMap(mGoogleApiClient,
                    new DigitalWatchFaceUtil.FetchConfigDataMapCallback() {
                        @Override
                        public void onConfigDataMapFetched(DataMap startupConfig) {
                            // If the DataItem hasn't been created yet or some keys are missing,
                            // use the default values.
                            setDefaultValuesForMissingConfigKeys(startupConfig);
                            DigitalWatchFaceUtil.putConfigDataItem(mGoogleApiClient, startupConfig);

                            updateUiForConfigDataMap(startupConfig);
                        }
                    }
            );
        }

        private void setDefaultValuesForMissingConfigKeys(DataMap config) {
//            addIntKeyIfMissing(config, DigitalWatchFaceUtil.KEY_COLOR,
//                    DigitalWatchFaceUtil.COLOR_VALUE_DEFAULT_AND_AMBIENT_BACKGROUND);

            addStringKeyIfMissing(config, DigitalWatchFaceUtil.KEY_GIF_URL, "");
        }

//        private void addIntKeyIfMissing(DataMap config, String key, int color) {
//            if (!config.containsKey(key)) {
//                config.putInt(key, color);
//            }
//        }

        private void addStringKeyIfMissing(DataMap config, String key, String url) {
            if (!config.containsKey(key)) {
                config.putString(key, url);
            }
        }

//        private void updateUiForConfigDataMap(final DataMap config) {
//            boolean uiUpdated = false;
//            for (String configKey : config.keySet()) {
//                if (!config.containsKey(configKey)) {
//                    continue;
//                }
//                int color = config.getInt(configKey);
//                if (updateUiForKey(configKey, color)) {
//                    uiUpdated = true;
//                }
//            }
//            if (uiUpdated) {
//                invalidate();
//            }
//        }

        private void updateUiForConfigDataMap(final DataMap config) {
            boolean uiUpdated = false;
            for (String configKey : config.keySet()) {
                if (!config.containsKey(configKey)) {
                    continue;
                }
                String url = config.getString(configKey);
                if (updateUiForKey(configKey, url)) {
                    uiUpdated = true;
                }
            }
            if (uiUpdated) {
                invalidate();
            }
        }

//        private boolean updateUiForKey(String configKey, int color) {
//            if (configKey.equals(DigitalWatchFaceUtil.KEY_COLOR)) {
//                setInteractiveBackgroundColor(color);
//            } else {
//                return false;
//            }
//            return true;
//        }

        private boolean updateUiForKey(String configKey, String url) {
            if (configKey.equals(DigitalWatchFaceUtil.KEY_COLOR)) {
//                setInteractiveBackgroundColor(url);
                Glide.with(mGifImageView.getContext())
                        .load(url)
                        .asGif()
                        .into(new SimpleTarget<com.bumptech.glide.load.resource.gif.GifDrawable>() {
                            @Override
                            public void onResourceReady(com.bumptech.glide.load.resource.gif.GifDrawable resource,
                                                        GlideAnimation<? super com.bumptech.glide.load.resource.gif.GifDrawable> glideAnimation) {
//                                int width = WindowUtil.getWidth(holder.mGifView.getContext());
//                                int height = width * resource.getIntrinsicHeight() / resource.getIntrinsicWidth();
//
//                                ViewGroup.LayoutParams params = holder.mGifView.getLayoutParams();
//                                params.width = width;
//                                params.height = height;
                                mGifImageView.setBackground(resource);

                                resource.start();
                            }
                        });

            } else {
                return false;
            }
            return true;
        }

        private void setInteractiveBackgroundColor(int color) {
            mInteractiveBackgroundColor = color;
            updatePaintIfInteractive(mBackgroundPaint, color);
        }

        private void updatePaintIfInteractive(Paint paint, int interactiveColor) {
            if (!isInAmbientMode() && paint != null) {
                paint.setColor(interactiveColor);
            }
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
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
