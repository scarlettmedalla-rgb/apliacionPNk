package com.example.myapplication.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
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
        private const val DATABASE_NAME = "iot_app_db_v2.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_USERS = "users"
        private const val KEY_USER_ID = "id"
        private const val KEY_USER_FIRSTNAME = "firstname"
        private const val KEY_USER_LASTNAME = "lastname"
        private const val KEY_USER_EMAIL = "email"
        private const val KEY_USER_PASSWORD = "password"

        private const val TABLE_SENSORS = "sensors"
        private const val KEY_SENSOR_ID = "id"
        private const val KEY_SENSOR_NAME = "name"
        private const val KEY_SENSOR_LOCATION = "location"
        private const val KEY_SENSOR_TEMP = "temperature"
        private const val KEY_SENSOR_HUMIDITY = "humidity"
        private const val KEY_SENSOR_DATE = "date"
        private const val KEY_SENSOR_THERM_ON = "is_thermometer_on"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = ("CREATE TABLE " + TABLE_USERS + "("
                + KEY_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USER_FIRSTNAME + " TEXT,"
                + KEY_USER_LASTNAME + " TEXT,"
                + KEY_USER_EMAIL + " TEXT UNIQUE,"
                + KEY_USER_PASSWORD + " TEXT" + ")")

        val createSensorsTable = ("CREATE TABLE " + TABLE_SENSORS + "("
                + KEY_SENSOR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_SENSOR_NAME + " TEXT,"
                + KEY_SENSOR_LOCATION + " TEXT,"
                + KEY_SENSOR_TEMP + " TEXT,"
                + KEY_SENSOR_HUMIDITY + " TEXT,"
                + KEY_SENSOR_DATE + " TEXT,"
                + KEY_SENSOR_THERM_ON + " INTEGER" + ")")

        db.execSQL(createUsersTable)
        db.execSQL(createSensorsTable)
        insertInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SENSORS")
        onCreate(db)
    }

    private fun currentDateString(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun insertInitialData(db: SQLiteDatabase) {
        val seedUsers = listOf(
            User(1, "", "Jorge Luis", "Cortes Gallardo", "jorge.cortes@empresa.com", "", "Clave123!"),
            User(2, "", "Kevin", "Encina Molina", "kevin.encina@empresa.com", "", "Clave123!"),
            User(3, "", "Scarlett", "Williams Medalla", "scarlett.williams@empresa.com", "", "Clave123!")
        )

        for (u in seedUsers) {
            val cv = ContentValues().apply {
                put(KEY_USER_FIRSTNAME, u.firstname)
                put(KEY_USER_LASTNAME, u.lastname)
                put(KEY_USER_EMAIL, u.email)
                put(KEY_USER_PASSWORD, u.password)
            }
            db.insert(TABLE_USERS, null, cv)
        }

        val now = currentDateString()
        val seedSensors = listOf(
            Triple("Sensor Clima Ovalle Central", "Ovalle", "23.5"),
            Triple("Sensor Clima Coquimbo Costa", "Coquimbo", "17.8"),
            Triple("Sensor Clima La Serena Centro", "La Serena", "21.2"),
            Triple("Sensor Clima Tierras Blancas", "Tierras Blancas", "19.4"),
            Triple("Sensor Clima Vicuña Elqui", "Vicuña", "22.0")
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

    fun insertUser(user: User): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_USER_FIRSTNAME, user.firstname)
            put(KEY_USER_LASTNAME, user.lastname)
            put(KEY_USER_EMAIL, user.email.lowercase(Locale.getDefault()).trim())
            put(KEY_USER_PASSWORD, user.password)
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

            do {
                val fn = cursor.getString(fnIdx) ?: ""
                val ln = cursor.getString(lnIdx) ?: ""
                val email = cursor.getString(emailIdx) ?: ""

                val fullTextNormalized = normalizeString("$fn $ln $email")

                if (normalizedQuery.isEmpty() || fullTextNormalized.contains(normalizedQuery)) {
                    val user = User(
                        id = cursor.getInt(idIdx),
                        rut = "",
                        firstname = fn,
                        lastname = ln,
                        email = email,
                        phone = "",
                        password = cursor.getString(passIdx) ?: ""
                    )
                    userList.add(user)
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return userList
    }

    fun updateUser(user: User): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_USER_FIRSTNAME, user.firstname)
            put(KEY_USER_LASTNAME, user.lastname)
            put(KEY_USER_EMAIL, user.email.lowercase(Locale.getDefault()).trim())
        }
        return db.update(TABLE_USERS, values, "$KEY_USER_ID = ?", arrayOf(user.id.toString()))
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
        return sensorList
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
            val tempOffset = when (index % 5) {
                0 -> 0.0
                1 -> -2.3
                2 -> 1.8
                3 -> -1.2
                else -> 0.7
            }
            val humOffset = when (index % 5) {
                0 -> 0.0
                1 -> 5.0
                2 -> -4.0
                3 -> 3.0
                else -> -2.0
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
}
