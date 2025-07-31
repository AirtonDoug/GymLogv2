package com.example.gymlog.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log // Importe o Log
import com.example.gymlog.services.NotificationService

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val workoutName = intent.getStringExtra("workoutName") ?: "Nome do Treino Desconhecido"
        // Log para confirmar que o alarme foi recebido
        Log.d("AlarmReceiver", "Alarme recebido para o treino: $workoutName. Iniciando serviço...")

        val serviceIntent = Intent(context, NotificationService::class.java).apply {
            putExtra("workoutName", workoutName)
        }
        context.startService(serviceIntent)
    }
}