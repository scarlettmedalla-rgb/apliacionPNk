package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.db.DatabaseHelper
import com.example.myapplication.model.User
import com.example.myapplication.util.SweetAlertHelper
import com.example.myapplication.util.ValidationUtils
import com.google.android.material.button.MaterialButton
import java.io.File
import java.io.FileOutputStream

class ModifyUserActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var userId: Int = -1
    private var selectedPhotoPath: String? = null

    private lateinit var ivProfilePhoto: ImageView
    private lateinit var tvProfileName: TextView
    private lateinit var etFn: EditText
    private lateinit var etLn: EditText
    private lateinit var etEm: EditText

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val savedPath = saveImageToInternalStorage(uri)
            if (savedPath != null) {
                selectedPhotoPath = savedPath
                com.example.myapplication.util.ImageHelper.loadProfilePhoto(ivProfilePhoto, savedPath)
                SweetAlertHelper.showSuccess(this, "Vista Previa", "Imagen seleccionada correctamente. Presiona Guardar Cambios para confirmarla.")
            } else {
                SweetAlertHelper.showError(this, "Error", "No se pudo procesar la imagen seleccionada.")
            }
        }
    }

    override fun dispatchTouchEvent(ev: android.view.MotionEvent): Boolean {
        if (com.example.myapplication.util.SwipeBackHelper.processDispatchTouchEvent(this, ev)) {
            return true
        }
        return super.dispatchTouchEvent(ev)
    }

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

        val btnBack = findViewById<ImageButton>(R.id.btn_profile_back)
        btnBack?.setOnClickListener { finish() }

        userId = intent.getIntExtra("USER_ID", -1)

        ivProfilePhoto = findViewById(R.id.iv_profile_photo)
        tvProfileName = findViewById(R.id.tv_profile_name)
        etFn = findViewById(R.id.et_mod_firstname)
        etLn = findViewById(R.id.et_mod_lastname)
        etEm = findViewById(R.id.et_mod_email)

        val btnChangePhoto = findViewById<MaterialButton>(R.id.btn_change_photo)
        val btnModificar = findViewById<MaterialButton>(R.id.btn_mod_modificar)
        val btnEliminar = findViewById<MaterialButton>(R.id.btn_mod_eliminar)

        // Modo solo lectura (visualizar perfil)
        btnChangePhoto?.visibility = android.view.View.GONE
        btnModificar?.visibility = android.view.View.GONE
        btnEliminar?.visibility = android.view.View.GONE

        etFn.isEnabled = false
        etFn.isFocusable = false
        etFn.isClickable = false

        etLn.isEnabled = false
        etLn.isFocusable = false
        etLn.isClickable = false

        etEm.isEnabled = false
        etEm.isFocusable = false
        etEm.isClickable = false

        // Load existing user from database or Intent extras
        val userFromDb = if (userId != -1) dbHelper.getUserById(userId) else null
        val initialFn = userFromDb?.firstname ?: intent.getStringExtra("USER_FN") ?: ""
        val initialLn = userFromDb?.lastname ?: intent.getStringExtra("USER_LN") ?: ""
        val initialEmail = userFromDb?.email ?: intent.getStringExtra("USER_EMAIL") ?: ""
        selectedPhotoPath = userFromDb?.profilePhoto

        etFn.setText(initialFn)
        etLn.setText(initialLn)
        etEm.setText(initialEmail)

        val fullName = "$initialFn $initialLn".trim()
        tvProfileName.text = fullName.ifEmpty { "Usuario SmartTemp" }

        val defaultRes = when {
            initialEmail.contains("jorge", ignoreCase = true) || initialFn.contains("Jorge", ignoreCase = true) -> R.drawable.jorge_cortes
            initialEmail.contains("kevin", ignoreCase = true) || initialFn.contains("Kevin", ignoreCase = true) -> R.drawable.kevin_encina
            initialEmail.contains("scarlett", ignoreCase = true) || initialFn.contains("Scarlett", ignoreCase = true) || initialFn.contains("Scar", ignoreCase = true) -> R.drawable.scarlett_williams
            else -> R.drawable.ic_users
        }
        com.example.myapplication.util.ImageHelper.loadProfilePhoto(ivProfilePhoto, selectedPhotoPath, defaultRes)

        btnChangePhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

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
                password = "",
                profilePhoto = selectedPhotoPath
            )

            dbHelper.updateUser(updatedUser)

            SweetAlertHelper.showSuccess(
                context = this,
                title = "¡Perfil Actualizado!",
                message = "Los datos y fotografía de perfil han sido guardados con éxito."
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



    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val dir = File(filesDir, "profile_photos")
            if (!dir.exists()) dir.mkdirs()
            val fileName = "user_photo_${userId}_${System.currentTimeMillis()}.jpg"
            val destFile = File(dir, fileName)
            val outputStream = FileOutputStream(destFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
