package com.nomi.artwatch.ui.util

import android.bluetooth.BluetoothAdapter

/**
 * Created by Ryota Niinomi on 2016/10/01.
 */
object BluetoothUtil {
    fun isEnabled(): Boolean {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled
    }
}