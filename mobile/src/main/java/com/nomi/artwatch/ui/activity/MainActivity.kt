package com.nomi.artwatch.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import butterknife.bindView
import com.nomi.artwatch.GifUrlProvider
import com.nomi.artwatch.GifUrlProvider.Type
import com.nomi.artwatch.R
import com.nomi.artwatch.data.entity.Gif
import com.nomi.artwatch.di.component.ActivityComponent
import com.nomi.artwatch.model.PostModel
import com.nomi.artwatch.ui.adapter.binder.BlogAdapter
import com.nomi.artwatch.ui.util.SnackbarUtil
import com.nomi.artwatch.ui.view.ArtView
import com.tumblr.jumblr.types.Photo
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Ryota Niinomi on 2015/11/04.
 */
class MainActivity : DrawerActivity() {

    companion object {
        fun createIntent(context: Context) = Intent(context, MainActivity::class.java)
    }

    private var mBlogAdapter: BlogAdapter? = null

    @Inject
    lateinit var mPostModel: PostModel

    val mArtView: ArtView by bindView(R.id.art_view)
    val mEmptyView: TextView by bindView(R.id.empty_view)

    val mOnItemSelectedListener: AdapterView.OnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        /**
         * Change blog.
         */
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val blog = mBlogAdapter?.getItem(position)
            mBlogModel.currentBlog = blog
            showGifs()
            setUserThumb(blog?.name ?: "")

            Timber.d("onItemSelected : " + blog?.name)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            Timber.d("onNothingSelected")
        }
    }

    override val layout: Int = R.layout.activity_main
    override val toolbarName: Int = R.string.home
    override val shouldShowSpinner: Boolean get() = true

    override fun injectDependency(component: ActivityComponent) {
        component.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mArtView.init(Action1 { photoSize -> onGifSelected(photoSize) })
        initBlogList()
    }

    /**
     * Authorize to Tumblr.
     */
    private fun authorize() {
        val subscription = mLoginModel
                .login()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Timber.d("Auth URL : " + it)
                    startActivity(Intent("android.intent.action.VIEW", Uri.parse(it)))
                }, {
                    SnackbarUtil.showAlert(this, it.message ?: return@subscribe)
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
                .subscribe({
                    if (!it.blogs.isEmpty()) {
                        mBlogModel.currentBlog = it.blogs[0]
                    }
                    // AdapterView.OnItemSelectedListener#onItemSelected will be called and then,
                    // showGifs will be executed.
                    mBlogAdapter?.setDataSet(it.blogs)
                    mSpinner.adapter = mBlogAdapter

                }, {
                    SnackbarUtil.showAlert(this, it.message ?: return@subscribe)
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
        mPostModel
                .getPhotoPost(mBlogModel.currentBlog?.name ?: return)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mArtView.setDataSet(toGif(it))
                    toggleEmptyView(it.isEmpty())
                }, {
                    SnackbarUtil.showAlert(this, it.message ?: return@subscribe)
                })
                .apply { mSubscriptionsOnDestroy.add(this) }
    }

    /**
     * To Gif entity.
     */
    private fun toGif(list: List<Photo>): List<Gif> {
        return Observable
                .from(list)
                .map { Gif(GifUrlProvider.getUrl(it.sizes, Type.MOBILE), it.sizes, it.caption) }
                .toList()
                .toBlocking()
                .single()
    }
}
