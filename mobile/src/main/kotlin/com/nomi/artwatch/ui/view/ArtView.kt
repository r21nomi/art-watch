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
    private var mArtBinder: ArtBinder? = null
    private var mOnSelect: Action1<PhotoSize>? = null

    fun init(onSelect: Action1<PhotoSize>) {
        mAdapter = ListBindAdapter()
        mOnSelect = onSelect

        mArtBinder = ArtBinder(mAdapter, mOnSelect)

        mAdapter.addBinder(mArtBinder)

        val layoutManager = LinearLayoutManager(context)
        setHasFixedSize(false)
        setLayoutManager(layoutManager)
        adapter = mAdapter
    }

    fun setDataSet(photoSizes: List<PhotoSize>) {
        mArtBinder?.setDataSet(photoSizes)
        mArtBinder?.notifyBinderDataSetChanged()
    }
}
