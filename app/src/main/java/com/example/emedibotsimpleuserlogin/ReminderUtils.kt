
package com.example.emedibotsimpleuserlogin

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import java.util.Calendar


fun scheduleMedicineReminder(context: Context, medicine: Medicine) {
    val timeParts = medicine.time.split(" ", ":")
    if (timeParts.size < 3) return

    var hour = timeParts[0].toInt()
    val minute = timeParts[1].toInt()
    val amPm = timeParts[2]

    if (amPm.equals("PM", ignoreCase = true) && hour != 12) hour += 12
    if (amPm.equals("AM", ignoreCase = true) && hour == 12) hour = 0

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (before(Calendar.getInstance())) add(Calendar.DATE, 1)
    }

    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("medicine_name", medicine.name)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        medicine.name.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
    )
}
