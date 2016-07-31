package com.nomi.artwatch.ui.util

import android.util.Log
import com.google.firebase.crash.FirebaseCrash
import timber.log.Timber

/**
 * Created by Ryota Niinomi on 2016/07/31.
 */
class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String?, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }
        if (priority != Log.ERROR) {
            FirebaseCrash.log(message)
            return
        }
        FirebaseCrash.report(t)
    }
}