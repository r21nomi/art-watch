package com.nomi.artwatch.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import butterknife.bindView
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.wearable.DataApi
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.nomi.artwatch.Application
import com.nomi.artwatch.R
import com.nomi.artwatch.di.component.ActivityComponent
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by Ryota Niinomi on 2016/08/16.
 */
class SettingActivity : DrawerActivity() {

    companion object {
        private val PATH_OF_TIMEOUT = "/timeout"
        private val KEY_TIMEOUT = "timeout"
        private val PATH_WITH_FEATURE_GIF_TIMEOUT = "/gif/timeout"

        fun createIntent(context: Context): Intent {
            val intent = Intent(context, SettingActivity::class.java)
            return intent
        }
    }

    val mRadioGroup: RadioGroup by bindView(R.id.radioGroup)

    private var mCurrentItem: Item = Item.ITEM_1
    private var mTimeout: Long = 0

    private val mGoogleConnectionCallback = object : GoogleApiClient.ConnectionCallbacks {
        override fun onConnected(connectionHint: Bundle?) {
            if (Application.sPeerId != null) {
                val builder = Uri.Builder()
                val featureTimeoutUri = builder.scheme(SCHEME).path(PATH_WITH_FEATURE_GIF_TIMEOUT).authority(Application.sPeerId).build()
                Wearable.DataApi.getDataItem(mGoogleApiClient, featureTimeoutUri).setResultCallback(mTimeoutResultCallback)
            }
        }

        override fun onConnectionSuspended(cause: Int) {
            // TODO：Handling
        }
    }

    override fun getGoogleConnectionCallback() : GoogleApiClient.ConnectionCallbacks {
        return mGoogleConnectionCallback
    }

    enum class Item(val id: Int, val time: Long, val labelRes: Int) {
        ITEM_1(R.id.radioItem1, TimeUnit.SECONDS.toMillis(30), R.string.settings_timeout_30_sec),
        ITEM_2(R.id.radioItem2, TimeUnit.SECONDS.toMillis(60), R.string.settings_timeout_60_sec),
        ITEM_3(R.id.radioItem3, TimeUnit.SECONDS.toMillis(90), R.string.settings_timeout_90_sec),
        ITEM_4(R.id.radioItem4, TimeUnit.SECONDS.toMillis(120), R.string.settings_timeout_120_sec),
        ITEM_5(R.id.radioItem5, 0, R.string.settings_timeout_infinite)
    }

    override fun injectDependency(component: ActivityComponent?) {
        component?.inject(this)
    }

    override val layout: Int get() = R.layout.activity_setting
    override val toolbarName: Int get() = R.string.settings
    override val shouldShowSpinner: Boolean get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        for (item in Item.values()) {
            val radioButton = findViewById(item.id) as RadioButton
            radioButton.text = getString(item.labelRes)
        }

        mRadioGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            if (checkedId != -1) {
                val item: Item = getItem(checkedId)
                selectRadioItem(item)

            } else {
                Timber.d("not found")
            }
        }
    }

    private val mTimeoutResultCallback = object : ResultCallback<DataApi.DataItemResult> {
        override fun onResult(dataItemResult: DataApi.DataItemResult) {
            if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {
                val configDataItem = dataItemResult.getDataItem()
                val dataMapItem = DataMapItem.fromDataItem(configDataItem)
                val config = dataMapItem.dataMap
                mTimeout = config.getLong(KEY_TIMEOUT)
                mCurrentItem = getItemByTime(mTimeout)

                mRadioGroup.check(mCurrentItem.id)

                Timber.d("TimeoutResultCallback, mTimeout : %d", mTimeout)

            } else {
                Timber.d("TimeoutResultCallback : error")
            }
        }
    }

    private fun selectRadioItem(item: Item) {
        val dataMap = PutDataMapRequest.create(PATH_OF_TIMEOUT)
        dataMap.dataMap.putLong(KEY_TIMEOUT, item.time)
        Wearable.DataApi.putDataItem(mGoogleApiClient, dataMap.asPutDataRequest())
    }

    private fun getItem(id: Int): Item {
        for (item in Item.values()) {
            if (item.id == id) {
                return item
            }
        }
        return Item.ITEM_1
    }

    private fun getItemByTime(time: Long): Item {
        for (item in Item.values()) {
            if (item.time == time) {
                return item
            }
        }
        return Item.ITEM_1
    }
}