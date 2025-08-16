package com.example.emedibotsimpleuserlogin



import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.IBinder

class RingtonePlayingService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? {
        // This is a started service, not a bound one, so we return null.
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Get the default alarm ringtone URI
        val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Stop any previously playing media
        mediaPlayer?.stop()
        mediaPlayer?.release()

        // Create and start the MediaPlayer
        mediaPlayer = MediaPlayer.create(this, alarmUri)
        mediaPlayer?.isLooping = true // Loop the sound
        mediaPlayer?.start()

        // This ensures the service continues running until it is explicitly stopped.
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop and release the MediaPlayer when the service is destroyed
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }
}