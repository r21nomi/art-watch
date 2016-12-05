package com.nomi.artwatch.ui.util

import android.content.Context
import android.graphics.Typeface
import timber.log.Timber
import java.util.*

/**
 * Created by Ryota Niinomi on 2016/03/16.
 */
class TypefaceCache {
    private val cache: MutableMap<String, Typeface> = WeakHashMap<String, Typeface>()

    fun getTypeface(resourceId: Int, context: Context): Typeface {
        val fontPath = context.getString(resourceId)
        return getTypeface(fontPath, context)
    }

    fun getTypeface(fontPath: String, context: Context): Typeface {
        var typeface: Typeface? = cache[fontPath]
        if (typeface == null) {
            try {
                typeface = Typeface.createFromAsset(context.assets, fontPath)
                cache.put(fontPath, typeface)
            } catch (e: RuntimeException) {
                Timber.e(e, "Failed to load font. Use system font")
            }

        }
        return typeface ?: Typeface.DEFAULT
    }

    companion object {
        val instance: TypefaceCache by lazy {
            TypefaceCache()
        }
    }
}
