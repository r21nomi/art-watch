package com.nomi.artwatch;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
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
import java.util.concurrent.TimeUnit;

import pl.droidsonroids.gif.GifImageView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ArtWatchFace extends CanvasWatchFaceService {

    private static final long HIDE_GIF_IMAGE_TIMER_MS = TimeUnit.SECONDS.toMillis(30);
    private static final String PATH_OF_GIF = "/gif";
    private static final String KEY_GIF = "gif";

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        private Handler mHandler = new Handler();
        private boolean mAmbient;
        private boolean mLowBitAmbient;
        private GifImageView mGifImageView;
        private GifDrawable mGifResource;
        private boolean mIsSleeping;
        private Runnable mRunTimer = () -> {
            // Hide gif image
            Log.d(this.getClass().getCanonicalName(), "Time has passed. Gif image is hidden.");
            stopGifAnimate();
            hideGifImage();
        };
        private boolean mShouldAnimateGif;

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

            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            mGifImageView = (GifImageView)inflater.inflate(R.layout.gif_view, null).findViewById(R.id.gifView);
            mGifImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            mGifImageView.setBackgroundResource(R.drawable.img_default);

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
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

//        @Override
//        public void onTimeTick() {
//            super.onTimeTick();
//            invalidate();
//        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
            }
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            Log.d(this.getClass().getCanonicalName(), "onDraw");

            int widthSpec = View.MeasureSpec.makeMeasureSpec(bounds.width(), View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(bounds.height(), View.MeasureSpec.EXACTLY);
            mGifImageView.measure(widthSpec, heightSpec);
            mGifImageView.layout(0, 0, bounds.width(), bounds.height());
            mGifImageView.draw(canvas);

            if (!mIsSleeping) {
                // CanvasWatchFaceService#onDraw will be called.
                // If mIsSleeping == true, do not draw.
                postInvalidate();
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                mGoogleApiClient.connect();
            } else {
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    Wearable.DataApi.removeListener(mGoogleApiClient, mDataListener);
                    mGoogleApiClient.disconnect();
                }
            }
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
                                if (mGifImageView.getVisibility() == View.GONE) {
                                    showGifImage();
                                } else {
                                    mGifImageView.setBackground(mGifResource);
                                }
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
                mIsSleeping = false;
                mGifResource.start();
                startTimer();
                // Start drawing.
                postInvalidate();
            }
        }

        private void stopGifAnimate() {
            if (mGifResource != null) {
                mShouldAnimateGif = false;
                mIsSleeping = true;
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
                int num = (int)animation.getAnimatedValue();
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
                    postInvalidate();
                    Log.d(this.getClass().getCanonicalName(), " Gif image has been hidden.");
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
}
