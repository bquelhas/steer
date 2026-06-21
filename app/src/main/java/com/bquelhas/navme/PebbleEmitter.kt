package com.bquelhas.navme

import android.content.Context
import android.util.Log
import com.getpebble.android.kit.PebbleKit
import com.getpebble.android.kit.util.PebbleDictionary

/** Sends a [NaviData] to the NavMe watchapp via the Pebble/Core Devices bridge. */
object PebbleEmitter {
    private const val TAG = "NavMe/Emitter"

    fun isWatchConnected(context: Context): Boolean =
        try { PebbleKit.isWatchConnected(context) } catch (e: Exception) { false }

    fun sendNav(context: Context, data: NaviData) {
        val dict = PebbleDictionary().apply {
            addInt32(NavKeys.NAV_TURN, data.directionId)
            addUint8(NavKeys.NAV_TEXT_BEGIN, 1)
            addString(NavKeys.NAV_TEXT, data.instructionText.take(120))
            addUint8(NavKeys.NAV_TEXT_END, 1)
            data.gpsAccuracy?.let { addString(NavKeys.NAV_GPS_ACCURACY, it) }
        }
        try {
            PebbleKit.sendDataToPebble(context, NavKeys.WATCH_UUID, dict)
            Log.i(TAG, "sent turn=${data.direction} text='${data.instructionText}'")
        } catch (e: Exception) {
            Log.e(TAG, "send failed: ${e.message}")
        }
    }

    fun sendCancel(context: Context) {
        val dict = PebbleDictionary().apply { addUint8(NavKeys.NAV_CANCEL, 1) }
        try { PebbleKit.sendDataToPebble(context, NavKeys.WATCH_UUID, dict) } catch (_: Exception) {}
    }
}
