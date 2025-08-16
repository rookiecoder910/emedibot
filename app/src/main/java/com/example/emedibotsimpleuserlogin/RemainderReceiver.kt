package com.example.emedibotsimpleuserlogin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat


class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val MEDICINE_NAME = "MEDICINE_NAME"
        private const val CHANNEL_ID = "medicine_reminder_channel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medicineName = intent.getStringExtra(MEDICINE_NAME) ?: "your medicine"


        showNotification(context, medicineName)


        val serviceIntent = Intent(context, RingtonePlayingService::class.java)
        context.startService(serviceIntent)
    }

    private fun showNotification(context: Context, medicineName: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Medicine Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority reminders to take medicine"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }


        val cancelIntent = Intent(context, AlarmCancelReceiver::class.java).apply {
            putExtra(AlarmCancelReceiver.MEDICINE_NAME, medicineName)
        }


        val cancelPendingIntent = PendingIntent.getBroadcast(
            context,
            medicineName.hashCode(),
            cancelIntent,

            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Make sure you have this icon
            .setContentTitle("⏰ Time for your medicine!")
            .setContentText("Please take your dose of $medicineName now.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setOngoing(true)
            .addAction(
                R.drawable.ic_cancel, // Make sure you have this icon
                "Turn Off",
                cancelPendingIntent
            )
            .build()

        notificationManager.notify(medicineName.hashCode(), notification)
    }
}