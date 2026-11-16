package com.pawsmedic.mobile.notifications

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.pawsmedic.mobile.PawMedicApplication
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal object NotificationDeviceSync {
    private const val PREFERENCES = "notification_device"
    private const val ENABLED = "enabled"
    private const val REGISTERED_TOKEN = "registered_token"
    private const val REGISTERED_USER = "registered_user"
    private const val PENDING_TOKEN = "pending_token"
    private const val PENDING_USER = "pending_user"
    private const val WORK = "notification_device_sync"
    private val mutex = Mutex()

    fun enabled(context: Context): Boolean =
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).getBoolean(ENABLED, false)

    fun registeredUser(context: Context): String? =
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).getString(REGISTERED_USER, null)

    fun schedule(context: Context) {
        if (!enabled(context)) return
        val request = OneTimeWorkRequestBuilder<NotificationDeviceWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK, ExistingWorkPolicy.REPLACE, request)
    }

    suspend fun synchronize(context: Context): Boolean = mutex.withLock {
        if (!enabled(context)) return@withLock true
        val client = (context.applicationContext as PawMedicApplication).supabaseClient
        client.auth.awaitInitialization()
        when (client.auth.sessionStatus.value) {
            is SessionStatus.RefreshFailure, SessionStatus.Initializing -> return@withLock false
            is SessionStatus.NotAuthenticated -> return@withLock true
            is SessionStatus.Authenticated -> Unit
        }
        val currentUser = client.auth.currentSessionOrNull()?.user?.id
        if (currentUser == null || currentUser != registeredUser(context)) return@withLock true
        check(FirebaseApp.getApps(context).isNotEmpty()) { "Firebase is not configured" }
        val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        clearPending(preferences, client, currentUser)
        val token = FirebaseMessaging.getInstance().token.await()
        val previous = preferences.getString(REGISTERED_TOKEN, null)
        persist(preferences.edit().putString(PENDING_TOKEN, token).putString(PENDING_USER, currentUser))
        client.postgrest.rpc("register_notification_device", buildJsonObject {
            put("p_token", token)
            if (previous != null) put("p_previous_token", previous)
        })
        persist(preferences.edit().putString(REGISTERED_TOKEN, token)
            .remove(PENDING_TOKEN).remove(PENDING_USER))
        true
    }

    suspend fun enable(context: Context) = mutex.withLock {
        val client = (context.applicationContext as PawMedicApplication).supabaseClient
        client.auth.awaitInitialization()
        val userId = requireNotNull(client.auth.currentSessionOrNull()?.user?.id) {
            "Debes iniciar sesión para activar las notificaciones."
        }
        check(FirebaseApp.getApps(context).isNotEmpty()) { "Firebase no está configurado." }
        val messaging = FirebaseMessaging.getInstance()
        messaging.isAutoInitEnabled = true
        try {
            val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            clearPending(preferences, client, userId)
            val token = messaging.token.await()
            val previous = preferences.getString(REGISTERED_TOKEN, null)
            persist(preferences.edit().putString(PENDING_TOKEN, token).putString(PENDING_USER, userId))
            client.postgrest.rpc("register_notification_device", buildJsonObject {
                put("p_token", token)
                if (previous != null) put("p_previous_token", previous)
            })
            persist(preferences.edit().putBoolean(ENABLED, true).putString(REGISTERED_TOKEN, token)
                .putString(REGISTERED_USER, userId).remove(PENDING_TOKEN).remove(PENDING_USER))
        } catch (e: CancellationException) {
            messaging.isAutoInitEnabled = false
            throw e
        } catch (e: Exception) {
            messaging.isAutoInitEnabled = false
            throw e
        }
    }

    suspend fun disable(context: Context) = mutex.withLock {
        val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val tokens = listOfNotNull(
            preferences.getString(REGISTERED_TOKEN, null)?.let { it to preferences.getString(REGISTERED_USER, null) },
            preferences.getString(PENDING_TOKEN, null)?.let { it to preferences.getString(PENDING_USER, null) }
        ).distinct()
        if (tokens.isNotEmpty()) {
            val client = (context.applicationContext as PawMedicApplication).supabaseClient
            client.auth.awaitInitialization()
            val userId = client.auth.currentSessionOrNull()?.user?.id
            for ((token, owner) in tokens) {
                check(userId != null && userId == owner) {
                    "La cuenta de notificaciones no coincide con la sesión actual."
                }
                client.postgrest.rpc("unregister_notification_device", buildJsonObject { put("p_token", token) })
            }
        }
        persist(preferences.edit().clear())
        if (FirebaseApp.getApps(context).isNotEmpty()) {
            FirebaseMessaging.getInstance().isAutoInitEnabled = false
        }
        WorkManager.getInstance(context).cancelUniqueWork(WORK)
    }

    suspend fun clearAfterAccountDeletion(context: Context) = mutex.withLock {
        persist(context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit().clear())
        if (FirebaseApp.getApps(context).isNotEmpty()) {
            FirebaseMessaging.getInstance().isAutoInitEnabled = false
        }
        WorkManager.getInstance(context).cancelUniqueWork(WORK)
    }

    private suspend fun persist(editor: android.content.SharedPreferences.Editor) {
        withContext(Dispatchers.IO) {
            check(editor.commit()) { "No se pudo guardar la configuración de notificaciones." }
        }
    }

    private suspend fun clearPending(
        preferences: android.content.SharedPreferences,
        client: SupabaseClient,
        userId: String
    ) {
        val token = preferences.getString(PENDING_TOKEN, null) ?: return
        check(preferences.getString(PENDING_USER, null) == userId) {
            "La cuenta de notificaciones no coincide con la sesión actual."
        }
        client.postgrest.rpc("unregister_notification_device", buildJsonObject { put("p_token", token) })
        persist(preferences.edit().remove(PENDING_TOKEN).remove(PENDING_USER))
    }
}

class NotificationDeviceWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result = try {
        if (NotificationDeviceSync.synchronize(applicationContext)) Result.success() else Result.retry()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w("PawMedicNotifications", "Device registration failed (${e.javaClass.simpleName}); will retry")
        Result.retry()
    }
}
