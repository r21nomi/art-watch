package com.nomi.artwatch.ui.view

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import com.nomi.artwatch.data.entity.Gif
import com.nomi.artwatch.ui.adapter.binder.ArtBinder
import com.yqritc.recyclerviewmultipleviewtypesadapter.ListBindAdapter
import rx.functions.Action1

/**
 * Created by Ryota Niinomi on 2015/11/08.
 */
class ArtView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : RecyclerView(context, attrs, defStyle) {

    private val mAdapter: ListBindAdapter = ListBindAdapter()
    private val mArtBinder: ArtBinder by lazy {
        ArtBinder(mAdapter, mOnSelect)
    }
    private var mOnSelect: Action1<Gif>? = null

    fun init(onSelect: Action1<Gif>) {
        mOnSelect = onSelect

        mAdapter.addBinder(mArtBinder)

        val layoutManager = LinearLayoutManager(context)
        setHasFixedSize(false)
        setLayoutManager(layoutManager)
        adapter = mAdapter
    }

    fun setDataSet(gifs: List<Gif>) {
        mArtBinder.setDataSet(gifs)
        mArtBinder.notifyDataSetChanged()
    }
}
