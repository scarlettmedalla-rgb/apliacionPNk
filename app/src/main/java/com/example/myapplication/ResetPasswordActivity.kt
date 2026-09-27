package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.util.SweetAlertHelper
import com.example.myapplication.util.ValidationUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ResetPasswordActivity : AppCompatActivity() {

    override fun dispatchTouchEvent(ev: android.view.MotionEvent): Boolean {
        if (com.example.myapplication.util.SwipeBackHelper.processDispatchTouchEvent(this, ev)) {
            return true
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reset_password)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.reset_scroll)) { v, insets ->
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

        val etPass1 = findViewById<TextInputEditText>(R.id.et_reset_pass1)
        val etPass2 = findViewById<TextInputEditText>(R.id.et_reset_pass2)
        val btnCrear = findViewById<MaterialButton>(R.id.btn_reset_crear)

        btnCrear.setOnClickListener {
            val p1 = etPass1.text?.toString()?.trim() ?: ""
            val p2 = etPass2.text?.toString()?.trim() ?: ""

            if (p1.isEmpty() || p2.isEmpty()) {
                SweetAlertHelper.showError(
                    this,
                    "Campos Obligatorios",
                    "Por favor ingresa y repite la nueva contraseña."
                )
                return@setOnClickListener
            }

            if (p1 != p2) {
                SweetAlertHelper.showError(
                    this,
                    "No Coinciden",
                    "Las contraseñas ingresadas no son idénticas."
                )
                return@setOnClickListener
            }

            if (!ValidationUtils.isRobustPassword(p1)) {
                SweetAlertHelper.showError(
                    this,
                    "Contraseña Débil",
                    "La contraseña debe tener mínimo 8 caracteres, 1 mayúscula, 1 minúscula, 1 número y 1 carácter especial."
                )
                return@setOnClickListener
            }

            SweetAlertHelper.showSuccess(
                context = this,
                title = "¡Contraseña Creada!",
                message = "Su nueva contraseña ha sido establecida con éxito."
            ) {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}
