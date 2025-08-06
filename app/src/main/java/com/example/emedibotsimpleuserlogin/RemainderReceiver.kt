//this file is for the reminder screen and notification
//handles the notifications
package com.example.emedibotsimpleuserlogin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicineName = intent.getStringExtra("medicine_name") ?: "your medicine"

        val builder = NotificationCompat.Builder(context, "med_reminder_channel")
            .setSmallIcon(R.drawable.emedibot) // your icon
            .setContentTitle("Medicine Reminder")
            .setContentText("It's time to take $medicineName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val manager = NotificationManagerCompat.from(context)
        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
