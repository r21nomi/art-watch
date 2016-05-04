package com.nomi.artwatch.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import butterknife.bindView
import com.nomi.artwatch.R
import com.nomi.artwatch.di.component.ActivityComponent
import com.nomi.artwatch.model.PostModel
import com.nomi.artwatch.ui.adapter.binder.BlogAdapter
import com.nomi.artwatch.ui.view.ArtView
import com.tumblr.jumblr.types.Photo
import com.tumblr.jumblr.types.PhotoSize
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Ryota Niinomi on 2015/11/04.
 */
class MainActivity : DrawerActivity() {

    private var mBlogAdapter: BlogAdapter? = null
    private var mCurrentBlogName: String? = null

    @Inject
    lateinit var mPostModel: PostModel

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

    override val layout: Int = R.layout.activity_main
    override val toolbarName: Int = R.string.app_name

    override fun injectDependency(component: ActivityComponent) {
        component.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBlogList()
    }

    /**
     * Authorize to Tumblr.
     */
    private fun authorize() {
        val subscription = mLoginModel
                .login()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ authUrl ->
                    Timber.d("Auth URL : " + authUrl)
                    startActivity(Intent("android.intent.action.VIEW", Uri.parse(authUrl)))

                }, { throwable ->
                    Timber.w(throwable, throwable.message)
                })
        mSubscriptionsOnDestroy.add(subscription)
    }

    /**
     * Initialize blog list.
     */
    private fun initBlogList() {
        mBlogAdapter = BlogAdapter(this)
        val subscription = mUserModel
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
        mSubscriptionsOnDestroy.add(subscription)

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
        val subscription = mPostModel
                .getPhotoPost(mCurrentBlogName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ items ->
                    mArtView.init(toPhotoSize(items), Action1 { photoSize -> onGifSelected(photoSize) })
                    toggleEmptyView(items.isEmpty())

                }, { throwable ->
                    Timber.w(throwable, throwable.message)
                })
        mSubscriptionsOnDestroy.add(subscription)
    }

    private fun toPhotoSize(list: List<Photo>): List<PhotoSize> {
        return Observable
                .from(list)
                .map { it.originalSize }
                .toList()
                .toBlocking()
                .single()
    }

    companion object {
        fun createIntent(context: Context): Intent {
            val intent = Intent(context, MainActivity::class.java)
            return intent
        }
    }
}
