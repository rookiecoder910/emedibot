
package com.example.emedibotsimpleuserlogin

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun convertTimeTo24HrFormat(timeString: String): Pair<Int, Int> {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val date = sdf.parse(timeString)
    val cal = Calendar.getInstance().apply { time = date }
    return Pair(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
}
