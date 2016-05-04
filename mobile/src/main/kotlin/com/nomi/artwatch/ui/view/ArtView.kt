package com.nomi.artwatch.ui.view

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import com.nomi.artwatch.ui.adapter.binder.ArtBinder
import com.tumblr.jumblr.types.PhotoSize
import com.yqritc.recyclerviewmultipleviewtypesadapter.ListBindAdapter
import rx.functions.Action1

/**
 * Created by Ryota Niinomi on 2015/11/08.
 */
class ArtView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : RecyclerView(context, attrs, defStyle) {

    private var mAdapter: ListBindAdapter = ListBindAdapter()
    private var mOnSelect: Action1<PhotoSize>? = null

    fun init(photos: List<PhotoSize>, onSelect: Action1<PhotoSize>) {
        mAdapter = ListBindAdapter()
        mOnSelect = onSelect

        initAdapter(photos)
    }

    private fun initAdapter(photos: List<PhotoSize>) {
        val artBinder = ArtBinder(mAdapter, photos, mOnSelect)

        mAdapter.addBinder(artBinder)

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        layoutManager.reverseLayout = false
        setHasFixedSize(false)
        setLayoutManager(layoutManager)
        adapter = mAdapter
    }
}
