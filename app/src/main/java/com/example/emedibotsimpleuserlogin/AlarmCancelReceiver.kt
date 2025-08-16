package com.example.emedibotsimpleuserlogin

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat

class AlarmCancelReceiver : BroadcastReceiver() {

    companion object {
        const val MEDICINE_NAME = "MEDICINE_NAME"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medicineName = intent.getStringExtra(MEDICINE_NAME)
        if (medicineName.isNullOrEmpty()) {
            return
        }

        // 1. Stop the currently playing ringtone
        val stopSoundIntent = Intent(context, RingtonePlayingService::class.java)
        context.stopService(stopSoundIntent)

        // 2. Dismiss the alarm notification
        val notificationId = medicineName.hashCode()
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(notificationId)

        // 3. Cancel any future repeating alarms
        cancelFutureAlarm(context, medicineName)

        Toast.makeText(context, "Alarm for $medicineName turned off", Toast.LENGTH_SHORT).show()
    }

    /**
     * Cancels a repeating alarm scheduled with AlarmManager.
     */
    private fun cancelFutureAlarm(context: Context, medicineName: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCode = medicineName.hashCode()

        // --- THIS IS THE KEY CHANGE ---
        // The intent must now point to ReminderReceiver, because that is the
        // receiver that is being used to SET the alarm.
        val alarmIntent = Intent(context, ReminderReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            alarmIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        // If the PendingIntent exists, cancel it.
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}