package com.bquelhas.navme

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

/**
 * Listens to ongoing navigation notifications from supported map apps, parses them,
 * and relays a NaviData to the watch. Requires the user to grant notification
 * access (Settings → Notifications → Notification access).
 */
class NavNotificationListenerService : NotificationListenerService() {
    private val TAG = "NavMe/Listener"
    private var lastSent: String? = null

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        if (pkg !in NaviParser.SUPPORTED) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

        Log.d(TAG, "notif from $pkg title='$title' text='$text'")

        val data = NaviParser.parse(pkg, title, text) ?: return

        // De-dup identical consecutive instructions (nav notifications update often).
        val sig = "${data.directionId}|${data.instructionText}"
        if (sig == lastSent) return
        lastSent = sig

        PebbleEmitter.sendNav(applicationContext, data)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (sbn.packageName in NaviParser.SUPPORTED) {
            lastSent = null
            PebbleEmitter.sendCancel(applicationContext)
        }
    }
}
