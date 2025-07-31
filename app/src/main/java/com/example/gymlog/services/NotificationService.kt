package com.example.gymlog.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log // Importe o Log
import androidx.core.app.NotificationCompat
import com.example.gymlog.MainActivity
import com.example.gymlog.R

class NotificationService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val workoutName = intent?.getStringExtra("workoutName") ?: "Workout"
        // Log para confirmar que o serviço iniciou
        Log.d("NotificationService", "Serviço iniciado para mostrar notificação do treino: $workoutName")

        showNotification(workoutName)
        return START_NOT_STICKY
    }

    private fun showNotification(workoutName: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "workout_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Lembretes de Treino", NotificationManager.IMPORTANCE_HIGH) // Use HIGH importance
            channel.description = "Canal para lembretes de treinos agendados"
            notificationManager.createNotificationChannel(channel)
            Log.d("NotificationService", "Canal de notificação criado ou já existente.")
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Lembrete de Treino do GymLog!")
            .setContentText("Está na hora do seu treino: $workoutName")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use o ícone de foreground
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Defina a prioridade alta
            .setAutoCancel(true)
            .build()

        // Use um ID de notificação único
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
        Log.d("NotificationService", "Notificação #${notificationId} enviada.")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}