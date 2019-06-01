package com.zjw.ting.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.blankj.rxbus.RxBus
import com.zjw.ting.bean.Event

/**
 * In music player, playback of songs has to be done within a service which runs in background
 * even after the application is closed. We will create such service to handle the inputs given
 * through the buttons shown by the Notification layout
 */
class NotificationService : Service() {

    private var notificationGenerator: NotificationGenerator = NotificationGenerator()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        stopForeground(true)
        super.onDestroy()
    }

    /**
     * Receive the big content actions in background.
     * @param [intent] The Intent supplied to startService, as given.
     *        This may be null if the service is being restarted after its process has gone away,
     *        and it had previously returned anything except START_STICKY_COMPATIBILITY.
     * @param [flags] Additional data about this start request.
     * @param [startId] A unique integer representing this specific request to start.     *
     * @return The return value indicates what semantics the system should use for the service's
     *         current started state.  It may be one of the constants associated with the
     *         START_CONTINUATION_MASK bits.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.apply {
            Log.d("onStartCommand", "action=$action")
            when (action) {
                START_SERVICE -> {
                    val showNotification = notificationGenerator.showNotification(applicationContext)
                    startForeground(100, showNotification)
                }
                NOTIFY_PREVIOUS -> {
                    RxBus.getDefault().post(Event.ServiceEvent(action))
                }
                NOTIFY_PLAY -> {
                    RxBus.getDefault().post(Event.ServiceEvent(action))
                }
                NOTIFY_NEXT -> {
                    RxBus.getDefault().post(Event.ServiceEvent(action))
                }
                NOTIFY_STOP -> {
                    RxBus.getDefault().post(Event.ServiceEvent(action))
                    stopForeground(true)
                    stopSelf()
                    // Terminate the notification
                    val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(NOTIFICATION_ID)
                }
            }
        }

        return START_STICKY
    }

}