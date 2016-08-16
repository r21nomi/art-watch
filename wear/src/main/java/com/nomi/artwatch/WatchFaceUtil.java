package com.nomi.artwatch;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import rx.functions.Action1;

/**
 * Created by Ryota Niinomi on 2016/05/10.
 */
public class WatchFaceUtil {

    private static final String SCHEME = "wear";
    private static final String PATH_WITH_FEATURE_LATEST_GIF = "/gif/latest";
    private static final String PATH_WITH_FEATURE_GIF_TIMEOUT = "/gif/timeout";

    public static void fetchGifDataMap(GoogleApiClient client, Action1<DataMap> callback) {
        fetchConfigDataMap(client, PATH_WITH_FEATURE_LATEST_GIF, callback);
    }

    public static void fetchTimeoutDataMap(GoogleApiClient client, Action1<DataMap> callback) {
        fetchConfigDataMap(client, PATH_WITH_FEATURE_GIF_TIMEOUT, callback);
    }

    public static void putGifData(GoogleApiClient googleApiClient, DataMap newConfig, Action1<DataMap> callback) {
        putConfigDataItem(googleApiClient, newConfig, PATH_WITH_FEATURE_LATEST_GIF, callback);
    }

    public static void putTimeoutData(GoogleApiClient googleApiClient, DataMap newConfig, Action1<DataMap> callback) {
        putConfigDataItem(googleApiClient, newConfig, PATH_WITH_FEATURE_GIF_TIMEOUT, callback);
    }

    /**
     * Fetch data item from the storage via using Wearable.DataApi.
     *
     * @param client
     * @param path
     * @param callback
     */
    private static void fetchConfigDataMap(GoogleApiClient client, String path, Action1<DataMap> callback) {
        Log.d(WatchFaceUtil.class.getCanonicalName(), "fetchConfigDataMap start");

        Wearable.NodeApi.getLocalNode(client).setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
            @Override
            public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {
                String localNode = getLocalNodeResult.getNode().getId();
                Uri uri = new Uri.Builder()
                        .scheme(SCHEME)
                        .path(path)
                        .authority(localNode)
                        .build();

                Wearable.DataApi.getDataItem(client, uri)
                        .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                            @Override
                            public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                                Log.d(this.getClass().getCanonicalName(), "fetchConfigDataMap onResult");

                                if (dataItemResult.getStatus().isSuccess()) {
                                    if (dataItemResult.getDataItem() != null) {
                                        DataItem configDataItem = dataItemResult.getDataItem();
                                        DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
                                        DataMap config = dataMapItem.getDataMap();
                                        callback.call(config);

                                    } else {
                                        callback.call(null);
                                    }
                                }
                            }
                        });
            }
        });
    }

    /**
     * Save data item onto the storage via using Wearable.DataApi.
     *
     * @param googleApiClient
     * @param newConfig
     * @param path
     * @param callback
     */
    private static void putConfigDataItem(GoogleApiClient googleApiClient,
                                         DataMap newConfig,
                                         String path,
                                         Action1<DataMap> callback) {
        Log.d(WatchFaceUtil.class.getCanonicalName(), "putConfigDataItem start");

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);
        putDataMapRequest.setUrgent();
        DataMap configToPut = putDataMapRequest.getDataMap();
        configToPut.putAll(newConfig);

        Wearable.DataApi.putDataItem(googleApiClient, putDataMapRequest.asPutDataRequest())
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        Log.d(this.getClass().getCanonicalName(), "putConfigDataItem onResult");
                        if (dataItemResult.getStatus().isSuccess()) {
                            if (dataItemResult.getDataItem() != null) {
                                DataItem configDataItem = dataItemResult.getDataItem();
                                DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
                                DataMap config = dataMapItem.getDataMap();
                                callback.call(config);

                            } else {
                                callback.call(null);
                            }
                        }
                    }
                });
    }
}
