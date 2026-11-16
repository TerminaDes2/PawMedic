package com.pawsmedic.mobile.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pawsmedic.mobile.MainActivity
import com.pawsmedic.mobile.PawMedicApplication
import com.pawsmedic.mobile.R
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

class PawMedicMessagingService : FirebaseMessagingService() {
    private companion object {
        const val TAG = "PawMedicNotifications"
    }

    override fun onNewToken(token: String) {
        NotificationDeviceSync.schedule(this)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.i(TAG, "Notification data received")
        if (!NotificationDeviceSync.enabled(this)) {
            Log.i(TAG, "Notification ignored: disabled")
            return
        }
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Notification ignored: permission missing")
            return
        }
        if (message.data["type"] !in setOf("appointment_status", "appointment_reminder_24h", "appointment_reminder_1h")) {
            Log.w(TAG, "Notification ignored: unsupported type")
            return
        }
        val isCurrentUser = runBlocking {
            withTimeoutOrNull(8_000) {
                val auth = (application as PawMedicApplication).supabaseClient.auth
                auth.awaitInitialization()
                val userId = auth.currentSessionOrNull()?.user?.id
                userId != null && userId == NotificationDeviceSync.registeredUser(this@PawMedicMessagingService)
            } ?: false
        }
        if (!isCurrentUser) {
            Log.w(TAG, "Notification ignored: session unavailable or account mismatch")
            return
        }
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            Log.w(TAG, "Notification ignored: blocked by system settings")
            return
        }

        val channelId = "appointments"
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(channelId, "Citas", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val intent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val text = when (message.data["type"]) {
            "appointment_reminder_24h" -> "Recordatorio de 24 horas: consulta los detalles de tu cita."
            "appointment_reminder_1h" -> "Recordatorio de 1 hora: consulta los detalles de tu cita."
            else -> "Una de tus citas cambió de estado. Abre PawMedic para consultar los detalles."
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("PawMedic")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(intent)
            .setAutoCancel(true)
            .build()
        val notificationId = message.data["delivery_id"]?.hashCode() ?: message.messageId?.hashCode() ?: 0
        NotificationManagerCompat.from(this).notify(notificationId, notification)
        Log.i(TAG, "Notification displayed")
    }
}
