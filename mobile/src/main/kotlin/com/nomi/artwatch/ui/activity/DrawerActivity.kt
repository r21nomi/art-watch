package com.nomi.artwatch.ui.activity

import android.content.Intent
import android.content.res.Configuration
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatSpinner
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import butterknife.bindView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.*
import com.nomi.artwatch.GifUrlProvider
import com.nomi.artwatch.GifUrlProvider.Type
import com.nomi.artwatch.R
import com.nomi.artwatch.data.cache.GifCache
import com.nomi.artwatch.data.entity.Gif
import com.nomi.artwatch.model.BlogModel
import com.nomi.artwatch.model.LoginModel
import com.nomi.artwatch.model.UserModel
import com.squareup.sqlbrite.BriteDatabase
import hugo.weaving.DebugLog
import jp.wasabeef.glide.transformations.CropCircleTransformation
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Ryota Niinomi on 15/12/06.
 */
abstract class DrawerActivity : InjectActivity() {

    companion object {
        val SCHEME = "wear"
        private val PATH_OF_GIF = "/gif"
        private val KEY_GIF = "gif"
    }

    protected var mGoogleApiClient: GoogleApiClient? = null
    private var mDrawerToggle: ActionBarDrawerToggle? = null

    private val mGoogleConnectionCallback = object : GoogleApiClient.ConnectionCallbacks {
        override fun onConnected(connectionHint: Bundle?) {
            this@DrawerActivity.onConnected(connectionHint)
        }

        override fun onConnectionSuspended(cause: Int) {
            this@DrawerActivity.onConnectionSuspended(cause)
        }
    }

    private val mDataListener = object : DataApi.DataListener {
        override fun onDataChanged(dataEvents: DataEventBuffer) {
            this@DrawerActivity.onDataChanged(dataEvents)
        }
    }

    private val mGoogleConnectionFailedListener = GoogleApiClient.OnConnectionFailedListener { }// TODO：Error handling

    @Inject
    lateinit var mLoginModel: LoginModel
    @Inject
    lateinit var mUserModel: UserModel
    @Inject
    lateinit var mBlogModel: BlogModel
    @Inject
    lateinit var mDb: BriteDatabase

    val mDrawerLayout: DrawerLayout by bindView(R.id.drawer_layout)
    val mSpinner: AppCompatSpinner by bindView(R.id.spinner)
    val mContainer: ViewGroup by bindView(R.id.container)
    val mUserThumb: ImageView by bindView(R.id.userThumb)
    val mUserName: TextView by bindView(R.id.userName)
    val mHomeBtn: TextView by bindView(R.id.homeButton)
    val mHistoryBtn: TextView by bindView(R.id.historyButton)
    val mSettingsBtn: TextView by bindView(R.id.settingsButton)
    val mLogoutBtn: TextView by bindView(R.id.logoutButton)

    protected abstract val layout: Int
    protected abstract val toolbarName: Int
    protected abstract val shouldShowSpinner: Boolean

    open fun onConnected(connectionHint: Bundle?) {
        Wearable.DataApi.addListener(mGoogleApiClient, mDataListener)
    }

    open fun onConnectionSuspended(cause: Int) {
        // TODO：Handling
    }

    open fun onDataChanged(dataEvents: DataEventBuffer) {
        // no-op
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawer_layout)

        if (layout != 0) {
            layoutInflater.inflate(layout, mContainer)
        }

        mSpinner.visibility = if (shouldShowSpinner) View.VISIBLE else View.GONE

        mGoogleApiClient = GoogleApiClient
                .Builder(this)
                .addConnectionCallbacks(mGoogleConnectionCallback)
                .addOnConnectionFailedListener(mGoogleConnectionFailedListener)
                .addApi(Wearable.API)
                .build()

