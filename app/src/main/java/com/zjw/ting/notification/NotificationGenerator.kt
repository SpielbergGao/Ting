package com.zjw.ting.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.zjw.ting.R
import com.zjw.ting.activity.AudioPlayActivity




const val NOTIFY_PLAY = "NOTIFY_PLAY"
const val NOTIFY_PAUSE = "NOTIFY_PAUSE"
const val NOTIFY_STOP = "NOTIFY_STOP"
const val NOTIFY_NEXT = "NOTIFY_NEXT"
const val NOTIFY_PREVIOUS = "NOTIFY_PREVIOUS"
const val START_SERVICE = "START_SERVICE"

const val NOTIFICATION_ID = 99

class NotificationGenerator(var notificationIntentClass: Class<*> = AudioPlayActivity::class.java) {

    private lateinit var bigView: RemoteViews
    private lateinit var smallView: RemoteViews
    private var notificationManager: NotificationManager? = null
    private var notificationChannel: NotificationChannel? = null

    private val CHANNEL_ID = "com.murgupluoglu.notificationdemo"
    private val CHANNEL_NAME = "Test notification"

    private val NOTIFICATION_TITLE = "Music Player"
    private val NOTIFICATION_TEXT = "Control Audio"
    private val NOTIFICATION_TICKER = "Music Player Playing Now."
    private val NOTIFICATION_STATUSBAR_PLAY_ICON = R.drawable.ic_stat_big_content

    fun showNotification(
        context: Context,
        songTitle: String = "Ting",
        artistName: String = "Artist Name",
        albumName: String = "Album Name"
    ): Notification {
        // Using RemoteViews to bind custom layouts into Notification
        smallView = RemoteViews(context.packageName, R.layout.status_bar)
        bigView = RemoteViews(context.packageName, R.layout.status_bar_expanded)

        // showing default album image
        smallView.setViewVisibility(R.id.status_bar_icon, View.VISIBLE)
        smallView.setViewVisibility(R.id.status_bar_album_art, View.GONE)
        bigView.setImageViewBitmap(
            R.id.status_bar_album_art,
            BitmapFactory.decodeResource(context.resources, R.drawable.default_album_picture)
        )
        setListeners(bigView, smallView, context, songTitle, artistName, albumName)

        // Build the content of the notification
        val nBuilder = getNotificationBuilder(
            context,
            NOTIFICATION_TITLE,
            NOTIFICATION_TEXT,
            NOTIFICATION_STATUSBAR_PLAY_ICON,
            NOTIFICATION_TICKER
        )

        // Notification through notification manager
        lateinit var notification: Notification

        /* Add Big View Specific Configuration */
        nBuilder.setContent(bigView)
        notification = nBuilder.build()
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //nBuilder.setCustomBigContentView(bigView)
            //NotificationManager.IMPORTANCE_LOW
            //nBuilder.setCustomContentView(smallView)
            notification = nBuilder.build()
        } else {
            notification = nBuilder.build()
            notification.contentView = smallView
            notification.bigContentView = bigView
        }
*/
        // Notification through notification manager
        notification.flags = Notification.FLAG_ONLY_ALERT_ONCE
        notificationManager?.notify(NOTIFICATION_ID, notification)
        //因为会一次弹出2个
        notificationManager?.cancel(NOTIFICATION_ID)
        return notification
    }


    private fun setListeners(
        bigView: RemoteViews,
        smallView: RemoteViews,
        context: Context,
        songTitle: String,
        artistName: String,
        albumName: String
    ) {

        bigView.setOnClickPendingIntent(R.id.status_bar_prev, createPendingIntent(context, NOTIFY_PREVIOUS))
        smallView.setOnClickPendingIntent(R.id.status_bar_prev, createPendingIntent(context, NOTIFY_PREVIOUS))

        bigView.setOnClickPendingIntent(R.id.status_bar_collapse, createPendingIntent(context, NOTIFY_STOP))
        smallView.setOnClickPendingIntent(R.id.status_bar_collapse, createPendingIntent(context, NOTIFY_STOP))

        bigView.setOnClickPendingIntent(R.id.status_bar_next, createPendingIntent(context, NOTIFY_NEXT))
        smallView.setOnClickPendingIntent(R.id.status_bar_next, createPendingIntent(context, NOTIFY_NEXT))

        bigView.setOnClickPendingIntent(R.id.status_bar_play, createPendingIntent(context, NOTIFY_PLAY))
        smallView.setOnClickPendingIntent(R.id.status_bar_play, createPendingIntent(context, NOTIFY_PLAY))

        bigView.setTextViewText(R.id.status_bar_track_name, songTitle)
        smallView.setTextViewText(R.id.status_bar_track_name, songTitle)

        bigView.setTextViewText(R.id.status_bar_artist_name, artistName)
        smallView.setTextViewText(R.id.status_bar_artist_name, artistName)

        bigView.setTextViewText(R.id.status_bar_album_name, albumName)
    }

    fun setTitleText(title :String){
        bigView.setTextViewText(R.id.status_bar_track_name, title)
    }

    private fun createPendingIntent(context: Context, action: String): PendingIntent {
        val intentPlay = Intent(context, NotificationService::class.java)
        intentPlay.action = action
        return PendingIntent.getService(context, 0, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /**
     * Initialize the notification manager and channel Id.
     * The notification builder has the basic initialization:
     *     - AutoCancel=true
     *     - LargeIcon = SmallIcon
     * @param [context] application context for associate the notification with.
     * @param [notificationTitle] notification title.
     * @param [notificationText] notification text.
     * @param [notificationIconId] notification icon id from application resource.
     * @param [notificationTicker] notification ticker text for accessibility.
     * @return the PendingIntent to be used on this notification.
     */
    private fun getNotificationBuilder(
        context: Context,
        notificationTitle: String,
        notificationText: String,
        notificationIconId: Int,
        notificationTicker: String
    ): NotificationCompat.Builder {
        // Define the notification channel for newest Android versions
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val pendingIntent = getPendingIntent(context)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (null == notificationChannel) {
                //notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
                //IMPORTANCE_HIGH 悬浮提示
                notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
                notificationChannel?.apply {
                    enableLights(true)
                    lightColor = Color.GREEN
                    enableVibration(false)
                    notificationManager?.createNotificationChannel(this)
                }

            }
        }
        // Build the content of the notification
        builder.setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setSmallIcon(notificationIconId)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, notificationIconId))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(pendingIntent)
            .setTicker(notificationTicker)

        // Restricts the notification information when the screen is blocked.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setVisibility(Notification.VISIBILITY_PRIVATE)
        }

        return builder
    }

    private fun getPendingIntent(context: Context): PendingIntent {
        val resultIntent = Intent(context, notificationIntentClass)
        resultIntent.action = Intent.ACTION_MAIN
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        return PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}