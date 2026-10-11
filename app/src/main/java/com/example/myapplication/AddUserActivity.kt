package com.example.myapplication

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.db.DatabaseHelper
import com.example.myapplication.model.User
import com.example.myapplication.util.EdgeToEdgeHelper
import com.example.myapplication.util.SweetAlertHelper
import com.example.myapplication.util.ValidationUtils
import com.google.android.material.button.MaterialButton

class AddUserActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun dispatchTouchEvent(ev: android.view.MotionEvent): Boolean {
        if (com.example.myapplication.util.SwipeBackHelper.processDispatchTouchEvent(this, ev)) {
            return true
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        EdgeToEdgeHelper.applyTransparentSystemBars(this, isLightStatusBar = true, isLightNavBar = false)
        setContentView(R.layout.activity_add_user)

        dbHelper = DatabaseHelper(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_user_scroll)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val btnBack = findViewById<ImageButton>(R.id.btn_add_user_back)
        btnBack.setOnClickListener { finish() }

        val etRut = findViewById<EditText>(R.id.et_user_rut)
        val etFn = findViewById<EditText>(R.id.et_user_fn)
        val etLn = findViewById<EditText>(R.id.et_user_ln)
        val etEmail = findViewById<EditText>(R.id.et_user_email)
        val etPhone = findViewById<EditText>(R.id.et_user_phone)
        val btnSave = findViewById<MaterialButton>(R.id.btn_save_user)

        btnSave.setOnClickListener {
            val rut = etRut.text.toString().trim()
            val fn = etFn.text.toString().trim()
            val ln = etLn.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            if (rut.isEmpty() || fn.isEmpty() || ln.isEmpty() || email.isEmpty()) {
                SweetAlertHelper.showError(this, "Campos Obligatorios", "Por favor completa el RUT, Nombres, Apellidos y Correo.")
                return@setOnClickListener
            }

            if (ValidationUtils.containsScriptOrInjection(rut) ||
                ValidationUtils.containsScriptOrInjection(fn) ||
                ValidationUtils.containsScriptOrInjection(ln) ||
                ValidationUtils.containsScriptOrInjection(email) ||
                ValidationUtils.containsScriptOrInjection(phone)
            ) {
                SweetAlertHelper.showError(this, "Ataque Detectado", "No se permiten scripts, comandos ni caracteres peligrosos en las celdas.")
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidName(fn)) {
                SweetAlertHelper.showError(this, "Nombre Inválido", "El nombre solo debe contener letras. No se permiten números (como 12345) ni símbolos.")
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidName(ln)) {
                SweetAlertHelper.showError(this, "Apellido Inválido", "El apellido solo debe contener letras. No se permiten números (como 12345) ni símbolos.")
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidEmail(email)) {
                SweetAlertHelper.showError(this, "Correo Inválido", "Ingresa un correo electrónico válido.")
                return@setOnClickListener
            }

            if (dbHelper.isEmailExists(email)) {
                SweetAlertHelper.showError(this, "Correo Ya Registrado", "El correo ingresado ya existe en la base de datos.")
                return@setOnClickListener
            }

            val fnClean = fn.lowercase(java.util.Locale.getDefault()).replace("\\s+".toRegex(), "")
            val lnClean = ln.lowercase(java.util.Locale.getDefault()).replace("\\s+".toRegex(), "")
            val generatedUsername = "$fnClean.$lnClean"

            if (dbHelper.isUsernameExists(generatedUsername)) {
                SweetAlertHelper.showError(this, "Usuario Duplicado", "Ya existe un usuario registrado con el identificador $generatedUsername.")
                return@setOnClickListener
            }

            val newUser = User(
                rut = rut,
                firstname = fn,
                lastname = ln,
                email = email,
                phone = phone,
                password = "Clave123!",
                username = generatedUsername
            )

            btnSave.isEnabled = false

            if (com.example.myapplication.util.NetworkHelper.isOnline(this)) {
                // Registrar en MySQL en AWS y replicar en SQLite local
                com.example.myapplication.network.AwsApiService.guardarUsuario(
                    context = this,
                    user = newUser,
                    onSuccess = { msg ->
                        dbHelper.insertUser(newUser, syncStatus = 1)
                        btnSave.isEnabled = true
                        SweetAlertHelper.showSuccess(
                            context = this,
                            title = "¡Usuario Registrado!",
                            message = "Usuario $fn $ln registrado y sincronizado en AWS."
                        ) {
                            finish()
                        }
                    },
                    onError = { errorMsg ->
                        if (errorMsg.contains("duplicado", ignoreCase = true) || errorMsg.contains("existe", ignoreCase = true)) {
                            btnSave.isEnabled = true
                            SweetAlertHelper.showError(this, "Error de Duplicidad", errorMsg)
                        } else {
                            // Si falla la red de AWS, guardar localmente y encolar cambio pendiente (Criterio 11)
                            val localId = dbHelper.insertUser(newUser, syncStatus = 0)
                            val userWithId = newUser.copy(id = localId.toInt(), syncStatus = 0)
                            dbHelper.addSyncAction("INSERT", localId.toInt(), com.example.myapplication.util.SyncManager.userToJson(userWithId))
                            btnSave.isEnabled = true
                            SweetAlertHelper.showSuccess(
                                context = this,
                                title = "Guardado Localmente",
                                message = "Sin conexión a AWS. Guardado en SQLite y encolado para sincronización automática."
                            ) {
                                finish()
                            }
                        }
                    }
                )
            } else {
                // Modo Offline: Operación CRUD local en SQLite y encolado pendiente (Criterio 10 y 11)
                val localId = dbHelper.insertUser(newUser, syncStatus = 0)
                val userWithId = newUser.copy(id = localId.toInt(), syncStatus = 0)
                dbHelper.addSyncAction("INSERT", localId.toInt(), com.example.myapplication.util.SyncManager.userToJson(userWithId))
                btnSave.isEnabled = true
                SweetAlertHelper.showSuccess(
                    context = this,
                    title = "Modo Sin Conexión",
                    message = "Usuario registrado en SQLite. Cambio encolado para sincronizar con AWS al recuperar la red."
                ) {
                    finish()
                }
            }
        }
    }
}
