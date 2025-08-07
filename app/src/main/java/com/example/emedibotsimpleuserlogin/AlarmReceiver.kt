package com.example.emedibotsimpleuserlogin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val medicineName = intent?.getStringExtra("medicine_name") ?: "Medicine"
        val time = intent?.getStringExtra("medicine_time") ?: ""

        showNotification(
            context!!,
            "Medicine Reminder",
            "$medicineName at $time"
        )
    }
}
