package com.nomi.artwatch.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import butterknife.bindView
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
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

        fun createIntent(context: Context): Intent {
            val intent = Intent(context, SettingActivity::class.java)
            return intent
        }
    }

    val mRadioGroup: RadioGroup by bindView(R.id.radioGroup)

    enum class Item(val id: Int, val time: Long, val label: String) {
        ITEM_1(R.id.radioItem1, TimeUnit.SECONDS.toMillis(30), "30 sec"),
        ITEM_2(R.id.radioItem2, TimeUnit.SECONDS.toMillis(60), "60 sec"),
        ITEM_3(R.id.radioItem3, 0, "Infinite")
    }

    override fun injectDependency(component: ActivityComponent?) {
        component?.inject(this)
    }

    override val layout: Int get() = R.layout.activity_setting

    override val toolbarName: Int get() = R.string.settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        for (item in Item.values()) {
            val radioButton = findViewById(item.id) as RadioButton
            radioButton.text = item.label
        }

        mRadioGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            if (checkedId != -1) {
                val item: Item = getItem(checkedId)
                selectRadioItem(item)

            } else {
                Timber.d("not found")
            }
        }

        mRadioGroup.check(Item.ITEM_1.id)
    }

    private fun selectRadioItem(item: Item) {
        val dataMap = PutDataMapRequest.create(PATH_OF_TIMEOUT)
        dataMap.dataMap.putLong(KEY_TIMEOUT, item.time)
        Wearable.DataApi.putDataItem(mGoogleApiClient, dataMap.asPutDataRequest())
    }

    private fun getItem(id: Int):Item {
        for (item in Item.values()) {
            if (item.id == id) {
                return item
            }
        }
        return Item.ITEM_1
    }
}