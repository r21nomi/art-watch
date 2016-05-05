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
import com.nomi.artwatch.util.WindowUtil
import com.tumblr.jumblr.types.PhotoSize
import com.yqritc.recyclerviewmultipleviewtypesadapter.DataBindAdapter
import com.yqritc.recyclerviewmultipleviewtypesadapter.DataBinder
import pl.droidsonroids.gif.GifImageView
import rx.functions.Action1
import java.util.*

/**
 * Created by Ryota Niinomi on 15/11/8.
 */
class ArtBinder(dataBindAdapter: DataBindAdapter,
                private val mListener: Action1<PhotoSize>?) : DataBinder<ArtBinder.ViewHolder>(dataBindAdapter) {

    private val mDataSet: MutableList<PhotoSize> = ArrayList()

    override fun newViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_art, parent, false)

        return ViewHolder(view)
    }

    override fun bindViewHolder(holder: ViewHolder, position: Int) {
        val photoSize = mDataSet[position]

        holder.mPosition = position

        Glide.with(holder.mGifView.context)
                .load(photoSize.url)
                .asGif()
                .into(object : SimpleTarget<GifDrawable>() {
                    override fun onResourceReady(resource: GifDrawable, glideAnimation: GlideAnimation<in GifDrawable>) {
                        val width = WindowUtil.getWidth(holder.mGifView.context)
                        val height = width * resource.intrinsicHeight / resource.intrinsicWidth
                        val params = holder.mGifView.layoutParams
                        params.width = width
                        params.height = height
                        holder.mGifView.background = resource

                        resource.start()
                    }
                })
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun setDataSet(photoSizes: List<PhotoSize>) {
        mDataSet.clear()
        mDataSet.addAll(photoSizes)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mPosition: Int = 0

        val mGifView: GifImageView by bindView(R.id.gifView)

        init {
            mGifView.setOnClickListener({
                val photoSize = mDataSet[mPosition]
                mListener?.call(photoSize)
            })
        }
    }
}
