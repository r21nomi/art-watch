package com.nomi.artwatch

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.*
import pl.droidsonroids.gif.GifImageView
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit

class ArtWatchFace : CanvasWatchFaceService() {

    override fun onCreateEngine(): CanvasWatchFaceService.Engine {
        return Engine()
    }

    private inner class Engine : CanvasWatchFaceService.Engine() {
        private val mSleepHandler = Handler()
        private val mGifAnimateHandler = Handler()
        private var mGifResource: GifDrawable? = null
        private var mIsSleeping: Boolean = false
        private var mTimeout = HIDE_GIF_IMAGE_TIMER_MS.toLong()
        private val mDate: Date = Date()
        private val mBackgroundPaint: Paint = Paint()
        private val mCalendar: Calendar = Calendar.getInstance()

        private val mRootView: View by lazy {
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.gif_view, null)
        }

        private val mGifImageView: GifImageView by lazy {
            mRootView.findViewById(R.id.gifView) as GifImageView
        }

        private val mTimeView: TextView by lazy {
            mRootView.findViewById(R.id.time) as TextView
        }

        private var mIsRound: Boolean = false

        private val mSleepRunnable = {
            // Hide gif image
            Log.d(this.javaClass.canonicalName, "Time has passed. Gif image is hidden.")
            stopGifAnimate()
            hideGifImage()
            invalidate()
        }

        private val mGifAnimateRunnable = {
            invalidate()
            startGifAnimateTimer()
        }

        private val mDataListener = object : DataApi.DataListener {
            /**
             * Called when the gif on the phone was selected.
             * Receive gif data as Asset.

             * TODO：To be fixed cause it takes too long time to receiver the gif data successfully.
             */
            override fun onDataChanged(dataEvents: DataEventBuffer) {
                dataEvents.forEach { event ->
                    if (event.type == DataEvent.TYPE_CHANGED) {
                        if (event.dataItem.uri.path == WatchFaceUtil.PATH_OF_GIF) {
                            val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                            val dataMap = dataMapItem.dataMap
                            applyGifDataMap(dataMap)

                        } else if (event.dataItem.uri.path == WatchFaceUtil.PATH_OF_TIMEOUT) {
                            val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                            val dataMap = dataMapItem.dataMap
                            applyTimeoutDataMap(dataMap)
                            Log.d(this.javaClass.canonicalName, "Timeout : " + mTimeout)
                        }
                    }
                }
            }
        }

        override fun onApplyWindowInsets(insets: WindowInsets) {
            super.onApplyWindowInsets(insets)

            mIsRound = insets.isRound

            Log.d(this.javaClass.canonicalName, "isRound : " + mIsRound)
        }

        private val mGoogleConnectionCallback = object : GoogleApiClient.ConnectionCallbacks {
            override fun onConnected(connectionHint: Bundle?) {
                Wearable.DataApi.addListener(mGoogleApiClient, mDataListener)

                // Fetch gif image from storage.
                WatchFaceUtil.fetchGifDataMap(mGoogleApiClient, Action1 { resultDataMap ->
                    if (resultDataMap != null) {
                        applyGifDataMap(resultDataMap)

                    } else {
                        // Set default image.
                        changeGif(resources.openRawResource(R.raw.img_default))
                    }
                })

                // Fetch timeout count from storage.
                WatchFaceUtil.fetchTimeoutDataMap(mGoogleApiClient, Action1 { resultDataMap ->
                    if (resultDataMap != null) {
                        mTimeout = resultDataMap.getLong(WatchFaceUtil.KEY_TIMEOUT, HIDE_GIF_IMAGE_TIMER_MS.toLong())
                    }
                })
            }

            override fun onConnectionSuspended(cause: Int) {
                // TODO：Handling
            }
        }

        private val mGoogleConnectionFailedListener = GoogleApiClient.OnConnectionFailedListener {
            // TODO：Error handling
        }

        private val mGoogleApiClient: GoogleApiClient = GoogleApiClient.Builder(this@ArtWatchFace)
                .addConnectionCallbacks(mGoogleConnectionCallback)
                .addOnConnectionFailedListener(mGoogleConnectionFailedListener)
                .addApi(Wearable.API)
                .build()

        override fun onCreate(holder: SurfaceHolder?) {
            super.onCreate(holder)

            setWatchFaceStyle(WatchFaceStyle.Builder(this@ArtWatchFace)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build())

            // Enable onTapCommand.
            setWatchFaceStyle(WatchFaceStyle.Builder(this@ArtWatchFace)
                    .setAcceptsTapEvents(true)
                    .build())

            mGifImageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

            mBackgroundPaint.color = ContextCompat.getColor(applicationContext, R.color.black)
        }

        override fun onDestroy() {
            super.onDestroy()
        }

        override fun onPropertiesChanged(properties: Bundle?) {
            super.onPropertiesChanged(properties)
        }

        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
            super.onAmbientModeChanged(inAmbientMode)
            Log.v(this.javaClass.canonicalName, "onAmbientModeChanged")

