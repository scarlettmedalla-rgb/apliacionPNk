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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.util.SweetAlertHelper
import com.example.myapplication.util.ValidationUtils
import com.google.android.material.button.MaterialButton

class ForgotPasswordActivity : AppCompatActivity() {

    private var countDownTimer: CountDownTimer? = null

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
        val tvTimer = findViewById<TextView>(R.id.tv_timer)

        startTimer(tvTimer)

        setupAutoAdvance(etCode1, etCode2)
        setupAutoAdvance(etCode2, etCode3)
        setupAutoAdvance(etCode3, etCode4)

        btnRecuperar.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                SweetAlertHelper.showError(
                    this,
                    "Campo Obligatorio",
                    "Por favor ingrese su correo electrónico."
                )
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidEmail(email)) {
                SweetAlertHelper.showError(
                    this,
                    "Formato Inválido",
                    "Por favor ingrese un correo válido."
                )
                return@setOnClickListener
            }

            SweetAlertHelper.showSuccess(
                context = this,
                title = "Código Generado",
                message = "Se ha enviado un código de verificación de 4 dígitos a su correo."
            ) {
                // Auto fill code or navigate to ResetPasswordActivity
                etCode1.setText("1")
                etCode2.setText("2")
                etCode3.setText("3")
                etCode4.setText("4")

                val intent = Intent(this, ResetPasswordActivity::class.java)
                intent.putExtra("USER_EMAIL", email)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun startTimer(tvTimer: TextView) {
        countDownTimer = object : CountDownTimer(59000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                tvTimer.text = "$seconds Segundos"
            }

            override fun onFinish() {
                tvTimer.text = "0 Segundos"
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
