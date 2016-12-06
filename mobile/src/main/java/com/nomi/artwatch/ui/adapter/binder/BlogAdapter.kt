package com.nomi.artwatch.ui.adapter.binder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.nomi.artwatch.R
import com.tumblr.jumblr.types.Blog
import java.util.*

/**
 * Created by Ryota Niinomi on 2016/03/26.
 */
class BlogAdapter(context: Context) : BaseAdapter() {

    private val mInflater: LayoutInflater by lazy {
        LayoutInflater.from(context)
    }

    private val mDataSet = ArrayList<Blog>()

    override fun getCount(): Int {
        return mDataSet.size
    }

    override fun getItem(position: Int): Blog {
        return mDataSet[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = mInflater.inflate(R.layout.list_row, parent, false)
            holder = ViewHolder(view)
            view.tag = holder

        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        holder.label.text = mDataSet[position].name
        return view
    }

    fun setDataSet(dataSet: List<Blog>) {
        mDataSet.addAll(dataSet)
    }

    private class ViewHolder(view: View) {
        val label: TextView by lazy {
            view.findViewById(R.id.label) as TextView
        }
    }
}