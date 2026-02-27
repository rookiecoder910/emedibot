package com.example.emedibotsimpleuserlogin

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

/**
 * BootReceiver listens for BOOT_COMPLETED / LOCKED_BOOT_COMPLETED system broadcasts.
 *
 * WHY THIS IS NEEDED:
 * AlarmManager alarms do NOT survive a phone reboot. Without this receiver,
 * every time the user restarts their phone, ALL medicine alarms are silently
 * lost and never ring again until the user manually re-opens the app.
 *
 * This receiver re-reads the user's medicines from Firebase and re-schedules
 * every alarm automatically after each reboot.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.LOCKED_BOOT_COMPLETED"
        ) return

        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val uid = currentUser.uid

        // Read all medicines for this user from Firebase and re-schedule their alarms
        val dbRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(uid)
            .child("medicines")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val medicineName = child.key?.replace("_", " ") ?: continue
                    val timeString = child.getValue(String::class.java) ?: continue

                    // Parse the time string (expected format: "HH:mm" or "h:mm AM/PM")
                    val (hour, minute) = parseTimeString(timeString) ?: continue

                    // Inline alarm scheduling (same logic as scheduleDailyAlarm)
                    scheduleAlarmAfterBoot(context, hour, minute, medicineName)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Cannot do much in a BroadcastReceiver context
            }
        })
    }

    /**
     * Schedules an exact daily alarm for a medicine. Inlined here to avoid
     * cross-file top-level function reference issues at boot time.
     */
    private fun scheduleAlarmAfterBoot(
        context: Context,
        hour: Int,
        minute: Int,
        medicineName: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // On Android 12+ check exact alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) return
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.MEDICINE_NAME, medicineName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicineName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    /**
     * Parses a time string into a Pair<hour (24h), minute>.
     * Handles both "HH:mm" (24-hour) and "h:mm AM/PM" (12-hour) formats.
     * Returns null if the format is unrecognised.
     */
    private fun parseTimeString(timeString: String): Pair<Int, Int>? {
        return try {
            val trimmed = timeString.trim().uppercase()

            if (trimmed.contains("AM") || trimmed.contains("PM")) {
                // 12-hour format: "9:30 AM" or "10:00 PM"
                val isPm = trimmed.contains("PM")
                val timePart = trimmed.replace("AM", "").replace("PM", "").trim()
                val parts = timePart.split(":")
                var hour = parts[0].trim().toInt()
                val minute = parts[1].trim().toInt()
                if (isPm && hour != 12) hour += 12
                if (!isPm && hour == 12) hour = 0
                Pair(hour, minute)
            } else {
                // 24-hour format: "14:30"
                val parts = timeString.trim().split(":")
                Pair(parts[0].toInt(), parts[1].toInt())
            }
        } catch (e: Exception) {
            null
        }
    }
}
