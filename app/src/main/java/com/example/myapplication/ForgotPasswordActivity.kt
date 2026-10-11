package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.db.DatabaseHelper
import com.example.myapplication.network.AwsApiService
import com.example.myapplication.util.NetworkHelper
import com.example.myapplication.util.SweetAlertHelper
import com.example.myapplication.util.ValidationUtils
import com.google.android.material.button.MaterialButton
import java.util.Locale

/**
 * Rúbrica Punto 2: Recuperación de contraseña
 * - Envía un código aleatorio mediante PHPMailer / API.
 * - Muestra la cuenta regresiva de un minuto (60 segundos).
 * - Rechaza códigos incorrectos o vencidos.
 * - Permite solicitar otro código tras expirar el tiempo.
 * - Bloquea la cuenta durante la recuperación y la reactiva después de guardar la nueva contraseña.
 */
class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var countDownTimer: CountDownTimer? = null
    private var localGeneratedCode: String? = null
    private var isCodeExpired: Boolean = false
    private var activeEmail: String = ""

    companion object {
        private const val PREFS_NAME = "forgot_pass_prefs"
        private const val KEY_RECOVERY_EMAIL = "recovery_email"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("SAVED_ACTIVE_EMAIL", activeEmail)
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
        setContentView(R.layout.activity_forgot_password)

        dbHelper = DatabaseHelper(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.forgot_scroll)) { v, insets ->
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

        val etEmail = findViewById<EditText>(R.id.et_forgot_email)
        val btnRecuperar = findViewById<MaterialButton>(R.id.btn_forgot_recuperar)

        val etCode1 = findViewById<EditText>(R.id.et_code_1)
        val etCode2 = findViewById<EditText>(R.id.et_code_2)
        val etCode3 = findViewById<EditText>(R.id.et_code_3)
        val etCode4 = findViewById<EditText>(R.id.et_code_4)
        val etCode5 = findViewById<EditText>(R.id.et_code_5)
        val etCode6 = findViewById<EditText>(R.id.et_code_6)
        val tvTimer = findViewById<TextView>(R.id.tv_timer)
        val btnVerificar = findViewById<MaterialButton>(R.id.btn_verificar_codigo)
        val btnReenviar = findViewById<TextView>(R.id.btn_reenviar_codigo)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        activeEmail = savedInstanceState?.getString("SAVED_ACTIVE_EMAIL") ?: prefs.getString(KEY_RECOVERY_EMAIL, "") ?: ""
        if (activeEmail.isNotEmpty() && etEmail.text.isNullOrEmpty()) {
            etEmail.setText(activeEmail)
        }

        setupAutoAdvance(etCode1, etCode2)
        setupAutoAdvance(etCode2, etCode3)
        setupAutoAdvance(etCode3, etCode4)
        setupAutoAdvance(etCode4, etCode5)
        setupAutoAdvance(etCode5, etCode6)

        // Acción para solicitar código inicial con bloqueo anti-rebote (Debounce)
        btnRecuperar.setOnClickListener {
            val email = etEmail.text.toString().trim().lowercase(Locale.getDefault())
            if (!validarEmail(email)) return@setOnClickListener
            
            // Persistir de inmediato el estado del correo
            activeEmail = email
            prefs.edit().putString(KEY_RECOVERY_EMAIL, email).apply()

            // Bloqueo inmediato del botón para evitar múltiples peticiones y sobreescrituras en BD
            btnRecuperar.isEnabled = false
            btnRecuperar.alpha = 0.6f
            
            solicitarCodigo(email, tvTimer, btnReenviar, btnRecuperar, etCode1, etCode2, etCode3, etCode4, etCode5, etCode6)
        }

        // Acción para reenviar otro código
        btnReenviar.setOnClickListener {
            if (!isCodeExpired && localGeneratedCode != null) {
                SweetAlertHelper.showError(this, "Espere", "El código actual sigue vigente. Espere que termine la cuenta regresiva.")
                return@setOnClickListener
            }
            val email = etEmail.text.toString().trim().lowercase(Locale.getDefault())
            if (!validarEmail(email)) return@setOnClickListener
            
            activeEmail = email
            prefs.edit().putString(KEY_RECOVERY_EMAIL, email).apply()

            btnReenviar.isEnabled = false
            solicitarCodigo(email, tvTimer, btnReenviar, btnRecuperar, etCode1, etCode2, etCode3, etCode4, etCode5, etCode6)
        }

        // Acción para verificar los 6 dígitos
        btnVerificar.setOnClickListener {
            // Recuperación con triple respaldo de persistencia: variable -> campo de texto -> SharedPreferences
            val email = activeEmail
                .ifEmpty { etEmail.text.toString().trim() }
                .ifEmpty { prefs.getString(KEY_RECOVERY_EMAIL, "") ?: "" }
                .trim()
                .lowercase(Locale.getDefault())

            if (email.isEmpty()) {
                SweetAlertHelper.showError(this, "Atención", "Primero debe ingresar su correo y solicitar un código de verificación.")
                return@setOnClickListener
            }

            if (isCodeExpired) {
                SweetAlertHelper.showError(
                    this,
                    "Código Vencido",
                    "El código de verificación ha vencido (superó 1 minuto). Por favor solicite un nuevo código."
                )
                return@setOnClickListener
            }

            // Regla 1: Concatenación estricta de los 6 inputs en un String continuo sin espacios ni separadores
            val fullCode = listOf(etCode1, etCode2, etCode3, etCode4, etCode5, etCode6)
                .joinToString("") { it.text?.toString()?.trim() ?: "" }

            if (fullCode.length < 6) {
                SweetAlertHelper.showError(this, "Código Incompleto", "Por favor ingrese los 6 dígitos del código de verificación.")
                return@setOnClickListener
            }

            // Log de depuración solicitado en terminal / Logcat
            android.util.Log.d("FORGOT_PASSWORD", "PAYLOAD ENVIADO: { email: \"$email\", code: \"$fullCode\" }")
            println("PAYLOAD ENVIADO: { email: \"$email\", code: \"$fullCode\" }")

            // Desactivar temporalmente botón de verificar durante la petición
            btnVerificar.isEnabled = false

            // Regla 2: Coerción y validación con el servidor (o local si está offline)
            if (NetworkHelper.isOnline(this)) {
                AwsApiService.validarCodigo(
                    context = this,
                    email = email,
                    code = fullCode,
                    onSuccess = {
                        btnVerificar.isEnabled = true
                        avanzarAReset(email)
                    },
                    onExpired = { msg ->
                        btnVerificar.isEnabled = true
                        isCodeExpired = true
                        SweetAlertHelper.showError(this, "Código Vencido", msg)
                    },
                    onError = { errorMsg ->
                        btnVerificar.isEnabled = true
                        // Si falla en backend, comprobar contra local si coincide
                        if (localGeneratedCode != null && fullCode == localGeneratedCode?.trim()) {
                            avanzarAReset(email)
                        } else {
                            SweetAlertHelper.showError(this, "Código Incorrecto", errorMsg.ifEmpty { "El código ingresado no coincide con el enviado a su correo." })
                        }
                    }
                )
            } else {
                btnVerificar.isEnabled = true
                if (localGeneratedCode != null && fullCode == localGeneratedCode?.trim()) {
                    avanzarAReset(email)
                } else {
                    SweetAlertHelper.showError(this, "Código Incorrecto", "El código ingresado no coincide con el generado.")
                }
            }
        }
    }

    private fun validarEmail(email: String): Boolean {
        if (email.isEmpty()) {
            SweetAlertHelper.showError(this, "Campo Obligatorio", "Por favor ingrese su correo electrónico.")
            return false
        }
        if (!ValidationUtils.isValidEmail(email)) {
            SweetAlertHelper.showError(this, "Formato Inválido", "Por favor ingrese un correo válido.")
            return false
        }
        return true
    }

    private fun solicitarCodigo(
        email: String,
        tvTimer: TextView,
        btnReenviar: TextView,
        btnRecuperar: MaterialButton,
        c1: EditText, c2: EditText, c3: EditText, c4: EditText, c5: EditText, c6: EditText
    ) {
        activeEmail = email
        // Generar código aleatorio de 6 dígitos
        val randomCode = (100000..999999).random().toString()
        localGeneratedCode = randomCode
        isCodeExpired = false

        // Bloquear cuenta localmente en SQLite (Rúbrica: "bloquea la cuenta durante la recuperación")
        dbHelper.blockUserByEmail(email)

        // Limpiar casillas de código
        c1.text.clear()
        c2.text.clear()
        c3.text.clear()
        c4.text.clear()
        c5.text.clear()
        c6.text.clear()

        // Iniciar temporizador de 1 minuto (60 segundos)
        startTimer(tvTimer, btnReenviar, btnRecuperar)

        if (NetworkHelper.isOnline(this)) {
            AwsApiService.enviarCodigoRecuperacion(
                context = this,
                email = email,
                onSuccess = { previewCode ->
                    if (previewCode.isNotEmpty()) {
                        localGeneratedCode = previewCode
                    }
                    SweetAlertHelper.showSuccess(
                        context = this,
                        title = "Código Generado",
                        message = "Código enviado vía PHPMailer. Cuenta bloqueada temporalmente por seguridad. Dispone de 1 minuto."
                    )
                },
                onError = { error ->
                    SweetAlertHelper.showSuccess(
                        context = this,
                        title = "Código Generado (Local)",
                        message = "Código: $randomCode. Su cuenta ha sido bloqueada temporalmente por seguridad (1 minuto)."
                    )
                }
            )
        } else {
            SweetAlertHelper.showSuccess(
                context = this,
                title = "Código Offline Generado",
                message = "Código: $randomCode. Cuenta bloqueada localmente por seguridad. Dispone de 1 minuto."
            )
        }
    }

    private fun avanzarAReset(email: String) {
        countDownTimer?.cancel()
        SweetAlertHelper.showSuccess(
            context = this,
            title = "Código Verificado",
            message = "Código válido. A continuación ingrese su nueva contraseña para reactivar la cuenta."
        ) {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            intent.putExtra("USER_EMAIL", email)
            startActivity(intent)
            finish()
        }
    }

    private fun startTimer(tvTimer: TextView, btnReenviar: TextView, btnRecuperar: MaterialButton) {
        countDownTimer?.cancel()
        btnReenviar.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        btnReenviar.isClickable = false
        btnReenviar.isEnabled = false

        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                tvTimer.text = "$seconds Segundos restantes"
                tvTimer.setTextColor(ContextCompat.getColor(this@ForgotPasswordActivity, R.color.red_primary))
            }

            override fun onFinish() {
                isCodeExpired = true
                tvTimer.text = "0 Segundos (Código Vencido)"
                tvTimer.setTextColor(ContextCompat.getColor(this@ForgotPasswordActivity, R.color.text_secondary))
                
                // Habilitar opción de solicitar otro código y desbloquear botón principal
                btnReenviar.setTextColor(ContextCompat.getColor(this@ForgotPasswordActivity, R.color.pink_primary))
                btnReenviar.isClickable = true
                btnReenviar.isEnabled = true
                btnRecuperar.isEnabled = true
                btnRecuperar.alpha = 1.0f

                SweetAlertHelper.showError(
                    this@ForgotPasswordActivity,
                    "Tiempo Expirado",
                    "El código de verificación ha vencido. Puede solicitar otro código presionando 'Solicitar otro'."
                )
            }
        }.start()
    }

    private fun setupAutoAdvance(current: EditText, next: EditText) {
        current.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 1) {
                    next.requestFocus()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
