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

        // 1. Stop the currently playing ringtone (uses static MediaPlayer in ReminderReceiver)
        ReminderReceiver.stopAlarmSound()

        // 2. Also stop the old foreground service if it was running (backwards compat)
        try {
            val stopSoundIntent = Intent(context, RingtonePlayingService::class.java)
            context.stopService(stopSoundIntent)
        } catch (_: Exception) { }

        // 3. Dismiss the alarm notification
        val notificationId = medicineName.hashCode()
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(notificationId)

        // 4. Cancel any future repeating alarms
        cancelFutureAlarm(context, medicineName)

        Toast.makeText(context, "Alarm for $medicineName turned off", Toast.LENGTH_SHORT).show()
    }

    private fun cancelFutureAlarm(context: Context, medicineName: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCode = medicineName.hashCode()

        val alarmIntent = Intent(context, ReminderReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            alarmIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}