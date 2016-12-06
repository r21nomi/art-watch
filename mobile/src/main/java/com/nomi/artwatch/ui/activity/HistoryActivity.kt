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
import com.nomi.artwatch.ui.util.SnackbarUtil
import com.nomi.artwatch.ui.view.ArtView
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1

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

    override fun injectDependency(component: ActivityComponent) {
        component.inject(this)
    }

    override val layout: Int = R.layout.activity_history
    override val toolbarName: Int = R.string.history
    override val shouldShowSpinner: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mArtView.init(Action1 { onGifSelected(it) })

        fetchHistoryItems()
    }

    private fun fetchHistoryItems() {
        mDb.createQuery(GifCache.TABLE, QUERY_SELECTED_ITEMS)
                .mapToList {
                    return@mapToList GifCache.toGif(it)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.isNotEmpty()) {
                        mArtView.setDataSet(it)

                    } else {
                        toggleEmptyView(true)
                    }
                }, {
                    SnackbarUtil.showAlert(this, it.message ?: return@subscribe)
                    toggleEmptyView(true)
                })
                .apply { mSubscriptionsOnDestroy.add(this) }
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