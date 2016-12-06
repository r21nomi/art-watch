package com.nomi.artwatch.util

import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import rx.Observable
import timber.log.Timber
import java.util.*

/**
 * Created by Ryota Niinomi on 2016/05/05.
 */
object JsonUtil {

    private val UNIQUE_ARRAYS = "unique_arrays"

    /**
     * Stringify given list.

     * @param originalList
     * *
     * @param clazz
     * *
     * @param <T>
     * *
     * @return
    </T> */
    fun <T> toJsonArrayString(originalList: List<T>, clazz: Class<T>): String {
        var jsonArrayString = ""

        try {
            val stringList = Observable
                    .from(originalList)
                    .map { item -> Gson().toJson(item, clazz) }
                    .toList()
                    .toBlocking()
                    .single()

            val json = JSONObject()
            json.put(UNIQUE_ARRAYS, JSONArray(stringList))
            jsonArrayString = json.toString()

        } catch (e: JSONException) {
            Timber.e(e, e.message)
        }

        return jsonArrayString
    }

    /**
     * Parse given string and return as list.

     * @param listString
     * *
     * @param clazz
     * *
     * @param <T>
     * *
     * @return
    </T> */
    fun <T> toList(listString: String, clazz: Class<T>): List<T> {
        var parsedList: List<T> = ArrayList()

        try {
            val json = JSONObject(listString)
            val jArray = json.optJSONArray(UNIQUE_ARRAYS)
            val stringList = ArrayList<String>()

            if (jArray != null) {
                for (i in 0..jArray.length() - 1) {
                    stringList.add(jArray.get(i).toString())
                }
                parsedList = Observable.from(stringList)
                        .map { item -> Gson().fromJson<T>(item, clazz) }
                        .toList()
                        .toBlocking()
                        .single()
            }
        } catch (e: JSONException) {
            Timber.e(e, e.message)
        }

        return parsedList
    }
}
