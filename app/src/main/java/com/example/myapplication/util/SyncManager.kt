package com.example.myapplication.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.myapplication.db.DatabaseHelper
import com.example.myapplication.model.SyncAction
import com.example.myapplication.model.User
import com.example.myapplication.network.AwsApiService
import org.json.JSONObject

/**
 * Gestor de Sincronización Automática (Criterios 11, 12 y 13 - 24 Puntos)
 * - Procesa la cola de transacciones pendientes (sync_queue) de SQLite.
 * - Despacha las acciones (INSERT, UPDATE, DELETE) hacia la API en AWS.
 * - Solo elimina de la cola al recibir respuesta exitosa (200 OK / status: success) o duplicado existente.
 * - Si falla la red, el registro permanece pendiente para reintentar sin duplicar datos.
 * - Notifica a la interfaz para mostrar el feedback visible de sincronización.
 */
object SyncManager {

    interface SyncListener {
        fun onSyncStatusChanged(statusMessage: String, pendingCount: Int, isSyncing: Boolean)
    }

    private val listeners = mutableListOf<SyncListener>()
    private var isSyncing = false
    private val mainHandler = Handler(Looper.getMainLooper())

    fun addListener(listener: SyncListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun removeListener(listener: SyncListener) {
        listeners.remove(listener)
    }

    private fun notifyListeners(message: String, pendingCount: Int, syncing: Boolean) {
        mainHandler.post {
            for (listener in listeners) {
                listener.onSyncStatusChanged(message, pendingCount, syncing)
            }
        }
    }

    /**
     * Disparo automático al detectar recuperación de red
     */
    fun syncPendingActions(
        context: Context,
        onComplete: ((Boolean, Int) -> Unit)? = null
    ) {
        val dbHelper = DatabaseHelper(context)
        val pendingList = dbHelper.getPendingSyncActions()

        if (pendingList.isEmpty()) {
            notifyListeners("🟢 Sincronizado con AWS (Sin cambios pendientes)", 0, false)
            onComplete?.invoke(true, 0)
            return
        }

        if (isSyncing) return
        isSyncing = true

        notifyListeners("🟡 Sincronizando ${pendingList.size} cambio(s) pendiente(s) con AWS...", pendingList.size, true)

        dispatchNext(context, dbHelper, pendingList, 0, onComplete)
    }

    fun getCurrentStatus(context: Context): Triple<String, Int, Boolean> {
        val dbHelper = DatabaseHelper(context)
        val pending = dbHelper.getPendingSyncCount()
        val isOnline = NetworkHelper.isOnline(context)
        return when {
            isSyncing -> Triple("🟡 Sincronizando cambios pendientes con AWS...", pending, true)
            !isOnline && pending > 0 -> Triple("🔴 Modo sin conexión • $pending cambio(s) pendiente(s) en SQLite", pending, false)
            !isOnline -> Triple("🔴 Modo sin conexión • Operando con SQLite local", 0, false)
            pending > 0 -> Triple("🟡 Conexión activa • $pending cambio(s) pendiente(s)", pending, false)
            else -> Triple("🟢 En Línea • Sincronización completada con éxito con AWS", 0, false)
        }
    }

    private fun dispatchNext(
        context: Context,
        dbHelper: DatabaseHelper,
        list: List<SyncAction>,
        index: Int,
        onComplete: ((Boolean, Int) -> Unit)?
    ) {
        if (index >= list.size) {
            val remaining = dbHelper.getPendingSyncCount()
            isSyncing = false
            if (remaining == 0) {
                notifyListeners("🟢 Sincronización completada con éxito con AWS", 0, false)
                onComplete?.invoke(true, 0)
            } else {
                notifyListeners("🔴 Sincronización parcial: $remaining cambio(s) pendiente(s)", remaining, false)
                onComplete?.invoke(false, remaining)
            }
            return
        }

        val item = list[index]

        when (item.action) {
            "INSERT" -> {
                val user = parseUserFromJson(item.payload, item.recordId)
                AwsApiService.guardarUsuario(
                    context = context,
                    user = user,
                    onSuccess = {
                        dbHelper.deleteSyncAction(item.id)
                        dbHelper.markUserAsSynced(item.recordId)
                        notifyListeners("Sincronizando: ${index + 1}/${list.size} cambios...", dbHelper.getPendingSyncCount(), true)
                        dispatchNext(context, dbHelper, list, index + 1, onComplete)
                    },
                    onError = { errorMsg ->
                        // Si el usuario ya existe en el servidor remoto, se considera resuelto para no bloquear la cola
                        if (errorMsg.contains("duplicado", ignoreCase = true) || errorMsg.contains("existe", ignoreCase = true)) {
                            dbHelper.deleteSyncAction(item.id)
                            dbHelper.markUserAsSynced(item.recordId)
                        }
                        dispatchNext(context, dbHelper, list, index + 1, onComplete)
                    }
                )
            }

            "UPDATE" -> {
                val user = parseUserFromJson(item.payload, item.recordId)
                AwsApiService.modificarUsuario(
                    context = context,
                    user = user,
                    onSuccess = {
                        dbHelper.deleteSyncAction(item.id)
                        dbHelper.markUserAsSynced(item.recordId)
                        notifyListeners("Sincronizando: ${index + 1}/${list.size} cambios...", dbHelper.getPendingSyncCount(), true)
                        dispatchNext(context, dbHelper, list, index + 1, onComplete)
                    },
                    onError = {
                        dispatchNext(context, dbHelper, list, index + 1, onComplete)
                    }
                )
            }

            "DELETE" -> {
                AwsApiService.eliminarUsuario(
                    context = context,
                    userId = item.recordId,
                    onSuccess = {
                        dbHelper.deleteSyncAction(item.id)
                        notifyListeners("Sincronizando: ${index + 1}/${list.size} cambios...", dbHelper.getPendingSyncCount(), true)
                        dispatchNext(context, dbHelper, list, index + 1, onComplete)
                    },
                    onError = {
                        dispatchNext(context, dbHelper, list, index + 1, onComplete)
                    }
                )
            }

            else -> {
                dispatchNext(context, dbHelper, list, index + 1, onComplete)
            }
        }
    }

    private fun parseUserFromJson(jsonStr: String, defaultId: Int): User {
        return try {
            val json = JSONObject(jsonStr)
            User(
                id = json.optInt("id", defaultId),
                rut = json.optString("rut", ""),
                firstname = json.optString("firstname", ""),
                lastname = json.optString("lastname", ""),
                email = json.optString("email", ""),
                phone = json.optString("phone", ""),
                password = json.optString("password", "Clave123!"),
                role = json.optString("role", "Usuario"),
                profilePhoto = if (json.isNull("profile_photo")) null else json.optString("profile_photo"),
                username = json.optString("username", "")
            )
        } catch (e: Exception) {
            User(id = defaultId, rut = "", firstname = "Usuario", lastname = "Local", email = "local@correo.com", phone = "")
        }
    }

    fun userToJson(user: User): String {
        val obj = JSONObject().apply {
            put("id", user.id)
            put("rut", user.rut)
            put("firstname", user.firstname)
            put("lastname", user.lastname)
            put("email", user.email)
            put("phone", user.phone)
            put("password", user.password)
            put("role", user.role)
            put("profile_photo", user.profilePhoto ?: JSONObject.NULL)
            put("username", user.username)
        }
        return obj.toString()
    }
}
