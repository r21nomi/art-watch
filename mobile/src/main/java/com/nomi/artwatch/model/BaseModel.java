package com.nomi.artwatch.model;

import com.nomi.artwatch.Config;
import com.tumblr.jumblr.JumblrClient;

/**
 * Created by Ryota Niinomi on 2015/11/03.
 */
public class BaseModel {

    protected PrefModel mPrefModel;

    public BaseModel(PrefModel prefModel) {
        mPrefModel = prefModel;
    }

    protected JumblrClient getClient() {
        JumblrClient client = new JumblrClient(Config.CONSUMER_KEY, Config.CONSUMER_SECRET);
        client.setToken(mPrefModel.getToken(), mPrefModel.getTokenSecret());

        return client;
    }
}
