package com.nomi.artwatch.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.nomi.artwatch.R
import com.nomi.artwatch.ui.util.TypefaceCache

/**
 * Created by Ryota Niinomi on 2016/03/16.
 */
class CustomFontTextView : TextView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setFont(context, attrs ?: return)
    }

    private fun setFont(context: Context, attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomFontTextView)

        val font = typedArray.getInt(R.styleable.CustomFontTextView_fontType, 0)
        val fontFamily = FONT_FAMILIES[font]

        val typeface = TypefaceCache.instance.getTypeface(fontFamily, context)
        setTypeface(typeface)
        typedArray.recycle()
    }

    companion object {
        private val FONT_FAMILIES = intArrayOf(R.string.font_light)
    }
}