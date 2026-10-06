package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.model.Developer
import com.example.myapplication.util.DeveloperHelper
import com.example.myapplication.util.SweetAlertHelper
import com.example.myapplication.util.SwipeBackHelper
import com.google.android.material.button.MaterialButton
import java.io.File
import java.io.FileOutputStream

class DeveloperProfileActivity : AppCompatActivity() {

    private var devId: Int = 1
    private var currentDeveloper: Developer? = null
    private var selectedPhotoPath: String? = null

    private lateinit var ivDevPhoto: ImageView
    private lateinit var tvDevName: TextView
    private lateinit var tvDevRole: TextView
    private lateinit var tvDevDesc: TextView
    private lateinit var tvDevEmail: TextView
    private lateinit var tvDevGithub: TextView

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val savedPath = saveImageToInternalStorage(uri)
            if (savedPath != null) {
                selectedPhotoPath = savedPath
                displayPhoto(savedPath)
                DeveloperHelper.saveDeveloperPhoto(this, devId, savedPath)
                SweetAlertHelper.showSuccess(this, "¡Foto Guardada!", "La foto de perfil del desarrollador ha sido actualizada.")
            } else {
                SweetAlertHelper.showError(this, "Error", "No se pudo procesar la imagen seleccionada.")
            }
        }
    }

    override fun dispatchTouchEvent(ev: android.view.MotionEvent): Boolean {
        if (SwipeBackHelper.processDispatchTouchEvent(this, ev)) {
            return true
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_developer_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dev_profile_scroll)) { v, insets ->
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

        devId = intent.getIntExtra("DEV_ID", 1)
        currentDeveloper = DeveloperHelper.getDeveloperById(this, devId)

        val btnBack = findViewById<ImageButton>(R.id.btn_dev_profile_back)
        btnBack?.setOnClickListener { finish() }

        ivDevPhoto = findViewById(R.id.iv_dev_profile_photo)
        tvDevName = findViewById(R.id.tv_dev_profile_name)
        tvDevRole = findViewById(R.id.tv_dev_profile_role)
        tvDevDesc = findViewById(R.id.tv_dev_profile_desc)
        tvDevEmail = findViewById(R.id.tv_dev_profile_email)
        tvDevGithub = findViewById(R.id.tv_dev_profile_github)

        val btnChangePhoto = findViewById<MaterialButton>(R.id.btn_dev_change_photo)
        val btnSave = findViewById<MaterialButton>(R.id.btn_dev_profile_save)

        btnChangePhoto?.visibility = android.view.View.GONE
        btnSave?.visibility = android.view.View.GONE

        currentDeveloper?.let { dev ->
            tvDevName.text = dev.name
            tvDevRole.text = dev.role
            tvDevDesc.text = dev.description
            tvDevEmail.text = "✉ ${dev.email}"
            tvDevGithub.text = "🔗 ${dev.github}"

            selectedPhotoPath = dev.photoUri
            if (!selectedPhotoPath.isNullOrEmpty()) {
                displayPhoto(selectedPhotoPath!!)
            } else if (dev.id == 1 || dev.name.contains("Jorge", ignoreCase = true)) {
                displayPhoto("android.resource://$packageName/${R.drawable.jorge_cortes}")
            } else if (dev.id == 2 || dev.name.contains("Kevin", ignoreCase = true)) {
                displayPhoto("android.resource://$packageName/${R.drawable.kevin_encina}")
            } else if (dev.id == 3 || dev.name.contains("Scarlett", ignoreCase = true) || dev.name.contains("Scar", ignoreCase = true)) {
                displayPhoto("android.resource://$packageName/${R.drawable.scarlett_williams}")
            }
        }
    }

    private fun displayPhoto(path: String) {
        try {
            val file = File(path)
            ivDevPhoto.clearColorFilter()
            ivDevPhoto.imageTintList = null
            ivDevPhoto.setPadding(0, 0, 0, 0)
            ivDevPhoto.scaleType = ImageView.ScaleType.CENTER_CROP

            if (file.exists()) {
                ivDevPhoto.setImageURI(Uri.fromFile(file))
            } else {
                val uri = Uri.parse(path)
                if (uri.scheme == "android.resource" || uri.scheme == "content" || uri.scheme == "file") {
                    ivDevPhoto.setImageURI(uri)
                } else {
                    val resId = resources.getIdentifier(path, "drawable", packageName)
                    if (resId != 0) {
                        ivDevPhoto.setImageResource(resId)
                    } else {
                        ivDevPhoto.setImageURI(uri)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ivDevPhoto.setImageResource(R.drawable.ic_users)
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val dir = File(filesDir, "dev_photos")
            if (!dir.exists()) dir.mkdirs()
            val fileName = "dev_photo_${devId}_${System.currentTimeMillis()}.jpg"
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
