package com.example.myapplication.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.myapplication.R
import com.example.myapplication.model.Sensor
import com.example.myapplication.model.User
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "iot_app_db_v5.db"
        private const val DATABASE_VERSION = 5

        private const val TABLE_USERS = "users"
        private const val KEY_USER_ID = "id"
        private const val KEY_USER_USERNAME = "username"
        private const val KEY_USER_FIRSTNAME = "firstname"
        private const val KEY_USER_LASTNAME = "lastname"
        private const val KEY_USER_EMAIL = "email"
        private const val KEY_USER_PASSWORD = "password"
        private const val KEY_USER_PHOTO = "profile_photo"
        private const val KEY_USER_BLOCKED = "is_blocked"
        private const val KEY_USER_SYNC_STATUS = "sync_status"

        private const val TABLE_SENSORS = "sensors"
        private const val KEY_SENSOR_ID = "id"
        private const val KEY_SENSOR_NAME = "name"
        private const val KEY_SENSOR_LOCATION = "location"
        private const val KEY_SENSOR_TEMP = "temperature"
        private const val KEY_SENSOR_HUMIDITY = "humidity"
        private const val KEY_SENSOR_DATE = "date"
        private const val KEY_SENSOR_THERM_ON = "is_thermometer_on"

        // Tabla de Sincronización Pendiente (Criterios 11, 12 y 13)
        private const val TABLE_SYNC_QUEUE = "sync_queue"
        private const val KEY_SYNC_ID = "id"
        private const val KEY_SYNC_ACTION = "action"
        private const val KEY_SYNC_RECORD_ID = "record_id"
        private const val KEY_SYNC_PAYLOAD = "payload"
        private const val KEY_SYNC_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = ("CREATE TABLE " + TABLE_USERS + "("
                + KEY_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USER_USERNAME + " TEXT,"
                + KEY_USER_FIRSTNAME + " TEXT,"
                + KEY_USER_LASTNAME + " TEXT,"
                + KEY_USER_EMAIL + " TEXT UNIQUE,"
                + KEY_USER_PASSWORD + " TEXT,"
                + KEY_USER_PHOTO + " TEXT,"
                + KEY_USER_BLOCKED + " INTEGER DEFAULT 0,"
                + KEY_USER_SYNC_STATUS + " INTEGER DEFAULT 1" + ")")

        val createSensorsTable = ("CREATE TABLE " + TABLE_SENSORS + "("
                + KEY_SENSOR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_SENSOR_NAME + " TEXT,"
                + KEY_SENSOR_LOCATION + " TEXT,"
                + KEY_SENSOR_TEMP + " TEXT,"
                + KEY_SENSOR_HUMIDITY + " TEXT,"
                + KEY_SENSOR_DATE + " TEXT,"
                + KEY_SENSOR_THERM_ON + " INTEGER" + ")")

        val createSyncQueueTable = ("CREATE TABLE " + TABLE_SYNC_QUEUE + "("
                + KEY_SYNC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_SYNC_ACTION + " TEXT NOT NULL,"
                + KEY_SYNC_RECORD_ID + " INTEGER,"
                + KEY_SYNC_PAYLOAD + " TEXT NOT NULL,"
                + KEY_SYNC_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")")

        db.execSQL(createUsersTable)
        db.execSQL(createSensorsTable)
        db.execSQL(createSyncQueueTable)
        insertInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $KEY_USER_PHOTO TEXT")
        } catch (e: Exception) {}
        try {
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $KEY_USER_USERNAME TEXT")
        } catch (e: Exception) {}
        try {
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $KEY_USER_BLOCKED INTEGER DEFAULT 0")
        } catch (e: Exception) {}
        try {
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $KEY_USER_SYNC_STATUS INTEGER DEFAULT 1")
        } catch (e: Exception) {}
        try {
            val createSyncQueueTable = ("CREATE TABLE IF NOT EXISTS " + TABLE_SYNC_QUEUE + "("
                    + KEY_SYNC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_SYNC_ACTION + " TEXT NOT NULL,"
                    + KEY_SYNC_RECORD_ID + " INTEGER,"
                    + KEY_SYNC_PAYLOAD + " TEXT NOT NULL,"
                    + KEY_SYNC_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")")
            db.execSQL(createSyncQueueTable)
        } catch (e: Exception) {}
    }

    private fun currentDateString(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun insertInitialData(db: SQLiteDatabase) {
        val seedUsers = listOf(
            User(1, "", "Jorge Luis", "Cortes Gallardo", "jorge.cortes@empresa.com", "", "Clave123!", profilePhoto = "jorge_cortes"),
            User(2, "", "Kevin", "Encina Molina", "kevin.encina@empresa.com", "", "Clave123!", profilePhoto = "kevin_encina"),
            User(3, "", "Scarlett", "Williams Medalla", "scarlett.williams@empresa.com", "", "Clave123!", profilePhoto = "scarlett_williams")
        )

        for (u in seedUsers) {
            val cv = ContentValues().apply {
                put(KEY_USER_FIRSTNAME, u.firstname)
                put(KEY_USER_LASTNAME, u.lastname)
                put(KEY_USER_EMAIL, u.email)
                put(KEY_USER_PASSWORD, u.password)
                put(KEY_USER_PHOTO, u.profilePhoto)
                put(KEY_USER_SYNC_STATUS, 1)
            }
            db.insert(TABLE_USERS, null, cv)
        }

        val now = currentDateString()
        val seedSensors = listOf(
            Triple("Sensor Clima Ovalle Central", "Ovalle", "23.5"),
            Triple("Sensor Clima Coquimbo Costa", "Coquimbo", "17.8"),
            Triple("Sensor Clima La Serena Centro", "La Serena", "21.2"),
            Triple("Sensor Clima Tierras Blancas", "Tierras Blancas", "19.4"),
            Triple("Sensor Clima Vicuña Elqui", "Vicuña", "22.0"),
            Triple("Sensor Clima Andacollo Cordillera", "Andacollo", "20.5"),
            Triple("Sensor Clima Monte Patria Valles", "Monte Patria", "24.1"),
            Triple("Sensor Clima Salamanca Choapa", "Salamanca", "22.8")
        )

        for ((name, location, temp) in seedSensors) {
            val s = ContentValues().apply {
                put(KEY_SENSOR_NAME, name)
                put(KEY_SENSOR_LOCATION, location)
                put(KEY_SENSOR_TEMP, temp)
                put(KEY_SENSOR_HUMIDITY, "58%")
                put(KEY_SENSOR_DATE, now)
                put(KEY_SENSOR_THERM_ON, 1)
            }
            db.insert(TABLE_SENSORS, null, s)
        }
    }

    // --- USER CRUD ---

    fun insertUser(user: User, syncStatus: Int = 1): Long {
        val db = writableDatabase
        val defaultUsername = if (user.username.isNotEmpty()) user.username else {
            val fnClean = user.firstname.lowercase(Locale.getDefault()).replace("\\s+".toRegex(), "")
            val lnClean = user.lastname.lowercase(Locale.getDefault()).replace("\\s+".toRegex(), "")
            "$fnClean.$lnClean"
        }
        val values = ContentValues().apply {
            put(KEY_USER_USERNAME, defaultUsername)
            put(KEY_USER_FIRSTNAME, user.firstname)
            put(KEY_USER_LASTNAME, user.lastname)
            put(KEY_USER_EMAIL, user.email.lowercase(Locale.getDefault()).trim())
            put(KEY_USER_PASSWORD, user.password)
            put(KEY_USER_PHOTO, user.profilePhoto)
            put(KEY_USER_BLOCKED, if (user.isBlocked) 1 else 0)
            put(KEY_USER_SYNC_STATUS, syncStatus)
        }
        return try {
            db.insertOrThrow(TABLE_USERS, null, values)
        } catch (e: Exception) {
            -1L
        }
    }

    fun isEmailExists(email: String): Boolean {
        val db = readableDatabase
        val cleanEmail = email.lowercase(Locale.getDefault()).trim()
        val cursor = db.rawQuery(
            "SELECT $KEY_USER_ID FROM $TABLE_USERS WHERE LOWER($KEY_USER_EMAIL) = ?",
            arrayOf(cleanEmail)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun isUsernameExists(username: String): Boolean {
        val db = readableDatabase
        val cleanUser = username.lowercase(Locale.getDefault()).trim()
        val cursor = db.rawQuery(
            "SELECT $KEY_USER_ID FROM $TABLE_USERS WHERE LOWER($KEY_USER_USERNAME) = ?",
            arrayOf(cleanUser)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun validateLocalLogin(identifier: String, pass: String): Triple<Boolean, Boolean, User?> {
        val db = readableDatabase
        val clean = identifier.lowercase(Locale.getDefault()).trim()
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE LOWER($KEY_USER_EMAIL) = ? OR LOWER($KEY_USER_USERNAME) = ?",
            arrayOf(clean, clean)
        )
        if (cursor.moveToFirst()) {
            val passDb = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_PASSWORD))
            val blockedCol = cursor.getColumnIndex(KEY_USER_BLOCKED)
            val isBlocked = if (blockedCol != -1) cursor.getInt(blockedCol) == 1 else false
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_ID))
            val fn = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_FIRSTNAME))
            val ln = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_LASTNAME))
            val em = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_EMAIL))
            val user = User(id = id, rut = "", firstname = fn, lastname = ln, email = em, phone = "", isBlocked = isBlocked)
            cursor.close()
            return if (passDb == pass) {
                Triple(true, isBlocked, user)
            } else {
                Triple(false, isBlocked, null)
            }
        }
        cursor.close()
        return Triple(false, false, null)
    }

    fun blockUserByEmail(email: String): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply { put(KEY_USER_BLOCKED, 1) }
        return db.update(TABLE_USERS, cv, "LOWER($KEY_USER_EMAIL) = ?", arrayOf(email.lowercase(Locale.getDefault()).trim())) > 0
    }

    fun unblockUserAndResetPassword(email: String, newPass: String): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(KEY_USER_PASSWORD, newPass)
            put(KEY_USER_BLOCKED, 0)
        }
        return db.update(TABLE_USERS, cv, "LOWER($KEY_USER_EMAIL) = ?", arrayOf(email.lowercase(Locale.getDefault()).trim())) > 0
    }

    fun searchUsers(query: String): List<User> {
        val userList = mutableListOf<User>()
        val db = readableDatabase
        val normalizedQuery = normalizeString(query)

        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS ORDER BY $KEY_USER_ID ASC", null)

        if (cursor.moveToFirst()) {
            val idIdx = cursor.getColumnIndexOrThrow(KEY_USER_ID)
            val fnIdx = cursor.getColumnIndexOrThrow(KEY_USER_FIRSTNAME)
            val lnIdx = cursor.getColumnIndexOrThrow(KEY_USER_LASTNAME)
            val emailIdx = cursor.getColumnIndexOrThrow(KEY_USER_EMAIL)
            val passIdx = cursor.getColumnIndexOrThrow(KEY_USER_PASSWORD)
            val photoIdx = cursor.getColumnIndex(KEY_USER_PHOTO)
            val userIdx = cursor.getColumnIndex(KEY_USER_USERNAME)
            val blockIdx = cursor.getColumnIndex(KEY_USER_BLOCKED)
            val syncIdx = cursor.getColumnIndex(KEY_USER_SYNC_STATUS)

            do {
                val fn = cursor.getString(fnIdx) ?: ""
                val ln = cursor.getString(lnIdx) ?: ""
                val email = cursor.getString(emailIdx) ?: ""

                val fullTextNormalized = normalizeString("$fn $ln $email")

                if (normalizedQuery.isEmpty() || fullTextNormalized.contains(normalizedQuery)) {
                    val photo = if (photoIdx != -1 && !cursor.isNull(photoIdx)) cursor.getString(photoIdx) else null
                    val uname = if (userIdx != -1 && !cursor.isNull(userIdx)) cursor.getString(userIdx) else ""
                    val isBlocked = if (blockIdx != -1) cursor.getInt(blockIdx) == 1 else false
                    val syncStatus = if (syncIdx != -1 && !cursor.isNull(syncIdx)) cursor.getInt(syncIdx) else 1

                    val user = User(
                        id = cursor.getInt(idIdx),
                        rut = "",
                        firstname = fn,
                        lastname = ln,
                        email = email,
                        phone = "",
                        password = cursor.getString(passIdx) ?: "",
                        profilePhoto = photo,
                        username = uname,
                        isBlocked = isBlocked,
                        syncStatus = syncStatus
                    )
                    userList.add(user)
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return userList
    }

    fun getUserById(userId: Int): User? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $KEY_USER_ID = ?", arrayOf(userId.toString()))
        var user: User? = null
        if (cursor.moveToFirst()) {
            val idIdx = cursor.getColumnIndexOrThrow(KEY_USER_ID)
            val fnIdx = cursor.getColumnIndexOrThrow(KEY_USER_FIRSTNAME)
            val lnIdx = cursor.getColumnIndexOrThrow(KEY_USER_LASTNAME)
            val emailIdx = cursor.getColumnIndexOrThrow(KEY_USER_EMAIL)
            val passIdx = cursor.getColumnIndexOrThrow(KEY_USER_PASSWORD)
            val photoIdx = cursor.getColumnIndex(KEY_USER_PHOTO)
            val userIdx = cursor.getColumnIndex(KEY_USER_USERNAME)
            val blockIdx = cursor.getColumnIndex(KEY_USER_BLOCKED)
            val syncIdx = cursor.getColumnIndex(KEY_USER_SYNC_STATUS)

            val photo = if (photoIdx != -1 && !cursor.isNull(photoIdx)) cursor.getString(photoIdx) else null
            val uname = if (userIdx != -1 && !cursor.isNull(userIdx)) cursor.getString(userIdx) else ""
            val isBlocked = if (blockIdx != -1) cursor.getInt(blockIdx) == 1 else false
            val syncStatus = if (syncIdx != -1 && !cursor.isNull(syncIdx)) cursor.getInt(syncIdx) else 1

            user = User(
                id = cursor.getInt(idIdx),
                rut = "",
                firstname = cursor.getString(fnIdx) ?: "",
                lastname = cursor.getString(lnIdx) ?: "",
                email = cursor.getString(emailIdx) ?: "",
                phone = "",
                password = cursor.getString(passIdx) ?: "",
                profilePhoto = photo,
                username = uname,
                isBlocked = isBlocked,
                syncStatus = syncStatus
            )
        }
        cursor.close()
        return user
    }

    fun updateUser(user: User, syncStatus: Int = 1): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_USER_FIRSTNAME, user.firstname)
            put(KEY_USER_LASTNAME, user.lastname)
            put(KEY_USER_EMAIL, user.email.lowercase(Locale.getDefault()).trim())
            put(KEY_USER_PHOTO, user.profilePhoto)
            put(KEY_USER_SYNC_STATUS, syncStatus)
        }
        return db.update(TABLE_USERS, values, "$KEY_USER_ID = ?", arrayOf(user.id.toString()))
    }

    fun markUserAsSynced(userId: Int): Int {
        val db = writableDatabase
        val cv = ContentValues().apply { put(KEY_USER_SYNC_STATUS, 1) }
        return db.update(TABLE_USERS, cv, "$KEY_USER_ID = ?", arrayOf(userId.toString()))
    }

    fun syncLocalUsersWithRemote(remoteUsers: List<User>) {
        val db = writableDatabase
        for (u in remoteUsers) {
            val defaultUsername = if (u.username.isNotEmpty()) u.username else {
                val fnClean = u.firstname.lowercase(Locale.getDefault()).replace("\\s+".toRegex(), "")
                val lnClean = u.lastname.lowercase(Locale.getDefault()).replace("\\s+".toRegex(), "")
                "$fnClean.$lnClean"
            }
            val cv = ContentValues().apply {
                put(KEY_USER_ID, u.id)
                put(KEY_USER_USERNAME, defaultUsername)
                put(KEY_USER_FIRSTNAME, u.firstname)
                put(KEY_USER_LASTNAME, u.lastname)
                put(KEY_USER_EMAIL, u.email.lowercase(Locale.getDefault()).trim())
                put(KEY_USER_PASSWORD, if (u.password.isNotEmpty()) u.password else "Clave123!")
                put(KEY_USER_PHOTO, u.profilePhoto)
                put(KEY_USER_BLOCKED, if (u.isBlocked) 1 else 0)
                put(KEY_USER_SYNC_STATUS, 1)
            }
            db.insertWithOnConflict(TABLE_USERS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    fun deleteUser(userId: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_USERS, "$KEY_USER_ID = ?", arrayOf(userId.toString()))
    }

    // --- SENSOR CRUD ---

    fun insertSensor(sensor: Sensor): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_SENSOR_NAME, sensor.name)
            put(KEY_SENSOR_LOCATION, sensor.location)
            put(KEY_SENSOR_TEMP, sensor.temperature)
            put(KEY_SENSOR_HUMIDITY, sensor.humidity)
            put(KEY_SENSOR_DATE, currentDateString())
            put(KEY_SENSOR_THERM_ON, if (sensor.isThermometerOn) 1 else 0)
        }
        return db.insert(TABLE_SENSORS, null, values)
    }

    fun getAllSensors(): List<Sensor> {
        val sensorList = mutableListOf<Sensor>()
        val selectQuery = "SELECT * FROM $TABLE_SENSORS ORDER BY $KEY_SENSOR_ID ASC"
        val db = readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            val idIdx = cursor.getColumnIndexOrThrow(KEY_SENSOR_ID)
            val nameIdx = cursor.getColumnIndexOrThrow(KEY_SENSOR_NAME)
            val locIdx = cursor.getColumnIndexOrThrow(KEY_SENSOR_LOCATION)
            val tempIdx = cursor.getColumnIndexOrThrow(KEY_SENSOR_TEMP)
            val humIdx = cursor.getColumnIndexOrThrow(KEY_SENSOR_HUMIDITY)
            val dateIdx = cursor.getColumnIndexOrThrow(KEY_SENSOR_DATE)
            val thermIdx = cursor.getColumnIndexOrThrow(KEY_SENSOR_THERM_ON)

            do {
                val sensor = Sensor(
                    id = cursor.getInt(idIdx),
                    name = cursor.getString(nameIdx),
                    location = cursor.getString(locIdx),
                    temperature = cursor.getString(tempIdx),
                    humidity = cursor.getString(humIdx),
                    date = cursor.getString(dateIdx),
                    isThermometerOn = cursor.getInt(thermIdx) == 1
                )
                sensorList.add(sensor)
            } while (cursor.moveToNext())
        }
        cursor.close()

        if (sensorList.size < 6) {
            ensureDefaultSensors()
            return fetchSensorsRaw()
        }
        return sensorList
    }

    private fun fetchSensorsRaw(): List<Sensor> {
        val sensorList = mutableListOf<Sensor>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SENSORS ORDER BY $KEY_SENSOR_ID ASC", null)
        if (cursor.moveToFirst()) {
            val idIdx = cursor.getColumnIndexOrThrow(KEY_SENSOR_ID)
            val nameIdx = cursor.getColumnIndexOrThrow(KEY_SENSOR_NAME)
            val locIdx = cursor.getColumnIndexOrThrow(KEY_SENSOR_LOCATION)
            val tempIdx = cursor.getColumnIndexOrThrow(KEY_SENSOR_TEMP)
            val humIdx = cursor.getColumnIndexOrThrow(KEY_SENSOR_HUMIDITY)
            val dateIdx = cursor.getColumnIndexOrThrow(KEY_SENSOR_DATE)
            val thermIdx = cursor.getColumnIndexOrThrow(KEY_SENSOR_THERM_ON)

            do {
                sensorList.add(
                    Sensor(
                        id = cursor.getInt(idIdx),
                        name = cursor.getString(nameIdx),
                        location = cursor.getString(locIdx),
                        temperature = cursor.getString(tempIdx),
                        humidity = cursor.getString(humIdx),
                        date = cursor.getString(dateIdx),
                        isThermometerOn = cursor.getInt(thermIdx) == 1
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return sensorList
    }

    fun ensureDefaultSensors() {
        val db = writableDatabase
        val existingNames = mutableSetOf<String>()
        val cursor = db.rawQuery("SELECT $KEY_SENSOR_NAME FROM $TABLE_SENSORS", null)
        if (cursor.moveToFirst()) {
            do {
                existingNames.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()

        val now = currentDateString()
        val defaultSensors = listOf(
            Triple("Sensor Clima Ovalle Central", "Ovalle", "23.5"),
            Triple("Sensor Clima Coquimbo Costa", "Coquimbo", "17.8"),
            Triple("Sensor Clima La Serena Centro", "La Serena", "21.2"),
            Triple("Sensor Clima Tierras Blancas", "Tierras Blancas", "19.4"),
            Triple("Sensor Clima Vicuña Elqui", "Vicuña", "22.0"),
            Triple("Sensor Clima Andacollo Cordillera", "Andacollo", "20.5"),
            Triple("Sensor Clima Monte Patria Valles", "Monte Patria", "24.1"),
            Triple("Sensor Clima Salamanca Choapa", "Salamanca", "22.8")
        )

        for ((name, location, temp) in defaultSensors) {
            if (!existingNames.contains(name)) {
                val s = ContentValues().apply {
                    put(KEY_SENSOR_NAME, name)
                    put(KEY_SENSOR_LOCATION, location)
                    put(KEY_SENSOR_TEMP, temp)
                    put(KEY_SENSOR_HUMIDITY, "58%")
                    put(KEY_SENSOR_DATE, now)
                    put(KEY_SENSOR_THERM_ON, 1)
                }
                db.insert(TABLE_SENSORS, null, s)
            }
        }
    }

    fun updateSensor(sensor: Sensor): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_SENSOR_NAME, sensor.name)
            put(KEY_SENSOR_LOCATION, sensor.location)
            put(KEY_SENSOR_TEMP, sensor.temperature)
            put(KEY_SENSOR_HUMIDITY, sensor.humidity)
            put(KEY_SENSOR_DATE, currentDateString())
            put(KEY_SENSOR_THERM_ON, if (sensor.isThermometerOn) 1 else 0)
        }
        return db.update(TABLE_SENSORS, values, "$KEY_SENSOR_ID = ?", arrayOf(sensor.id.toString()))
    }

    fun toggleThermometer(sensorId: Int, isOn: Boolean): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_SENSOR_THERM_ON, if (isOn) 1 else 0)
            put(KEY_SENSOR_DATE, currentDateString())
        }
        return db.update(TABLE_SENSORS, values, "$KEY_SENSOR_ID = ?", arrayOf(sensorId.toString()))
    }

    fun deleteSensor(sensorId: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_SENSORS, "$KEY_SENSOR_ID = ?", arrayOf(sensorId.toString()))
    }

    fun updateAllActiveSensorsFromApi(baseTempStr: String, baseHumStr: String) {
        val db = writableDatabase
        val baseTemp = baseTempStr.toDoubleOrNull() ?: 21.5
        val baseHum = baseHumStr.replace("%", "").trim().toDoubleOrNull() ?: 58.0
        val now = currentDateString()

        val cursor = db.rawQuery("SELECT $KEY_SENSOR_ID FROM $TABLE_SENSORS", null)
        val ids = mutableListOf<Int>()
        if (cursor.moveToFirst()) {
            val idIdx = cursor.getColumnIndexOrThrow(KEY_SENSOR_ID)
            do {
                ids.add(cursor.getInt(idIdx))
            } while (cursor.moveToNext())
        }
        cursor.close()

        for ((index, sensorId) in ids.withIndex()) {
            val tempOffset = when (index % 8) {
                0 -> 0.0
                1 -> -2.3
                2 -> 1.8
                3 -> -1.2
                4 -> 0.7
                5 -> -1.5
                6 -> 2.2
                else -> 1.1
            }
            val humOffset = when (index % 8) {
                0 -> 0.0
                1 -> 5.0
                2 -> -4.0
                3 -> 3.0
                4 -> -2.0
                5 -> 4.0
                6 -> -6.0
                else -> 2.0
            }
            val sensorTemp = String.format(Locale.US, "%.1f", baseTemp + tempOffset)
            val sensorHum = "${(baseHum + humOffset).toInt().coerceIn(30, 95)}%"

            val values = ContentValues().apply {
                put(KEY_SENSOR_TEMP, sensorTemp)
                put(KEY_SENSOR_HUMIDITY, sensorHum)
                put(KEY_SENSOR_DATE, now)
                put(KEY_SENSOR_THERM_ON, 1)
            }
            db.update(TABLE_SENSORS, values, "$KEY_SENSOR_ID = ?", arrayOf(sensorId.toString()))
        }
    }

    fun simulateRealtimeReadings() {
        val sensors = getAllSensors()
        val db = writableDatabase
        val now = currentDateString()
        for (s in sensors) {
            if (s.isThermometerOn) {
                val currentVal = s.temperature.toDoubleOrNull() ?: 22.0
                val delta = Random.nextDouble(-0.8, 0.8)
                val newVal = String.format(Locale.US, "%.1f", currentVal + delta)
                val cv = ContentValues().apply {
                    put(KEY_SENSOR_TEMP, newVal)
                    put(KEY_SENSOR_DATE, now)
                }
                db.update(TABLE_SENSORS, cv, "$KEY_SENSOR_ID = ?", arrayOf(s.id.toString()))
            }
        }
    }

    private fun normalizeString(text: String): String {
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
        return normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "").lowercase(Locale.getDefault()).trim()
    }

    // --- COLA DE CAMBIOS PENDIENTES (Criterios 11, 12 y 13) ---

    fun addSyncAction(action: String, recordId: Int, payload: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_SYNC_ACTION, action)
            put(KEY_SYNC_RECORD_ID, recordId)
            put(KEY_SYNC_PAYLOAD, payload)
        }
        return db.insert(TABLE_SYNC_QUEUE, null, values)
    }

    fun getPendingSyncActions(): List<com.example.myapplication.model.SyncAction> {
        val list = mutableListOf<com.example.myapplication.model.SyncAction>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SYNC_QUEUE ORDER BY $KEY_SYNC_ID ASC", null)
        if (cursor.moveToFirst()) {
            val idIdx = cursor.getColumnIndexOrThrow(KEY_SYNC_ID)
            val actionIdx = cursor.getColumnIndexOrThrow(KEY_SYNC_ACTION)
            val recordIdIdx = cursor.getColumnIndexOrThrow(KEY_SYNC_RECORD_ID)
            val payloadIdx = cursor.getColumnIndexOrThrow(KEY_SYNC_PAYLOAD)
            val tsIdx = cursor.getColumnIndex(KEY_SYNC_TIMESTAMP)
            do {
                val action = com.example.myapplication.model.SyncAction(
                    id = cursor.getInt(idIdx),
                    action = cursor.getString(actionIdx),
                    recordId = cursor.getInt(recordIdIdx),
                    payload = cursor.getString(payloadIdx),
                    timestamp = if (tsIdx != -1 && !cursor.isNull(tsIdx)) cursor.getString(tsIdx) else ""
                )
                list.add(action)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun deleteSyncAction(id: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_SYNC_QUEUE, "$KEY_SYNC_ID = ?", arrayOf(id.toString()))
    }

    fun getPendingSyncCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_SYNC_QUEUE", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }
}
