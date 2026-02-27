package com.example.emedibotsimpleuserlogin

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * A Foreground Service that plays the alarm ringtone.
 *
 * Running as a foreground service is CRITICAL on Android 8+ because:
 * - Background services are killed by Doze mode / battery optimization
 * - Foreground services show a persistent notification and cannot be killed
 *   by the OS while the alarm is active.
 */
class RingtonePlayingService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    companion object {
        private const val ALARM_CHANNEL_ID = "alarm_ringing_channel"
        private const val FOREGROUND_NOTIFICATION_ID = 9999
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Step 1: Promote to foreground immediately so Android cannot kill us
        createAlarmChannel()
        startForeground(FOREGROUND_NOTIFICATION_ID, buildForegroundNotification())

        // Step 2: Stop any previously playing media (prevents double-play)
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        // Step 3: Get the default alarm sound; fall back to notification sound
        val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: return START_STICKY

        // Step 4: Create MediaPlayer with proper audio attributes for alarms
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(applicationContext, alarmUri)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }

        // START_STICKY: if OS kills the service, restart it (important for alarm reliability)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        // Remove the foreground notification when alarm is dismissed
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    /**
     * Creates the notification channel for the foreground alarm notification.
     * Must be created before calling startForeground() on Android 8+.
     */
    private fun createAlarmChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(ALARM_CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    ALARM_CHANNEL_ID,
                    "Alarm Ringing",
                    NotificationManager.IMPORTANCE_LOW  // LOW so it doesn't make its own sound
                ).apply {
                    description = "Shows while the medicine alarm is ringing"
                    setSound(null, null)  // No extra sound — MediaPlayer handles it
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    /**
     * Builds the minimal persistent notification required to keep this service
     * in the foreground. The actual alarm sound is played by MediaPlayer.
     */
    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏰ Medicine Alarm Ringing")
            .setContentText("Tap the notification to open the app and dismiss.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
}