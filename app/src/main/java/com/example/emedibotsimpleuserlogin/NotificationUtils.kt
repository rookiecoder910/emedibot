package com.example.emedibotsimpleuserlogin

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

fun showNotification(context: Context, medicineName: String, time: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val builder = NotificationCompat.Builder(context, "med_reminder_channel")
        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
        .setContentTitle("")

        .setContentText(" Hello pal don't forget to take $medicineName $time",)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)

    notificationManager.notify(1, builder.build())
}
