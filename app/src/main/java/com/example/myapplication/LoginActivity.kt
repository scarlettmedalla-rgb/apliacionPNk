package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.myapplication.db.DatabaseHelper
import com.example.myapplication.network.AwsApiService
import com.example.myapplication.util.EdgeToEdgeHelper
import com.example.myapplication.util.NetworkHelper
import com.example.myapplication.util.SweetAlertHelper
import com.example.myapplication.util.ValidationUtils
import com.google.android.material.button.MaterialButton

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var isPasswordVisible = false
    private var progressDialog: SweetAlertDialog? = null

    private lateinit var layoutInitialView: ConstraintLayout
    private lateinit var layoutLoginForm: ConstraintLayout
    private lateinit var btnLogin: MaterialButton

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
        setContentView(R.layout.activity_login)

        dbHelper = DatabaseHelper(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_scroll)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                0
            )
            insets
        }

        layoutInitialView = findViewById(R.id.layout_initial_view)
        layoutLoginForm = findViewById(R.id.layout_login_form)

        val btnOptionLogin = findViewById<MaterialButton>(R.id.btn_option_login)
        val btnOptionRegister = findViewById<MaterialButton>(R.id.btn_option_register)

        val tvInitialTitle = findViewById<TextView>(R.id.tv_initial_title)
        val tvWelcomeTitle = findViewById<TextView>(R.id.tv_welcome_title)

        val etUsername = findViewById<EditText>(R.id.et_username)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val ivPasswordToggle = findViewById<ImageView>(R.id.iv_password_toggle)
        btnLogin = findViewById(R.id.btn_login)
        val tvForgot = findViewById<TextView>(R.id.tv_forgot_password)
        val tvFormRegisterPrompt = findViewById<TextView>(R.id.tv_form_register_prompt)
        val tvBackToOptions = findViewById<TextView>(R.id.tv_back_to_options)

        // Styled Initial Title: "¡Hola! ingresa a SmartTemp"
        val pinkColor = ContextCompat.getColor(this, R.color.pink_primary)
        val darkColor = ContextCompat.getColor(this, R.color.text_dark)

        val initialTitleBuilder = SpannableStringBuilder()
        val spanHola = SpannableString("¡Hola! ")
        spanHola.setSpan(ForegroundColorSpan(pinkColor), 0, spanHola.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanHola.setSpan(StyleSpan(Typeface.BOLD), 0, spanHola.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        val spanIngresa = SpannableString("ingresa a SmartTemp")
        spanIngresa.setSpan(ForegroundColorSpan(darkColor), 0, spanIngresa.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanIngresa.setSpan(StyleSpan(Typeface.BOLD), 0, spanIngresa.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        initialTitleBuilder.append(spanHola).append(spanIngresa)
        tvInitialTitle.text = initialTitleBuilder

        // Styled Form Title: "¡Hola!\nBienvenido"
        val formTitleBuilder = SpannableStringBuilder()
        val spanHello = SpannableString("¡Hola!\n")
        spanHello.setSpan(ForegroundColorSpan(pinkColor), 0, spanHello.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanHello.setSpan(StyleSpan(Typeface.BOLD), 0, spanHello.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        val spanWelcome = SpannableString("Bienvenido a SmartTemp")
        spanWelcome.setSpan(ForegroundColorSpan(darkColor), 0, spanWelcome.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanWelcome.setSpan(StyleSpan(Typeface.BOLD), 0, spanWelcome.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        formTitleBuilder.append(spanHello).append(spanWelcome)
        tvWelcomeTitle.text = formTitleBuilder

        val showForm = intent.getBooleanExtra("SHOW_LOGIN_FORM", false)
        if (showForm) {
            layoutInitialView.visibility = View.GONE
            layoutLoginForm.visibility = View.VISIBLE
        } else {
            layoutInitialView.visibility = View.VISIBLE
            layoutLoginForm.visibility = View.GONE
        }

        btnOptionLogin.setOnClickListener {
            layoutInitialView.visibility = View.GONE
            layoutLoginForm.visibility = View.VISIBLE
        }

        btnOptionRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        tvFormRegisterPrompt.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        tvBackToOptions.setOnClickListener {
            layoutLoginForm.visibility = View.GONE
            layoutInitialView.visibility = View.VISIBLE
        }

        ivPasswordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivPasswordToggle.setImageResource(R.drawable.ic_eye_off)
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivPasswordToggle.setImageResource(R.drawable.ic_eye)
            }
            etPassword.setSelection(etPassword.text.length)
        }

        btnLogin.setOnClickListener {
            val username = etUsername.text?.toString()?.trim() ?: ""
            val password = etPassword.text?.toString()?.trim() ?: ""

            if (username.isEmpty() || password.isEmpty()) {
                SweetAlertHelper.showError(
                    this,
                    "Campos Obligatorios",
                    "Por favor complete todos los campos en blanco."
                )
                return@setOnClickListener
            }

            if (ValidationUtils.containsScriptOrInjection(username) || ValidationUtils.containsScriptOrInjection(password)) {
                SweetAlertHelper.showError(
                    this,
                    "Seguridad - Entrada Inválida",
                    "Se detectó un intento de script o comando no permitido."
                )
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidUsernameOrEmail(username)) {
                SweetAlertHelper.showError(
                    this,
                    "Usuario o Email Inválido",
                    "Por favor ingrese un usuario o correo electrónico válido."
                )
                return@setOnClickListener
            }

            btnLogin.isEnabled = false

            val proceedToMain = {
                btnLogin.isEnabled = true
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

            if (NetworkHelper.isOnline(this)) {
                progressDialog = SweetAlertHelper.showProgress(this, "Iniciando sesión...")
                // Validación contra MySQL mediante la API en AWS (Rúbrica Punto 1)
                AwsApiService.iniciarSesion(
                    context = this,
                    usernameOrEmail = username,
                    pass = password,
                    onSuccess = { user ->
                        dismissProgress()
                        if (!isFinishing && !isDestroyed) {
                            dbHelper.upsertUserFromLogin(user, password)
                            saveUserSession(user.firstname.ifBlank { username }, username, isOffline = false)
                            btnLogin.isEnabled = true
                            SweetAlertHelper.showSuccess(
                                context = this,
                                title = "¡Bienvenido!",
                                message = "Sesión iniciada con éxito (${user.firstname.ifBlank { username }})."
                            ) {
                                proceedToMain()
                            }
                        }
                    },
                    onBlocked = { msg ->
                        dismissProgress()
                        if (!isFinishing && !isDestroyed) {
                            btnLogin.isEnabled = true
                            SweetAlertHelper.showError(
                                this,
                                "Cuenta Bloqueada",
                                msg
                            )
                        }
                    },
                    onError = { errorMsg ->
                        dismissProgress()
                        if (!isFinishing && !isDestroyed) {
                            // Si el usuario se registró localmente sin sincronizar aún, permitir acceso local
                            val localUser = dbHelper.findUserByIdentifier(username)
                            if (localUser != null && localUser.syncStatus == 0 && localUser.password == password) {
                                if (localUser.isBlocked) {
                                    btnLogin.isEnabled = true
                                    SweetAlertHelper.showError(this, "Cuenta Bloqueada", "Esta cuenta está bloqueada localmente.")
                                } else {
                                    saveUserSession(localUser.firstname.ifBlank { username }, username, isOffline = true)
                                    btnLogin.isEnabled = true
                                    SweetAlertHelper.showSuccess(
                                        context = this,
                                        title = "Modo Local",
                                        message = "Sesión iniciada con usuario registrado localmente (${localUser.firstname.ifBlank { username }})."
                                    ) {
                                        proceedToMain()
                                    }
                                }
                            } else {
                                btnLogin.isEnabled = true
                                SweetAlertHelper.showError(this, "Error de Inicio de Sesión", errorMsg)
                            }
                        }
                    },
                    onConnectionError = {
                        dismissProgress()
                        if (!isFinishing && !isDestroyed) {
                            // Sin conexión al servidor AWS: validar estrictamente contra SQLite local
                            loginWithLocalSqlite(username, password, proceedToMain)
                        }
                    }
                )
            } else {
                // Modo Offline (sin Internet / sin AWS): validar estrictamente contra SQLite local (Rúbrica Punto 9 y 10)
                loginWithLocalSqlite(username, password, proceedToMain)
            }
        }

        tvForgot.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginWithLocalSqlite(username: String, password: String, proceedToMain: () -> Unit) {
        val localUser = dbHelper.findUserByIdentifier(username)
        if (localUser == null) {
            btnLogin.isEnabled = true
            SweetAlertHelper.showError(
                this,
                "Acceso Denegado",
                "El usuario o correo ingresado no existe en la base de datos local (SQLite). Regístrate primero para poder ingresar."
            )
            return
        }

        if (localUser.isBlocked) {
            btnLogin.isEnabled = true
            SweetAlertHelper.showError(
                this,
                "Cuenta Bloqueada",
                "Esta cuenta está bloqueada localmente por seguridad."
            )
            return
        }

        if (localUser.password != password) {
            btnLogin.isEnabled = true
            SweetAlertHelper.showError(
                this,
                "Credenciales Incorrectas",
                "La contraseña ingresada no coincide con el usuario registrado en SQLite."
            )
            return
        }

        val displayName = localUser.firstname.ifBlank { username }
        saveUserSession(displayName, username, isOffline = true)
        btnLogin.isEnabled = true
        SweetAlertHelper.showSuccess(
            context = this,
            title = "Modo Sin Conexión (SQLite)",
            message = "Bienvenido, $displayName. Acceso validado con la base de datos local."
        ) {
            proceedToMain()
        }
    }

    private fun saveUserSession(displayName: String, identifier: String, isOffline: Boolean) {
        val userPrefs = getSharedPreferences("smarttemp_user_session", Context.MODE_PRIVATE)
        userPrefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_display_name", displayName)
            .putString("user_identifier", identifier)
            .putBoolean("is_offline_session", isOffline)
            .apply()
    }

    private fun dismissProgress() {
        try {
            progressDialog?.dismiss()
        } catch (_: Exception) {}
        progressDialog = null
    }

    override fun onResume() {
        super.onResume()
        if (::btnLogin.isInitialized) {
            btnLogin.isEnabled = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissProgress()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        val showForm = intent?.getBooleanExtra("SHOW_LOGIN_FORM", false) ?: false
        if (showForm) {
            layoutInitialView.visibility = View.GONE
            layoutLoginForm.visibility = View.VISIBLE
        }
    }
}
