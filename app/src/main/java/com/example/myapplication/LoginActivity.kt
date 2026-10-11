package com.example.myapplication

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
import com.example.myapplication.db.DatabaseHelper
import com.example.myapplication.util.EdgeToEdgeHelper
import com.example.myapplication.util.SweetAlertHelper
import com.example.myapplication.util.ValidationUtils
import com.google.android.material.button.MaterialButton

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var isPasswordVisible = false

    private lateinit var layoutInitialView: ConstraintLayout
    private lateinit var layoutLoginForm: ConstraintLayout

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
        val btnLogin = findViewById<MaterialButton>(R.id.btn_login)
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

            if (com.example.myapplication.util.NetworkHelper.isOnline(this)) {
                // Validación contra MySQL mediante la API en AWS (Rúbrica Punto 1)
                com.example.myapplication.network.AwsApiService.iniciarSesion(
                    context = this,
                    usernameOrEmail = username,
                    pass = password,
                    onSuccess = { user ->
                        SweetAlertHelper.showSuccess(
                            context = this,
                            title = "¡Bienvenido!",
                            message = "Sesión iniciada con éxito (${user.firstname})."
                        ) {
                            proceedToMain()
                        }
                    },
                    onBlocked = { msg ->
                        btnLogin.isEnabled = true
                        SweetAlertHelper.showError(
                            this,
                            "Cuenta Bloqueada",
                            msg
                        )
                    },
                    onError = { errorMsg ->
                        // Fallback a SQLite local si el servidor no responde o está en modo local
                        val (ok, blocked, localUser) = dbHelper.validateLocalLogin(username, password)
                        if (ok) {
                            if (blocked) {
                                btnLogin.isEnabled = true
                                SweetAlertHelper.showError(this, "Cuenta Bloqueada", "Esta cuenta está bloqueada localmente.")
                            } else {
                                SweetAlertHelper.showSuccess(
                                    context = this,
                                    title = "Modo Offline",
                                    message = "Inicio de sesión validado con la base de datos local."
                                ) {
                                    proceedToMain()
                                }
                            }
                        } else {
                            btnLogin.isEnabled = true
                            SweetAlertHelper.showError(this, "Error de Inicio de Sesión", errorMsg)
                        }
                    }
                )
            } else {
                // Modo Offline: Validación directa en SQLite (Rúbrica Punto 9 y 10)
                val (ok, blocked, localUser) = dbHelper.validateLocalLogin(username, password)
                if (ok) {
                    if (blocked) {
                        btnLogin.isEnabled = true
                        SweetAlertHelper.showError(this, "Cuenta Bloqueada", "Esta cuenta está bloqueada localmente por seguridad.")
                    } else {
                        SweetAlertHelper.showSuccess(
                            context = this,
                            title = "Modo Sin Conexión",
                            message = "Acceso concedido mediante almacenamiento local SQLite."
                        ) {
                            proceedToMain()
                        }
                    }
                } else {
                    btnLogin.isEnabled = true
                    SweetAlertHelper.showError(this, "Credenciales Incorrectas", "Usuario/correo o contraseña inválidos en la base local.")
                }
            }
        }

        tvForgot.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
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
