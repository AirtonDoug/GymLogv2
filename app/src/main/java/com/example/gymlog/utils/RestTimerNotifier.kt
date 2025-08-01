package com.example.gymlog.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.gymlog.R

object RestTimerNotifier {

    private const val CHANNEL_ID = "rest_timer_channel"
    private const val NOTIFICATION_ID = 99

    /**
     * Mostra uma notificação de alta prioridade com som para alertar que o descanso terminou.
     * @param context O contexto da aplicação para acessar os serviços do sistema.
     */
    fun showNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Cria o canal de notificação para Android 8.0 (Oreo) e superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Lembretes de Descanso"
            val descriptionText = "Notificações para o fim do tempo de descanso"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                // Habilita som e vibração
                enableVibration(true)
            }
            // Registra o canal no sistema
            notificationManager.createNotificationChannel(channel)
        }

        // Constrói a notificação
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Certifique-se de que este ícone existe
            .setContentTitle("Descanso Finalizado!")
            .setContentText("É hora de voltar ao treino.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) // Define o som padrão
            .setAutoCancel(true) // A notificação some ao ser tocada

        // Exibe a notificação
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}