        initDrawer()
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient!!.connect()
    }

    override fun onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected) {
            Wearable.DataApi.removeListener(mGoogleApiClient, mDataListener)
            mGoogleApiClient!!.disconnect()
        }
        super.onStop()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        mDrawerToggle!!.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        mDrawerToggle!!.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (mDrawerToggle!!.onOptionsItemSelected(item)) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * Send an request to change gif image with selected one.
     */
    @DebugLog
    fun onGifSelected(gif: Gif) {
        if (!mGoogleApiClient!!.isConnected) {
            Toast.makeText(this@DrawerActivity, "Please connect to Android Wear", Toast.LENGTH_SHORT).show()
            return
        }
        updateGifCache(gif)

        Glide.with(this).load(GifUrlProvider.getUrl(gif.photoSizes, Type.WEAR)).asGif().into(object : SimpleTarget<GifDrawable>() {
            override fun onResourceReady(resource: GifDrawable, glideAnimation: GlideAnimation<in GifDrawable>) {
                // Convert GifDrawable to Asset.
                val asset = createAssetFromDrawable(resource)

                val dataMap = PutDataMapRequest.create(PATH_OF_GIF)
                dataMap.dataMap.putAsset(KEY_GIF, asset)
                val request = dataMap.asPutDataRequest()

                // Send the request.
                Wearable.DataApi.putDataItem(mGoogleApiClient, request)

                Toast.makeText(this@DrawerActivity, "changed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Update GifCache table.
     */
    private fun updateGifCache(gif: Gif) {
        val updatedAt = System.currentTimeMillis()
        mDb.insert(GifCache.TABLE, GifCache
                .Builder()
                .originalGifUrl(gif.originalGifUrl)
                .photoSizes(gif.photoSizes)
                .caption(gif.caption)
                .updatedAt(updatedAt)
                .build(), SQLiteDatabase.CONFLICT_REPLACE)
    }

    /**
     * Create asset for sending the gif data to wear.
     */
    private fun createAssetFromDrawable(drawable: GifDrawable): Asset {
        val byteArray = drawable.data
        return Asset.createFromBytes(byteArray)
    }

    private fun initDrawer() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        mDrawerToggle = object : ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbarName,
                toolbarName) {
            override fun onDrawerClosed(view: View?) {
                super.onDrawerClosed(view)
            }

            override fun onDrawerOpened(drawerView: View?) {
                super.onDrawerOpened(drawerView)
            }
        }

        mDrawerLayout.setDrawerListener(mDrawerToggle)
        mDrawerToggle!!.isDrawerIndicatorEnabled = true

        supportActionBar!!.title = getString(toolbarName)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        mHomeBtn.setOnClickListener({
            startHomeActivity()
        })

        mHistoryBtn.setOnClickListener({
            startHistoryActivity()
        })

        mSettingsBtn.setOnClickListener({
            startSettingsActivity()
        })

        mLogoutBtn.setOnClickListener({
            logout()
        })

        // User info
        val subscription = mUserModel
                .user
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ user ->
                    setUserThumb()
                    mUserName.text = user.name
                }, { throwable ->
                    Timber.e(throwable, throwable.message)
                })
        mSubscriptionsOnDestroy.add(subscription)
    }

    private fun logout() {
        AlertDialog.Builder(this, R.style.DefaultDialog)
                .setTitle("Logout")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", {dialogInterface, i ->
                    mLoginModel.logout().subscribe({aVoid -> startActivity()})
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show()
    }

    private fun startHomeActivity() {
        mDrawerLayout.closeDrawer(GravityCompat.START)
        val intent: Intent = MainActivity.createIntent(this)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun startHistoryActivity() {
        mDrawerLayout.closeDrawer(GravityCompat.START)
        val intent: Intent = HistoryActivity.createIntent(this)
        startActivity(intent)
    }

    private fun startSettingsActivity() {
        mDrawerLayout.closeDrawer(GravityCompat.START)
        val intent: Intent = SettingActivity.createIntent(this)
        startActivity(intent)
    }

    private fun setUserThumb() {
        val subscription = mBlogModel
                .getAvatar("ryotaniinomi")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ url ->
                    Glide.with(this)
                            .load(url)
                            .bitmapTransform(CropCircleTransformation(this))
                            .into(mUserThumb)
                }, { throwable ->
                    Timber.e(throwable, throwable.message)
                })
        mSubscriptionsOnDestroy.add(subscription)
    }

    private fun startActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
