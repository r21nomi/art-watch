package com.nomi.artwatch.model

import com.nomi.artwatch.Config
import com.tumblr.jumblr.JumblrClient

/**
 * Created by Ryota Niinomi on 2015/11/03.
 */
open class BaseModel(protected var mPrefModel: PrefModel) {

    protected val client: JumblrClient by lazy {
        val client = JumblrClient(Config.CONSUMER_KEY, Config.CONSUMER_SECRET)
        client.setToken(mPrefModel.token, mPrefModel.tokenSecret)
        return@lazy client
    }
}
