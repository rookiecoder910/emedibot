package com.example.emedibotsimpleuserlogin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat

/**
 * ReminderReceiver fires when AlarmManager triggers a medicine reminder.
 *
 * Instead of starting a separate foreground service (which can crash on Android 12+
 * due to background restrictions), this receiver now plays the alarm sound directly
 * using a static MediaPlayer. This is simpler and avoids the
 * ForegroundServiceStartNotAllowedException crash entirely.
 */
class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val MEDICINE_NAME = "MEDICINE_NAME"
        private const val CHANNEL_ID = "medicine_reminder_channel"
        private const val TAG = "ReminderReceiver"

        // Static MediaPlayer so it survives past onReceive() and can be stopped later
        private var mediaPlayer: MediaPlayer? = null

        /**
         * Call this to stop the currently playing alarm ringtone.
         */
        fun stopAlarmSound() {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping alarm: ${e.message}")
            } finally {
                mediaPlayer = null
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medicineName = intent.getStringExtra(MEDICINE_NAME) ?: "your medicine"

        // Acquire a partial WakeLock to keep CPU alive during execution.
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "eMediBot:AlarmWakeLock"
        )
        wakeLock.acquire(30_000L) // Hold for max 30 seconds

        try {
            // Show the high-priority notification with a "Turn Off" action
            showNotification(context, medicineName)

            // Play alarm sound directly (avoids ForegroundService crash on Android 12+)
            playAlarmSound(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error in onReceive: ${e.message}", e)
        } finally {
            if (wakeLock.isHeld) wakeLock.release()
        }
    }

    /**
     * Plays the default alarm ringtone using a static MediaPlayer.
     * The MediaPlayer is kept alive as a static reference so the sound
     * continues after onReceive() returns.
     */
    private fun playAlarmSound(context: Context) {
        try {
            // Stop any previously playing alarm
            stopAlarmSound()

            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: return

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, alarmUri)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing alarm: ${e.message}", e)
            // Fallback: try using MediaPlayer.create()
            try {
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: return
                mediaPlayer = MediaPlayer.create(context, alarmUri)?.apply {
                    isLooping = true
                    start()
                }
            } catch (e2: Exception) {
                Log.e(TAG, "Fallback alarm also failed: ${e2.message}", e2)
            }
        }
    }

    private fun showNotification(context: Context, medicineName: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create or update the notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Medicine Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority reminders to take medicine"
                enableVibration(true)
                setBypassDnd(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // PendingIntent to dismiss alarm when user taps "Turn Off"
        val cancelIntent = Intent(context, AlarmCancelReceiver::class.java).apply {
            putExtra(AlarmCancelReceiver.MEDICINE_NAME, medicineName)
        }
        val cancelPendingIntent = PendingIntent.getBroadcast(
            context,
            medicineName.hashCode(),
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // PendingIntent to open the app when user taps the notification
        val openAppIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
        val openAppPendingIntent = if (openAppIntent != null) {
            PendingIntent.getActivity(
                context,
                medicineName.hashCode() + 1,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else null

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏰ Time for your medicine!")
            .setContentText("Please take your dose of $medicineName now.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Turn Off",
                cancelPendingIntent
            )
            .build()

        notificationManager.notify(medicineName.hashCode(), notification)
    }
}