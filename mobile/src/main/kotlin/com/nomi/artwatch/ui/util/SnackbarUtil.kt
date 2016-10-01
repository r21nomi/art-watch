package com.nomi.artwatch.ui.util

import android.app.Activity
import android.support.annotation.ColorRes
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import com.nomi.artwatch.R

/**
 * Created by Ryota Niinomi on 2016/10/01.
 */
class SnackbarUtil {
    companion object {
        fun showNotification(activity: Activity, message: String) {
            show(activity, message, ContextCompat.getColor(activity, R.color.green))
        }

        fun showAlert(activity: Activity, message: String) {
            show(activity, message, ContextCompat.getColor(activity, R.color.red))
        }

        private fun show(activity: Activity,
                         message: String,
                         @ColorRes color: Int) {
            val snackbar = Snackbar.make(
                    activity.findViewById(android.R.id.content),
                    message,
                    Snackbar.LENGTH_SHORT
            )
            snackbar.view.setBackgroundColor(color)
            snackbar.show()
        }
    }
}