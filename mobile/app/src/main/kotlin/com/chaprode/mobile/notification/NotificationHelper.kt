package com.chaprode.mobile.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.chaprode.mobile.MainActivity

class NotificationHelper(private val context: Context) {

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelReminders = NotificationChannel(
                CHANNEL_REMINDERS_ID,
                "Recordatorios de Partidos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas cuando faltan pocos minutos para el cierre de pronósticos"
                enableVibration(true)
            }

            val channelResults = NotificationChannel(
                CHANNEL_RESULTS_ID,
                "Resultados y Puntuaciones",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones cuando finaliza un partido y ganas puntos"
                enableVibration(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channelReminders)
            manager.createNotificationChannel(channelResults)
        }
    }

    fun showMatchClosingReminder(matchTitle: String, minutesRemaining: Int = 5) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⏱️ ¡Pronóstico por cerrar! (-$minutesRemaining min)")
            .setContentText("El partido $matchTitle cierra sus pronósticos en $minutesRemaining minutos. ¡No te quedes sin sumar!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_REMINDER_ID, notification)
        } catch (e: SecurityException) {
            // Permiso no otorgado en Android 13+
        }
    }

    fun showPointsWonNotification(matchTitle: String, points: Int, isExact: Boolean) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = if (isExact) "🎉 ¡PLENO EXACTO! (+3 Pts)" else "⚽ ¡Sumaste puntos! (+$points Pts)"
        val message = "Finalizó $matchTitle. Ganaste $points puntos para la tabla de posiciones."

        val notification = NotificationCompat.Builder(context, CHANNEL_RESULTS_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_RESULTS_ID, notification)
        } catch (e: SecurityException) {
            // Permiso no otorgado en Android 13+
        }
    }

    companion object {
        const val CHANNEL_REMINDERS_ID = "chaprode_match_reminders"
        const val CHANNEL_RESULTS_ID = "chaprode_match_results"

        const val NOTIFICATION_REMINDER_ID = 1001
        const val NOTIFICATION_RESULTS_ID = 1002
    }
}
