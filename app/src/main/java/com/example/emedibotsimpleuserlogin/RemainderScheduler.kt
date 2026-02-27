package com.example.emedibotsimpleuserlogin

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import java.util.Calendar

/**
 * Schedules a daily exact alarm for the given medicine at [hour]:[minute].
 * Uses setExactAndAllowWhileIdle() so it fires even during Doze mode.
 *
 * On Android 12+ it first checks canScheduleExactAlarms() and opens the
 * system settings if the permission has not been granted yet.
 */
fun scheduleDailyAlarm(context: Context, hour: Int, minute: Int, medicineName: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // On Android 12+ (S) we need the SCHEDULE_EXACT_ALARM permission at runtime.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!alarmManager.canScheduleExactAlarms()) {
            // Open system settings so the user can grant exact alarm permission
            val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(settingsIntent)
            return
        }
    }

    // FIXED: Use the same constant key that ReminderReceiver reads ("MEDICINE_NAME")
    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra(ReminderReceiver.MEDICINE_NAME, medicineName)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        medicineName.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Build the next occurrence of [hour]:[minute]
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        // If the time has already passed today, schedule for tomorrow
        if (before(Calendar.getInstance())) {
            add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    // setExactAndAllowWhileIdle fires even when the device is in Doze mode
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
    )
}
