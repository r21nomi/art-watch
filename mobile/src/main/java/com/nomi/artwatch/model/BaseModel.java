package com.nomi.artwatch.model;

import com.nomi.artwatch.Config;
import com.tumblr.jumblr.JumblrClient;

/**
 * Created by Ryota Niinomi on 2015/11/03.
 */
public class BaseModel {

    public BaseModel() {

    }

    protected JumblrClient getClient() {
        JumblrClient client = new JumblrClient(Config.CONSUMER_KEY, Config.CONSUMER_SECRET);
        client.setToken(Config.OAUTH_TOKEN, Config.OAUTH_TOKEN_SECRET);

        return client;
    }
}
