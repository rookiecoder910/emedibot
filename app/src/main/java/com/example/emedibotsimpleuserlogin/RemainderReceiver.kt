package com.example.emedibotsimpleuserlogin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlin.jvm.java
private fun playAlarmSound(context: Context) {
    try {
        val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone: Ringtone = RingtoneManager.getRingtone(context, alarmUri)
        ringtone.play()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicineName = intent.getStringExtra("MEDICINE_NAME") ?: "your medicine"
        showNotification(context, medicineName)
        playAlarmSound(context)
    }

    private fun showNotification(context: Context, medicineName: String) {
        val channelId = "medicine_reminder"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Medicine Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to take medicine"
            }
            notificationManager.createNotificationChannel(channel)
        }
        val cancelIntent = Intent(context, AlarmCancelReceiver::class.java).apply {
            putExtra("MEDICINE_NAME", medicineName)
        }

        val cancelPendingIntent = PendingIntent.getBroadcast(
            context,
            medicineName.hashCode(),
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏰ Time to take your medicine!")
            .setContentText("💊 Take $medicineName now.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_cancel,
                "Cancel",
                cancelPendingIntent
            )

        notificationManager.notify(medicineName.hashCode(), notification.build())


    }
}
