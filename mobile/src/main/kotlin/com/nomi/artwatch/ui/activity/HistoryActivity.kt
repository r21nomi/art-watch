package com.nomi.artwatch.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import butterknife.bindView
import com.nomi.artwatch.R
import com.nomi.artwatch.data.cache.GifCache
import com.nomi.artwatch.di.component.ActivityComponent
import com.nomi.artwatch.ui.view.ArtView
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import timber.log.Timber

/**
 * Created by Ryota Niinomi on 2016/05/04.
 */
class HistoryActivity : DrawerActivity() {

    companion object {
        private val QUERY_SELECTED_ITEMS = "SELECT * FROM " + GifCache.TABLE
        private val QUERY_LATEST_ITEM = "SELECT * FROM " +
                GifCache.TABLE +
                " ORDER BY " +
                GifCache.UPDATED_AT +
                " DESC limit 1"

        fun createIntent(context: Context): Intent {
            val intent = Intent(context, HistoryActivity::class.java)
            return intent
        }
    }

    val mArtView: ArtView by bindView(R.id.art_view)
    val mEmptyView: TextView by bindView(R.id.empty_view)

    override fun injectDependency(component: ActivityComponent?) {
        component?.inject(this)
    }

    override val layout: Int get() = R.layout.activity_history
    override val toolbarName: Int get() = R.string.history
    override val shouldShowSpinner: Boolean get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mArtView.init(Action1 { gif -> onGifSelected(gif) })

        fetchHistoryItems()
    }

    private fun fetchHistoryItems() {
        val subscription = mDb.createQuery(GifCache.TABLE, QUERY_SELECTED_ITEMS)
                .mapToList { cursor ->
                    return@mapToList GifCache.toGif(cursor)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({gifs ->
                    if (gifs.isNotEmpty()) {
                        mArtView.setDataSet(gifs)

                    } else {
                        toggleEmptyView(true)
                    }
                }, {throwable ->
                    Timber.e(throwable, throwable.message)
                    toggleEmptyView(true)
                })
        mSubscriptionsOnDestroy.add(subscription)
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
}