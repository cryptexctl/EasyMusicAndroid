package com.platon.easymusicandroid

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import android.support.v4.media.session.MediaSessionCompat // Импортируем MediaSessionCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem

class MusicService : Service() {

    private val binder = MusicBinder()
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSessionCompat

    companion object {
        const val CHANNEL_ID = "MusicChannel"
        const val NOTIFICATION_ID = 1

        const val ACTION_PLAY = "action_play"
        const val ACTION_PAUSE = "action_pause"
        const val ACTION_NEXT = "action_next"
        const val ACTION_PREVIOUS = "action_previous"
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onCreate() {
        super.onCreate()

        // Инициализация ExoPlayer
        player = ExoPlayer.Builder(this).build()

        // Инициализация MediaSession
        mediaSession = MediaSessionCompat(this, "MusicService")
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                super.onPlay()
                player.play()
                startForeground(NOTIFICATION_ID, createNotification())
            }

            override fun onPause() {
                super.onPause()
                player.pause()
                stopForeground(false)
                updateNotification()
            }

            override fun onStop() {
                super.onStop()
                player.stop()
                stopForeground(true)
                stopSelf()
            }
        })
        mediaSession.isActive = true

        // Создание Notification Channel
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> mediaSession.controller.transportControls.play()
            ACTION_PAUSE -> mediaSession.controller.transportControls.pause()
            ACTION_NEXT -> {
                // Реализация переключения трека вперед
            }
            ACTION_PREVIOUS -> {
                // Реализация переключения трека назад
            }
        }

        // Убедитесь, что служба всегда работает как foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, createNotification())
        }

        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Перезапуск службы, если система ее завершает
        val restartServiceIntent = Intent(applicationContext, this::class.java).apply {
            setPackage(packageName)
        }
        val restartServicePendingIntent = PendingIntent.getService(
            applicationContext,
            1,
            restartServiceIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent
        )
        super.onTaskRemoved(rootIntent)
    }

    fun play(mediaUrl: String) {
        val mediaItem = MediaItem.fromUri(mediaUrl)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    fun pause() {
        player.pause()
        stopForeground(false)
    }

    fun isPlaying(): Boolean = player.isPlaying

    override fun onDestroy() {
        super.onDestroy()
        player.release()
        mediaSession.release()
    }

    private fun createNotification(): Notification {
        val playPauseAction = if (player.isPlaying) {
            NotificationCompat.Action(
                android.R.drawable.ic_media_pause,
                "Pause",
                createPendingIntent(ACTION_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                android.R.drawable.ic_media_play,
                "Play",
                createPendingIntent(ACTION_PLAY)
            )
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Easy Music")
            .setContentText("Now playing your favorite music")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .addAction(
                NotificationCompat.Action(
                    android.R.drawable.ic_media_previous,
                    "Previous",
                    createPendingIntent(ACTION_PREVIOUS)
                )
            )
            .addAction(playPauseAction)
            .addAction(
                NotificationCompat.Action(
                    android.R.drawable.ic_media_next,
                    "Next",
                    createPendingIntent(ACTION_NEXT)
                )
            )
            .setStyle(MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, createNotification())
    }

    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}