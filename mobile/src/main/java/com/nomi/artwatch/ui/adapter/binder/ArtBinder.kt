package com.nomi.artwatch.ui.adapter.binder

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.bindView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.nomi.artwatch.R
import com.nomi.artwatch.data.entity.Gif
import com.nomi.artwatch.util.WindowUtil
import com.yqritc.recyclerviewmultipleviewtypesadapter.DataBindAdapter
import com.yqritc.recyclerviewmultipleviewtypesadapter.DataBinder
import pl.droidsonroids.gif.GifImageView
import rx.functions.Action1
import java.util.*

/**
 * Created by Ryota Niinomi on 15/11/8.
 */
class ArtBinder(dataBindAdapter: DataBindAdapter, private val mListener: Action1<Gif>?)
    : DataBinder<ArtBinder.ViewHolder>(dataBindAdapter) {

    private val mDataSet: MutableList<Gif> = ArrayList()

    override fun newViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_art, parent, false)

        return ViewHolder(view)
    }

    override fun bindViewHolder(holder: ViewHolder, position: Int) {
        val photoSize = mDataSet[position]

        Glide.with(holder.gifView.context)
                .load(photoSize.originalGifUrl)
                .asGif()
                .into(object : SimpleTarget<GifDrawable>() {
                    override fun onResourceReady(resource: GifDrawable, glideAnimation: GlideAnimation<in GifDrawable>) {
                        val width = WindowUtil.getWidth(holder.gifView.context)
                        val height = width * resource.intrinsicHeight / resource.intrinsicWidth
                        val params = holder.gifView.layoutParams
                        params.width = width
                        params.height = height
                        holder.gifView.background = resource

                        resource.start()
                    }
                })
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(gifs: List<Gif>) {
        mDataSet.clear()
        addDataSet(gifs)
    }

    fun addDataSet(gifs: List<Gif>) {
        mDataSet.addAll(gifs)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val gifView: GifImageView by bindView(R.id.gifView)

        init {
            gifView.setOnClickListener {
                val photoSize = mDataSet[adapterPosition]
                mListener?.call(photoSize)
            }
        }
    }
}