            if (!isInAmbientMode && mGifImageView.visibility == View.GONE) {
                showGifImage()
                startGifAnimate()
            }
        }

        override fun onDraw(canvas: Canvas?, bounds: Rect?) {
            Log.v(this.javaClass.canonicalName, "onDraw")

            val widthSpec = View.MeasureSpec.makeMeasureSpec(bounds!!.width(), View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(bounds.height(), View.MeasureSpec.EXACTLY)
            mRootView.measure(widthSpec, heightSpec)
            mRootView.layout(0, 0, bounds.width(), bounds.height())

            if (mGifResource != null) {
                // Adjust GifImageView size
                val width = bounds.width()
                val height = bounds.width() * mGifResource!!.intrinsicHeight / mGifResource!!.intrinsicWidth
                mGifImageView.measure(
                        View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
                )
                mGifImageView.layout(
                        (bounds.width() - width) / 2,
                        (bounds.height() - height) / 2,
                        (bounds.width() + width) / 2,
                        (bounds.height() + height) / 2
                )
            } else {
                mGifImageView.layout(0, 0, bounds.width(), bounds.height())
            }

            mGifImageView.visibility = if (mIsSleeping) View.GONE else View.VISIBLE

            setMarginToTimeView()

            if (isInAmbientMode) {
                val is24Hour = DateFormat.is24HourFormat(this@ArtWatchFace)
                val now = System.currentTimeMillis()
                mCalendar.timeInMillis = now
                mDate.time = now

                // Draw the hours.
                val hourString: String
                if (is24Hour) {
                    hourString = formatTwoDigitNumber(mCalendar.get(Calendar.HOUR_OF_DAY))

                } else {
                    var hour = mCalendar.get(Calendar.HOUR)
                    if (hour == 0) {
                        hour = 12
                    }
                    hourString = hour.toString()
                }

                if (mTimeView.visibility != View.VISIBLE) {
                    mTimeView.visibility = View.VISIBLE
                }

                val minuteString = formatTwoDigitNumber(mCalendar.get(Calendar.MINUTE))
                mTimeView.text = "$hourString$COLON_STRING$minuteString"

            } else {
                mTimeView.visibility = View.GONE
            }

            mRootView.draw(canvas)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            Log.v(this.javaClass.canonicalName, "onVisibilityChanged")

            if (visible) {
                mGoogleApiClient.connect()
                mCalendar.timeZone = TimeZone.getDefault()

            } else {
                if (mGoogleApiClient.isConnected) {
                    Wearable.DataApi.removeListener(mGoogleApiClient, mDataListener)
                    mGoogleApiClient.disconnect()
                }
            }
        }

        override fun onTimeTick() {
            super.onTimeTick()
            Log.v(this.javaClass.canonicalName, "onTimeTick")

            invalidate()
        }

        /**
         * Apply data map.
         * By this process, gif image will be shown.

         * @param dataMap
         */
        private fun applyGifDataMap(dataMap: DataMap) {
            val asset = dataMap.getAsset(WatchFaceUtil.KEY_GIF)
            // Change Gif image.
            changeGifWithAsset(asset)
            // Save data onto storage.
            saveGifData(dataMap)
        }

        private fun applyTimeoutDataMap(dataMap: DataMap) {
            mTimeout = dataMap.getLong(WatchFaceUtil.KEY_TIMEOUT)

            showGifImage()
            startGifAnimate()
            saveTimeoutData(dataMap)
        }

        private fun saveGifData(dataMap: DataMap) {
            WatchFaceUtil.putGifData(mGoogleApiClient, dataMap, Action1 { resultDataMap ->
                if (resultDataMap != null) {
                    Log.d(this.javaClass.canonicalName, "Gif data item has successfully saved.")
                } else {
                    Log.d(this.javaClass.canonicalName, "Gif data item has not saved by any reason.")
                }
            })
        }

        private fun saveTimeoutData(dataMap: DataMap) {
            WatchFaceUtil.putTimeoutData(mGoogleApiClient, dataMap, Action1 { resultDataMap ->
                if (resultDataMap != null) {
                    Log.d(this.javaClass.canonicalName, "Timeout data item has successfully saved.")
                } else {
                    Log.d(this.javaClass.canonicalName, "Timeout data item has not saved by any reason.")
                }
            })
        }

        /**
         * Change Gif image with asset.

         * @param asset
         */
        private fun changeGifWithAsset(asset: Asset) {
            getInputStreamFromAsset(asset)
                    .subscribe({
                        changeGif(it)
                    }, {
                        Log.e(this.javaClass.canonicalName, it.localizedMessage)
                    })
        }

        /**
         * Get inputStream from asset.

         * @param asset
         */
        private fun getInputStreamFromAsset(asset: Asset?): Observable<InputStream> {
            if (asset == null) {
                return Observable.error<InputStream>(IllegalArgumentException("Asset must be non-null"))
            }
            return Observable
                    .just<Any>(null)
                    .subscribeOn(Schedulers.io())
                    .flatMap {
                        // We must get inputStream on io thread.
                        // convert asset into a file descriptor and block until it's ready
                        val assetInputStream = Wearable.DataApi.getFdForAsset(
                                mGoogleApiClient, asset).await().inputStream
                        Observable.just(assetInputStream)
                    }
                    .onErrorResumeNext {
                        Log.e(this.javaClass.canonicalName, it.localizedMessage)
                        Observable.error<InputStream>(it)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
        }

        /**
         * Change current gif.

         * @param inputStream
         */
        private fun changeGif(inputStream: InputStream) {
            try {
                Glide.with(mGifImageView.context)
                        .load(toByteArray(inputStream))
                        .asGif()
                        .into(object : SimpleTarget<GifDrawable>() {
                            override fun onResourceReady(resource: com.bumptech.glide.load.resource.gif.GifDrawable,
                                                         glideAnimation: GlideAnimation<in GifDrawable>) {
                                mGifResource = resource
                                if (mGifImageView.visibility == View.GONE) {
                                    showGifImage()
                                } else {
                                    mGifImageView.background = mGifResource
                                }
                                startGifAnimate()
                            }

                            override fun onLoadFailed(e: Exception?, errorDrawable: Drawable?) {
                                Log.e(this.javaClass.canonicalName, e!!.localizedMessage)
                            }
                        })
            } catch (e: IOException) {
                Log.e(this.javaClass.canonicalName, e.localizedMessage)
            }

        }

        /**
         * Convert InputStream to byte[].

         * @param inputStream
         * *
         * @return
         * *
         * @throws IOException
         */
        @Throws(IOException::class)
        private fun toByteArray(inputStream: InputStream): ByteArray {
            val bout = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            while (true) {
                val len = inputStream.read(buffer)
                if (len < 0) {
                    break
                }
                bout.write(buffer, 0, len)
            }
            return bout.toByteArray()
        }

        private fun startGifAnimate() {
            mGifResource?.let { giResource ->
                mIsSleeping = false
                giResource.start()
                startSleepTimer()
                // Start drawing.
                startGifAnimateTimer()
            }
        }

        private fun stopGifAnimate() {
            mGifResource?.let { giResource ->
                mIsSleeping = true
                giResource.stop()
                stopSleepTimer()
                stopGifAnimateTimer()
            }
        }

        private fun showGifImage() {
            mGifImageView.visibility = View.VISIBLE
            mGifImageView.background = mGifResource

            val animator = ValueAnimator.ofInt(0, 255).setDuration(300)
            animator.addUpdateListener { animation ->
                val num = animation.animatedValue as Int
                mGifImageView.imageAlpha = num
            }
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                }
            })
            animator.start()
        }

        private fun hideGifImage() {
            val animator = ValueAnimator.ofInt(255, 0).setDuration(300)
            animator.addUpdateListener { animation ->
                val num = animation.animatedValue as Int
                // TODO：To be faded out because it's not faded out now.
                mGifImageView.imageAlpha = num
            }
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    // GifImageView is not hidden by setVisibility...
                    mGifImageView.visibility = View.GONE
                    // Change background image to hide GifImageView.
                    mGifImageView.background = ColorDrawable(ContextCompat.getColor(applicationContext, R.color.black))
                    // Apply changes.
                    //                    invalidate();
                    Log.d(this.javaClass.canonicalName, " Gif image has been hidden.")
                }
            })
            animator.start()
        }

        private fun setMarginToTimeView() {
            val leftRes: Int

            if (mIsRound) {
                leftRes = R.dimen.time_x_round
            } else {
                leftRes = R.dimen.time_x
            }

            val layoutParams = mTimeView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.setMargins(
                    resources.getDimensionPixelSize(leftRes),
                    resources.getDimensionPixelSize(R.dimen.time_y),
                    0,
                    0
            )
            mTimeView.layoutParams = layoutParams
        }

        private fun formatTwoDigitNumber(hour: Int): String {
            return String.format("%02d", hour)
        }

        /**
         * Start timer to hide gif image.
         */
        private fun startSleepTimer() {
            stopSleepTimer()

            if (mTimeout > 0) {
                mSleepHandler.postDelayed(mSleepRunnable, mTimeout)
            }
        }

        private fun stopSleepTimer() {
            mSleepHandler.removeCallbacks(mSleepRunnable)
        }

        private fun startGifAnimateTimer() {
            stopGifAnimateTimer()
            mGifAnimateHandler.postDelayed(mGifAnimateRunnable, GIF_ANIMATE_DURATION)
        }

        private fun stopGifAnimateTimer() {
            mGifAnimateHandler.removeCallbacks(mGifAnimateRunnable)
        }
    }

    companion object {

        private val GIF_ANIMATE_DURATION: Long = 100
        private val HIDE_GIF_IMAGE_TIMER_MS = TimeUnit.SECONDS.toMillis(30).toInt()
        private val COLON_STRING = " : "
    }
}
