package com.nomi.artwatch.util;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import timber.log.Timber;

/**
 * Created by Ryota Niinomi on 2016/05/05.
 */
public class JsonUtil {

    private static final String UNIQUE_ARRAYS = "unique_arrays";

    /**
     * Stringify given list.
     *
     * @param originalList
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> String toJsonArrayString(List<T> originalList, Class<T> clazz) {
        String jsonArrayString = "";

        try {
            List<String> stringList = Observable
                    .from(originalList)
                    .map(item -> new Gson().toJson(item, clazz))
                    .toList()
                    .toBlocking()
                    .single();

            JSONObject json = new JSONObject();
            json.put(UNIQUE_ARRAYS, new JSONArray(stringList));
            jsonArrayString = json.toString();

        } catch (JSONException e) {
            Timber.e(e, e.getLocalizedMessage());
        }

        return jsonArrayString;
    }

    /**
     * Parse given string and return as list.
     *
     * @param listString
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> toList(String listString, Class<T> clazz) {
        List<T> parsedList = new ArrayList<>();

        try {
            JSONObject json = new JSONObject(listString);
            JSONArray jArray = json.optJSONArray(UNIQUE_ARRAYS);
            List<String> stringList = new ArrayList<>();

            if (jArray != null) {
                for (int i = 0; i < jArray.length(); i++){
                    stringList.add(jArray.get(i).toString());
                }
                parsedList = Observable.from(stringList)
                        .map(item -> new Gson().fromJson(item, clazz))
                        .toList()
                        .toBlocking()
                        .single();
            }
        } catch (JSONException e) {
            Timber.e(e, e.getLocalizedMessage());
        }

        return parsedList;
    }
}
