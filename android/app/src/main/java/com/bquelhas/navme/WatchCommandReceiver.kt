package com.bquelhas.navme

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.getpebble.android.kit.PebbleKit
import com.getpebble.android.kit.util.PebbleDictionary

/**
 * Receives messages sent *from* the NavMe watchapp. Currently the only inbound command is
 * [NavKeys.NAV_TRIGGER_ROUTE]: when nav is idle the watch SELECT button picks a favorite and
 * sends back its index, and the phone starts navigation to that destination.
 *
 * Registered dynamically (see [NavNotificationListenerService]); ack every transaction so the
 * watch's AppMessage outbox drains.
 */
class WatchCommandReceiver : PebbleKit.PebbleDataReceiver(NavKeys.WATCH_UUID) {

    override fun receiveData(context: Context, transactionId: Int, data: PebbleDictionary) {
        // Always ack first so the watch isn't left waiting.
        PebbleKit.sendAckToPebble(context, transactionId)

        val idx = data.getUnsignedIntegerAsLong(NavKeys.NAV_TRIGGER_ROUTE)?.toInt() ?: return
        // Debounce: the watch may resend on a flaky link; ignore repeats of the same pick.
        val now = SystemClock.elapsedRealtime()
        if (idx == lastIndex && now - lastAt < DEBOUNCE_MS) return
        lastIndex = idx; lastAt = now

        val favs = FavoritesStore.all(context)
        val fav = favs.getOrNull(idx)
        if (fav == null) {
            Log.w(TAG, "trigger for unknown favorite index $idx (have ${favs.size})")
            return
        }
        Log.i(TAG, "watch triggered favorite #$idx '${fav.label}' -> ${fav.query}")
        launchFavorite(context, fav)
    }

    private fun launchFavorite(context: Context, fav: Favorite) {
        // Prefer a concrete navigator intent; fall back to a generic geo: intent.
        val intents = NavLauncher.intentsFor(context, fav.query)
        val intent = intents.firstOrNull()?.second ?: NavLauncher.genericGeoIntent(fav.query)
        // Started from a background context (the watch press); NEW_TASK is required and some
        // OEMs may still gate background activity starts — acceptable for a companion trigger.
        NavLauncher.launch(context, intent)
    }

    companion object {
        private const val TAG = "NavMe/WatchCmd"
        private const val DEBOUNCE_MS = 3000L
        private var lastIndex = -1
        private var lastAt = 0L
    }
}
