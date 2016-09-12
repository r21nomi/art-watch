package com.nomi.artwatch.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.nomi.artwatch.Application
import com.nomi.artwatch.ui.activity.MainActivity
import com.nomi.artwatch.util.StringUtil
import rx.Observable

/**
 * Created by Ryota Niinomi on 2016/09/12.
 */
class DeepLinkRouter {

    companion object {
        val LOGIN = "login"

        fun createIntent(context: Context, uri: Uri): Observable<Intent> {
            val host = uri.getHost()
            var path = uri.getPath()
            if (path.startsWith("/")) {
                path = path.substring(1)
            }
            val pathParams = StringUtil.split(path, '/')

            if (LOGIN.equals(host) && pathParams.size == 1) {
                // ex. artwatch://login/xxxx
                val intent = MainActivity.createIntent(context)
                Application.setPeerId(pathParams[0])
                return Observable.just(intent)

            } else {
                return Observable.just(null)
            }
        }
    }
}