package com.nomi.artwatch

import android.net.Uri
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.wearable.*
import rx.functions.Action1

/**
 * Created by Ryota Niinomi on 2016/05/10.
 */
object WatchFaceUtil {

    val SCHEME = "wear"
    val PATH_OF_GIF = "/gif"
    val PATH_OF_TIMEOUT = "/timeout"
    val KEY_GIF = "gif"
    val KEY_TIMEOUT = "timeout"

    fun fetchGifDataMap(client: GoogleApiClient, callback: Action1<DataMap>) {
        fetchConfigDataMap(client, PATH_OF_GIF, callback)
    }

    fun fetchTimeoutDataMap(client: GoogleApiClient, callback: Action1<DataMap>) {
        fetchConfigDataMap(client, PATH_OF_TIMEOUT, callback)
    }

    fun putGifData(googleApiClient: GoogleApiClient, newConfig: DataMap, callback: Action1<DataMap>) {
        putConfigDataItem(googleApiClient, newConfig, PATH_OF_GIF, callback)
    }

    fun putTimeoutData(googleApiClient: GoogleApiClient, newConfig: DataMap, callback: Action1<DataMap>) {
        putConfigDataItem(googleApiClient, newConfig, PATH_OF_TIMEOUT, callback)
    }

    /**
     * Fetch data item from the storage via using Wearable.DataApi.

     * @param client
     * *
     * @param path
     * *
     * @param callback
     */
    private fun fetchConfigDataMap(client: GoogleApiClient, path: String, callback: Action1<DataMap>) {
        Log.d(WatchFaceUtil::class.java.canonicalName, "fetchConfigDataMap start")

        Wearable.NodeApi.getLocalNode(client).setResultCallback { getLocalNodeResult ->
            val localNode = getLocalNodeResult.node.id
            val uri = Uri.Builder()
                    .scheme(SCHEME)
                    .path(path)
                    .authority(localNode)
                    .build()

            Wearable.DataApi.getDataItem(client, uri)
                    .setResultCallback(object : ResultCallback<DataApi.DataItemResult> {
                        override fun onResult(dataItemResult: DataApi.DataItemResult) {
                            Log.d(this.javaClass.canonicalName, "fetchConfigDataMap onResult")

                            if (!dataItemResult.status.isSuccess) return

                            if (dataItemResult.dataItem != null) {
                                val configDataItem = dataItemResult.dataItem
                                val dataMapItem = DataMapItem.fromDataItem(configDataItem)
                                val config = dataMapItem.dataMap
                                callback.call(config)

                            } else {
                                callback.call(null)
                            }
                        }
                    })
        }
    }

    /**
     * Save data item onto the storage via using Wearable.DataApi.

     * @param googleApiClient
     * *
     * @param newConfig
     * *
     * @param path
     * *
     * @param callback
     */
    private fun putConfigDataItem(googleApiClient: GoogleApiClient,
                                  newConfig: DataMap,
                                  path: String,
                                  callback: Action1<DataMap>) {
        Log.d(WatchFaceUtil::class.java.canonicalName, "putConfigDataItem start")

        val putDataMapRequest = PutDataMapRequest.create(path)
        putDataMapRequest.setUrgent()
        val configToPut = putDataMapRequest.dataMap
        configToPut.putAll(newConfig)

        Wearable.DataApi.putDataItem(googleApiClient, putDataMapRequest.asPutDataRequest())
                .setResultCallback(object : ResultCallback<DataApi.DataItemResult> {
                    override fun onResult(dataItemResult: DataApi.DataItemResult) {
                        Log.d(this.javaClass.canonicalName, "putConfigDataItem onResult")

                        if (!dataItemResult.status.isSuccess) return

                        if (dataItemResult.dataItem != null) {
                            val configDataItem = dataItemResult.dataItem
                            val dataMapItem = DataMapItem.fromDataItem(configDataItem)
                            val config = dataMapItem.dataMap
                            callback.call(config)

                        } else {
                            callback.call(null)
                        }
                    }
                })
    }
}
