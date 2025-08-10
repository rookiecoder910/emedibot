package com.example.emedibotsimpleuserlogin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.widget.Toast





class AlarmCancelReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medicineName = intent.getStringExtra("MEDICINE_NAME") ?: return





        cancelAlarm(context, medicineName)


        Toast.makeText(context, "Turned off the alarm", Toast.LENGTH_SHORT).show()
    }
}
