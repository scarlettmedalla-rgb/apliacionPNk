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

            val newUser = User(
                rut = rut,
                firstname = fn,
                lastname = ln,
                email = email,
                phone = phone,
                password = "Clave123!"
            )

            val result = dbHelper.insertUser(newUser)
            if (result != -1L) {
                SweetAlertHelper.showSuccess(
                    context = this,
                    title = "¡Usuario Registrado!",
                    message = "El nuevo usuario $fn $ln ha sido agregado con éxito a la base de datos."
                ) {
                    finish()
                }
            } else {
                SweetAlertHelper.showError(this, "Error de Inserción", "No se pudo guardar el usuario en la base de datos.")
            }
        }
    }
}
