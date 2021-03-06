package com.nomi.artwatch.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import butterknife.bindView
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.wearable.DataApi
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.nomi.artwatch.R
import com.nomi.artwatch.di.component.ActivityComponent
import com.nomi.artwatch.ui.util.BluetoothUtil
import com.nomi.artwatch.ui.util.SnackbarUtil
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by Ryota Niinomi on 2016/08/16.
 */
class SettingActivity : DrawerActivity() {

    companion object {
        private val PATH_OF_TIMEOUT = "/timeout"
        private val KEY_TIMEOUT = "timeout"
        private val DEFAULT_TIMEOUT = TimeUnit.SECONDS.toMillis(30)

        fun createIntent(context: Context): Intent = Intent(context, SettingActivity::class.java)
    }

    private val mRadioGroup: RadioGroup by bindView(R.id.radioGroup)
    private val mOssLicenseBtn: TextView by bindView(R.id.ossLicenseBtn)

    private var mCurrentItem: Item = Item.ITEM_1
    private var mTime: Long = 0

    enum class Item(val id: Int, val time: Long, val labelRes: Int) {
        ITEM_1(R.id.radioItem1, TimeUnit.SECONDS.toMillis(30), R.string.settings_timeout_30_sec),
        ITEM_2(R.id.radioItem2, TimeUnit.SECONDS.toMillis(60), R.string.settings_timeout_60_sec),
        ITEM_3(R.id.radioItem3, TimeUnit.SECONDS.toMillis(90), R.string.settings_timeout_90_sec),
        ITEM_4(R.id.radioItem4, TimeUnit.SECONDS.toMillis(120), R.string.settings_timeout_120_sec),
        ITEM_5(R.id.radioItem5, 0, R.string.settings_timeout_infinite)
    }

    override fun onConnected(connectionHint: Bundle?) {
        super.onConnected(connectionHint)

        // Get Node Id first.
        Wearable.NodeApi.getLocalNode(mGoogleApiClient).setResultCallback { getLocalNodeResult ->
            val localNode = getLocalNodeResult.node.id
            val uri = Uri.Builder().scheme(SCHEME).path(PATH_OF_TIMEOUT).authority(localNode).build()

            // Get data from wear.
            Wearable.DataApi.getDataItem(mGoogleApiClient, uri).setResultCallback(object : ResultCallback<DataApi.DataItemResult> {
                override fun onResult(dataItemResult: DataApi.DataItemResult) {
                    Log.d(this.javaClass.canonicalName, "fetchConfigDataMap onResult.")

                    if (!dataItemResult.status.isSuccess) return

                    if (dataItemResult.dataItem != null) {
                        val configDataItem = dataItemResult.dataItem
                        val dataMapItem = DataMapItem.fromDataItem(configDataItem)
                        val dataMap = dataMapItem.dataMap
                        // Set initial time.
                        setTime(dataMap.getLong(KEY_TIMEOUT))
                    } else {
                        Log.d(this.javaClass.canonicalName, "No data was found. Set default time.")
                        setTime(DEFAULT_TIMEOUT)
                    }
                }
            })
        }
    }

    override fun injectDependency(component: ActivityComponent) {
        component.inject(this)
    }

    override val layout: Int = R.layout.activity_setting
    override val toolbarName: Int = R.string.settings
    override val shouldShowSpinner: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        for (item in Item.values()) {
            (findViewById(item.id) as RadioButton).run {
                text = getString(item.labelRes)
            }
        }

        mRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) {
                val item: Item = getItem(checkedId)
                selectRadioItem(item)

            } else {
                Timber.d("not found")
            }
        }

        mOssLicenseBtn.setOnClickListener {
            startActivity(OssActivity.createIntent(this))
        }
    }

    private fun setTime(time: Long) {
        mTime = time
        mCurrentItem = getItemByTime(mTime)

        mRadioGroup.check(mCurrentItem.id)
    }

    private fun selectRadioItem(item: Item) {
        if (!mGoogleApiClient.isConnected) {
            SnackbarUtil.showAlert(this, getString(R.string.error_no_connection_to_wear))
            return
        }
        if (!BluetoothUtil.isEnabled()) {
            SnackbarUtil.showAlert(this, getString(R.string.error_no_connection_to_bluetooth))
            return
        }

        PutDataMapRequest.create(PATH_OF_TIMEOUT).let { dataMap ->
            dataMap.dataMap.putLong(KEY_TIMEOUT, item.time)
            Wearable.DataApi.putDataItem(mGoogleApiClient, dataMap.asPutDataRequest())
        }
    }

    private fun getItem(id: Int): Item {
        Item.values().forEach { item ->
            if (item.id == id) {
                return item
            }
        }
        return Item.ITEM_1
    }

    private fun getItemByTime(time: Long): Item {
        Item.values().forEach { item ->
            if (item.time == time) {
                return item
            }
        }
        return Item.ITEM_1
    }
}