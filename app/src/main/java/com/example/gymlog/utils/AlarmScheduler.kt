package com.example.gymlog.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log // Importe o Log
import com.example.gymlog.broadcast.AlarmReceiver
import java.text.SimpleDateFormat
import java.util.*

object AlarmScheduler {

    fun scheduleAlarm(context: Context, workoutName: String, timeInMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("workoutName", workoutName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            workoutName.hashCode(), // Use um request code único para cada alarme
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Log para verificar se o agendamento está sendo chamado
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val date = sdf.format(timeInMillis)
        Log.d("AlarmScheduler", "Agendando alarme para '$workoutName' em: $date")


        // Verifica se o app pode agendar alarmes exatos
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle( // Use setExactAndAllowWhileIdle para mais precisão
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
            Log.d("AlarmScheduler", "Alarme agendado com sucesso.")
        } else {
            Log.w("AlarmScheduler", "Não foi possível agendar alarme exato. A permissão não foi concedida.")
            // Opcional: Redirecionar o usuário para as configurações para habilitar a permissão.
        }
    }
}