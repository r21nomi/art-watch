package com.nomi.artwatch.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.AppCompatSpinner
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import butterknife.bindView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.nomi.artwatch.R
import com.nomi.artwatch.di.component.ActivityComponent
import com.nomi.artwatch.model.PostModel
import com.nomi.artwatch.ui.adapter.binder.BlogAdapter
import com.nomi.artwatch.ui.view.ArtView
import hugo.weaving.DebugLog
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Ryota Niinomi on 2015/11/04.
 */
class MainActivity : DrawerActivity() {

    private var mGoogleApiClient: GoogleApiClient? = null
    private var mPeerId: String? = null
    private var mBlogAdapter: BlogAdapter? = null
    private var mCurrentBlogName: String? = null

    @Inject
    lateinit var mPostModel: PostModel

    val mSpinner: AppCompatSpinner by bindView(R.id.spinner)
    val mArtView: ArtView by bindView(R.id.art_view)
    val mEmptyView: TextView by bindView(R.id.empty_view)

    val mOnItemSelectedListener: AdapterView.OnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        /**
         * Change blog.
         */
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val blogName: String? = mBlogAdapter?.getItem(position)?.name
            mCurrentBlogName = blogName
            showGifs()

            Timber.d("onItemSelected : " + blogName)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            Timber.d("onNothingSelected")
        }
    }

    private val mGoogleConnectionCallback = object : GoogleApiClient.ConnectionCallbacks {
        override fun onConnected(connectionHint: Bundle?) {
            if (mPeerId != null) {
                Timber.d("Connected to wear.")
            }
        }

        override fun onConnectionSuspended(cause: Int) {
            // TODO：Handling
        }
    }

    private val mGoogleConnectionFailedListener = GoogleApiClient.OnConnectionFailedListener { }// TODO：Error handling

    override val layout: Int = R.layout.activity_main

    override fun injectDependency(component: ActivityComponent) {
        component.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mPeerId = intent.getStringExtra(PEER_ID)
        mGoogleApiClient = GoogleApiClient.Builder(this).addConnectionCallbacks(mGoogleConnectionCallback).addOnConnectionFailedListener(mGoogleConnectionFailedListener).addApi(Wearable.API).build()

        initBlogList()
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient!!.connect()
    }

    override fun onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected) {
            mGoogleApiClient!!.disconnect()
        }
        super.onStop()
    }

    /**
     * Authorize to Tumblr.
     */
    private fun authorize() {
        mLoginModel
                .login()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ authUrl ->
                    Timber.d("Auth URL : " + authUrl)
                    startActivity(Intent("android.intent.action.VIEW", Uri.parse(authUrl)))

                }, { throwable ->
                    Timber.w(throwable, throwable.message)
                })
    }

    /**
     * Initialize blog list.
     */
    private fun initBlogList() {
        mBlogAdapter = BlogAdapter(this);
        mUserModel
                .user
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({user ->
                    if (!user.blogs.isEmpty()) {
                        mCurrentBlogName = user.blogs[0].name
                        showGifs()
                    }
                    mBlogAdapter?.setDataSet(user.blogs)
                    mSpinner.adapter = mBlogAdapter

                }, {throwable ->
                    Timber.e(throwable.message, throwable)
                })

        mSpinner.onItemSelectedListener = mOnItemSelectedListener
    }

    private fun showGifs() {
        if (mLoginModel.isAuthorized) {
            // Already authorized.
            fetchGifPosts()

        } else {
            // Authorize now.
            authorize()
        }
    }

    private fun toggleEmptyView(isEmpty: Boolean) {
        if (isEmpty) {
            mArtView.visibility = View.GONE
            mEmptyView.visibility = View.VISIBLE

        } else {
            mArtView.visibility = View.VISIBLE
            mEmptyView.visibility = View.GONE
        }
    }

    /**
     * Fetch Gif posts from Tumblr.
     */
    private fun fetchGifPosts() {
        mPostModel
                .getPhotoPost(mCurrentBlogName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ items ->
                    mArtView.init(items, Action1 { url -> onGifSelected(url) })
                    toggleEmptyView(items.isEmpty())

                }, { throwable ->
                    Timber.w(throwable, throwable.message)
                })
    }

    /**
     * Send an request to change gif image with selected one.

     * @param url
     */
    @DebugLog
    private fun onGifSelected(url: String) {
        if (mPeerId != null) {
            Glide.with(this).load(url).asGif().into(object : SimpleTarget<GifDrawable>() {
                override fun onResourceReady(resource: GifDrawable, glideAnimation: GlideAnimation<in GifDrawable>) {
                    // Convert GifDrawable to Asset.
                    val asset = createAssetFromDrawable(resource)

                    val dataMap = PutDataMapRequest.create(PATH_OF_GIF)
                    dataMap.dataMap.putAsset(KEY_GIF, asset)
                    val request = dataMap.asPutDataRequest()

                    // Send the request.
                    Wearable.DataApi.putDataItem(mGoogleApiClient, request)

                    Toast.makeText(this@MainActivity, "changed", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    /**
     * Create asset for sending the gif data to wear.

     * @param drawable
     * *
     * @return
     */
    private fun createAssetFromDrawable(drawable: GifDrawable): Asset {
        val byteArray = drawable.data
        return Asset.createFromBytes(byteArray)
    }

    companion object {
        private val PEER_ID = "peer_id"
        private val PATH_OF_GIF = "/gif"
        private val KEY_GIF = "gif"

        fun createIntent(context: Context, peerId: String?): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(PEER_ID, peerId)
            return intent
        }
    }
}
