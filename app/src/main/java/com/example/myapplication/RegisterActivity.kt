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
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.db.DatabaseHelper
import com.example.myapplication.model.User
import com.example.myapplication.util.EdgeToEdgeHelper
import com.example.myapplication.util.SweetAlertHelper
import com.example.myapplication.util.ValidationUtils
import com.google.android.material.button.MaterialButton

class RegisterActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var isPass1Visible = false
    private var isPass2Visible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        EdgeToEdgeHelper.applyTransparentSystemBars(this, isLightStatusBar = true, isLightNavBar = false)
        setContentView(R.layout.activity_register)

        dbHelper = DatabaseHelper(this)

        val scrollContainer = findViewById<ScrollView>(R.id.register_scroll)

        // Dynamic padding for system bars & soft keyboard (IME)
        ViewCompat.setOnApplyWindowInsetsListener(scrollContainer) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val density = resources.displayMetrics.density

            val bottomPadding = if (ime.bottom > 0) {
                ime.bottom + (20 * density).toInt()
            } else {
                0
            }

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                bottomPadding
            )
            insets
        }

        // Tap outside an input field to dismiss the soft keyboard
        val mainView = findViewById<View>(R.id.register_main)
        mainView?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                val currentFocused = currentFocus
                if (currentFocused != null) {
                    imm?.hideSoftInputFromWindow(currentFocused.windowToken, 0)
                    currentFocused.clearFocus()
                }
            }
            false
        }

        val tvRegisterTitle = findViewById<TextView>(R.id.tv_register_title)
        val etFirstName = findViewById<EditText>(R.id.et_reg_firstname)
        val etLastName = findViewById<EditText>(R.id.et_reg_lastname)
        val etEmail = findViewById<EditText>(R.id.et_reg_email)
        val etPass1 = findViewById<EditText>(R.id.et_reg_pass1)
        val etPass2 = findViewById<EditText>(R.id.et_reg_pass2)
        val ivPass1Toggle = findViewById<ImageView>(R.id.iv_reg_pass1_toggle)
        val ivPass2Toggle = findViewById<ImageView>(R.id.iv_reg_pass2_toggle)
        val btnRegistrar = findViewById<MaterialButton>(R.id.btn_reg_registrar)
        val tvAlreadyAccount = findViewById<TextView>(R.id.tv_already_account)
        findViewById<MaterialButton>(R.id.btn_easter_egg)?.setOnClickListener {
            startActivity(Intent(this, EasterEggActivity::class.java))
        }

        // Auto-scroll focused input box smoothly above the soft keyboard
        val focusAutoScrollListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                scrollContainer?.postDelayed({
                    val parentContainer = v.parent as? View ?: v
                    val scrollY = parentContainer.top - (60 * resources.displayMetrics.density).toInt()
                    scrollContainer.smoothScrollTo(0, maxOf(0, scrollY))
                }, 200)
            }
        }
        etFirstName.onFocusChangeListener = focusAutoScrollListener
        etLastName.onFocusChangeListener = focusAutoScrollListener
        etEmail.onFocusChangeListener = focusAutoScrollListener
        etPass1.onFocusChangeListener = focusAutoScrollListener
        etPass2.onFocusChangeListener = focusAutoScrollListener

        // Single-line harmonious centered title: "Crear una " (pink) + "Cuenta" (dark)
        val pinkColor = ContextCompat.getColor(this, R.color.pink_primary)
        val darkColor = ContextCompat.getColor(this, R.color.text_dark)

        val titleBuilder = SpannableStringBuilder()
        val spanCrear = SpannableString("Crear una ")
        spanCrear.setSpan(ForegroundColorSpan(pinkColor), 0, spanCrear.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanCrear.setSpan(StyleSpan(Typeface.BOLD), 0, spanCrear.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        val spanCuenta = SpannableString("Cuenta")
        spanCuenta.setSpan(ForegroundColorSpan(darkColor), 0, spanCuenta.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanCuenta.setSpan(StyleSpan(Typeface.BOLD), 0, spanCuenta.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        titleBuilder.append(spanCrear).append(spanCuenta)
        tvRegisterTitle?.text = titleBuilder

        // Password 1 Toggle
        ivPass1Toggle?.setOnClickListener {
            isPass1Visible = !isPass1Visible
            if (isPass1Visible) {
                etPass1.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivPass1Toggle.setImageResource(R.drawable.ic_eye_off)
            } else {
                etPass1.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivPass1Toggle.setImageResource(R.drawable.ic_eye)
            }
            etPass1.setSelection(etPass1.text.length)
        }

        // Password 2 Toggle
        ivPass2Toggle?.setOnClickListener {
            isPass2Visible = !isPass2Visible
            if (isPass2Visible) {
                etPass2.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivPass2Toggle.setImageResource(R.drawable.ic_eye_off)
            } else {
                etPass2.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivPass2Toggle.setImageResource(R.drawable.ic_eye)
            }
            etPass2.setSelection(etPass2.text.length)
        }

        // When tapping "¿Ya tienes una cuenta? Iniciar Sesión", open Login Activity in Form mode!
        tvAlreadyAccount?.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("SHOW_LOGIN_FORM", true)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        btnRegistrar.setOnClickListener {
            val fn = etFirstName.text.toString().trim()
            val ln = etLastName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val p1 = etPass1.text.toString().trim()
            val p2 = etPass2.text.toString().trim()

            if (fn.isEmpty() || ln.isEmpty() || email.isEmpty() || p1.isEmpty() || p2.isEmpty()) {
                SweetAlertHelper.showError(
                    this,
                    "Campos Obligatorios",
                    "Todos los campos del formulario son requeridos."
                )
                return@setOnClickListener
            }

            // Anti-script & anti-injection validation
            if (ValidationUtils.containsScriptOrInjection(fn) ||
                ValidationUtils.containsScriptOrInjection(ln) ||
                ValidationUtils.containsScriptOrInjection(email) ||
                ValidationUtils.containsScriptOrInjection(p1) ||
                ValidationUtils.containsScriptOrInjection(p2)
            ) {
                SweetAlertHelper.showError(
                    this,
                    "Seguridad - Entrada Inválida",
                    "No se permiten scripts, comandos ni caracteres peligrosos en las celdas."
                )
                return@setOnClickListener
            }

            // Strict Name Validation (no numbers like 12345, no special symbols)
            if (!ValidationUtils.isValidName(fn)) {
                SweetAlertHelper.showError(
                    this,
                    "Nombre Inválido",
                    "El nombre solo debe contener letras. No se permiten números (como 12345) ni caracteres especiales."
                )
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidName(ln)) {
                SweetAlertHelper.showError(
                    this,
                    "Apellido Inválido",
                    "El apellido solo debe contener letras. No se permiten números (como 12345) ni caracteres especiales."
                )
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidEmail(email)) {
                SweetAlertHelper.showError(
                    this,
                    "Formato Inválido",
                    "Por favor ingrese un correo electrónico válido."
                )
                return@setOnClickListener
            }

            if (dbHelper.isEmailExists(email)) {
                SweetAlertHelper.showError(
                    this,
                    "Correo Ya Registrado",
                    "El correo electrónico ingresado ya se encuentra en uso."
                )
                return@setOnClickListener
            }

            if (p1 != p2) {
                SweetAlertHelper.showError(
                    this,
                    "Claves No Coinciden",
                    "Las contraseñas ingresadas no son idénticas."
                )
                return@setOnClickListener
            }

            if (!ValidationUtils.isRobustPassword(p1)) {
                SweetAlertHelper.showError(
                    this,
                    "Contraseña Débil",
                    "La clave debe tener mínimo 8 caracteres, 1 mayúscula, 1 minúscula, 1 número y 1 carácter especial."
                )
                return@setOnClickListener
            }

            val newUser = User(
                rut = "",
                firstname = fn,
                lastname = ln,
                email = email,
                phone = "",
                password = p1
            )

            val result = dbHelper.insertUser(newUser)
            if (result != -1L) {
                SweetAlertHelper.showSuccess(
                    context = this,
                    title = "¡Registro Exitoso!",
                    message = "El usuario se ha registrado correctamente. Redirigiendo al Login..."
                ) {
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.putExtra("SHOW_LOGIN_FORM", true)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            } else {
                SweetAlertHelper.showError(
                    this,
                    "Error de Registro",
                    "No se pudo completar el registro. Intente nuevamente."
                )
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (com.example.myapplication.util.SwipeBackHelper.processDispatchTouchEvent(this, ev)) {
            return true
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onResume() {
        super.onResume()
    }
}
