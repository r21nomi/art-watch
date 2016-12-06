package com.nomi.artwatch.ui.util

import android.content.Intent
import android.net.Uri
import com.nomi.artwatch.util.StringUtil
import rx.Observable

/**
 * Created by Ryota Niinomi on 2016/09/12.
 */
class DeepLinkRouter {

    companion object {
        val LOGIN = "login"

        fun createIntent(intent: Intent, uri: Uri): Observable<Intent> {
            val host = uri.getHost()
            var path = uri.getPath()
            if (path.startsWith("/")) {
                path = path.substring(1)
            }
            val pathParams = StringUtil.split(path, '/')

            if (fromTumblrOAuth(host, pathParams)) {
                // ex. artwatch://login
                // FIXME：ここでIntentをつくるとたまにjava.lang.NoClassDefFoundError: com.nomi.artwatch.MainActivity
                // が発生することがある。
                // これを避けるために呼び出し側で作成したIntentを渡すようにした
//                val intent = MainActivity.createIntent(context)
                return Observable.just(intent)

            } else {
                return Observable.just(null)
            }
        }

        private fun fromTumblrOAuth(host: String, pathParams: List<String>) : Boolean =
                LOGIN == host && pathParams.isEmpty()
    }
}