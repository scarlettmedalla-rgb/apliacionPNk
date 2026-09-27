package com.example.myapplication

import android.os.Bundle
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.db.DatabaseHelper
import com.example.myapplication.model.User
import com.example.myapplication.util.SweetAlertHelper
import com.example.myapplication.util.ValidationUtils
import com.google.android.material.button.MaterialButton

class ModifyUserActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_modify_user)

        dbHelper = DatabaseHelper(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.modify_scroll)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val extraBottomPadding = (40 * resources.displayMetrics.density).toInt()
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom + extraBottomPadding
            )
            insets
        }

        userId = intent.getIntExtra("USER_ID", -1)
        val initialFn = intent.getStringExtra("USER_FN") ?: ""
        val initialLn = intent.getStringExtra("USER_LN") ?: ""
        val initialEmail = intent.getStringExtra("USER_EMAIL") ?: ""

        val etFn = findViewById<EditText>(R.id.et_mod_firstname)
        val etLn = findViewById<EditText>(R.id.et_mod_lastname)
        val etEm = findViewById<EditText>(R.id.et_mod_email)

        etFn.setText(initialFn)
        etLn.setText(initialLn)
        etEm.setText(initialEmail)

        val btnModificar = findViewById<MaterialButton>(R.id.btn_mod_modificar)
        val btnEliminar = findViewById<MaterialButton>(R.id.btn_mod_eliminar)

        btnModificar.setOnClickListener {
            val fn = etFn.text.toString().trim()
            val ln = etLn.text.toString().trim()
            val email = etEm.text.toString().trim()

            if (fn.isEmpty() || ln.isEmpty() || email.isEmpty()) {
                SweetAlertHelper.showError(
                    this,
                    "Campos Obligatorios",
                    "Todos los campos son requeridos."
                )
                return@setOnClickListener
            }

            if (ValidationUtils.containsScriptOrInjection(fn) ||
                ValidationUtils.containsScriptOrInjection(ln) ||
                ValidationUtils.containsScriptOrInjection(email)
            ) {
                SweetAlertHelper.showError(
                    this,
                    "Seguridad",
                    "No se permiten comandos, scripts ni caracteres especiales en las celdas."
                )
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidName(fn)) {
                SweetAlertHelper.showError(
                    this,
                    "Nombre Inválido",
                    "El nombre solo debe contener letras. No se permiten números (como 12345) ni símbolos."
                )
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidName(ln)) {
                SweetAlertHelper.showError(
                    this,
                    "Apellido Inválido",
                    "El apellido solo debe contener letras. No se permiten números (como 12345) ni símbolos."
                )
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidEmail(email)) {
                SweetAlertHelper.showError(
                    this,
                    "Email Inválido",
                    "Por favor ingrese un correo electrónico válido."
                )
                return@setOnClickListener
            }

            val updatedUser = User(
                id = userId,
                rut = "",
                firstname = fn,
                lastname = ln,
                email = email,
                phone = "",
                password = ""
            )

            dbHelper.updateUser(updatedUser)

            SweetAlertHelper.showSuccess(
                context = this,
                title = "¡Usuario Modificado!",
                message = "Los datos del usuario han sido actualizados con éxito."
            ) {
                finish()
            }
        }

        btnEliminar.setOnClickListener {
            SweetAlertHelper.showConfirmation(
                context = this,
                title = "¿Eliminar Usuario?",
                message = "¿Estás seguro de que deseas eliminar permanentemente a $initialFn $initialLn?",
                confirmText = "Sí, Eliminar",
                cancelText = "Cancelar",
                onConfirm = {
                    dbHelper.deleteUser(userId)
                    SweetAlertHelper.showSuccess(
                        context = this,
                        title = "¡Eliminado!",
                        message = "El usuario ha sido eliminado correctamente del sistema."
                    ) {
                        finish()
                    }
                }
            )
        }
    }
}
