package com.example.myapplication.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapter.UserAdapter
import com.example.myapplication.db.DatabaseHelper
import com.example.myapplication.model.User
import com.example.myapplication.util.SweetAlertHelper
import com.example.myapplication.util.ValidationUtils
import com.google.android.material.button.MaterialButton

class UsersFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var userAdapter: UserAdapter
    private lateinit var rvUsers: RecyclerView
    private lateinit var etSearch: EditText

    private lateinit var layoutSearchSection: LinearLayout
    private lateinit var layoutAddSection: ScrollView

    private lateinit var btnModeSearch: MaterialButton
    private lateinit var btnModeAdd: MaterialButton

    // Form fields
    private lateinit var etRut: EditText
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSave: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_users, container, false)

        dbHelper = DatabaseHelper(requireContext())

        // UI Binding
        layoutSearchSection = view.findViewById(R.id.layout_search_user_section)
        layoutAddSection = view.findViewById(R.id.layout_add_user_section)

        btnModeSearch = view.findViewById(R.id.btn_mode_search)
        btnModeAdd = view.findViewById(R.id.btn_mode_add)

        rvUsers = view.findViewById(R.id.rv_users)
        rvUsers.layoutManager = LinearLayoutManager(requireContext())

        etSearch = view.findViewById(R.id.et_search_user)

        etRut = view.findViewById(R.id.et_add_rut)
        etFirstName = view.findViewById(R.id.et_add_firstname)
        etLastName = view.findViewById(R.id.et_add_lastname)
        etEmail = view.findViewById(R.id.et_add_email)
        etPhone = view.findViewById(R.id.et_add_phone)
        etPassword = view.findViewById(R.id.et_add_password)
        btnSave = view.findViewById(R.id.btn_save_user)

        userAdapter = UserAdapter(
            users = emptyList(),
            onEditClick = { user -> showEditUserDialog(user) },
            onDeleteClick = { user -> showDeleteConfirmation(user) }
        )
        rvUsers.adapter = userAdapter

        // Segmented Tab Listeners
        btnModeSearch.setOnClickListener {
            switchTab(isSearchTab = true)
        }

        btnModeAdd.setOnClickListener {
            switchTab(isSearchTab = false)
        }

        // Live Search Listener
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                if (ValidationUtils.containsScriptOrInjection(query)) {
                    SweetAlertHelper.showError(requireContext(), "Seguridad", "No se permiten scripts en la búsqueda.")
                    etSearch.setText("")
                } else {
                    loadUsers(query)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnSave.setOnClickListener {
            saveNewUser()
        }

        loadUsers("")

        return view
    }

    private fun switchTab(isSearchTab: Boolean) {
        val pinkPrimary = ContextCompat.getColor(requireContext(), R.color.pink_primary)
        val textSecondary = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        val white = ContextCompat.getColor(requireContext(), R.color.white)
        val transparent = ContextCompat.getColor(requireContext(), android.R.color.transparent)

        if (isSearchTab) {
            btnModeSearch.backgroundTintList = ColorStateList.valueOf(pinkPrimary)
            btnModeSearch.setTextColor(white)

            btnModeAdd.backgroundTintList = ColorStateList.valueOf(transparent)
            btnModeAdd.setTextColor(textSecondary)

            layoutSearchSection.visibility = View.VISIBLE
            layoutAddSection.visibility = View.GONE
            loadUsers(etSearch.text.toString())
        } else {
            btnModeAdd.backgroundTintList = ColorStateList.valueOf(pinkPrimary)
            btnModeAdd.setTextColor(white)

            btnModeSearch.backgroundTintList = ColorStateList.valueOf(transparent)
            btnModeSearch.setTextColor(textSecondary)

            layoutSearchSection.visibility = View.GONE
            layoutAddSection.visibility = View.VISIBLE
        }
    }

    private fun loadUsers(query: String) {
        val users = dbHelper.searchUsers(query)
        userAdapter.updateData(users)
    }

    private fun saveNewUser() {
        val rut = etRut.text.toString().trim()
        val fn = etFirstName.text.toString().trim()
        val ln = etLastName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val pass = etPassword.text.toString().trim()

        if (rut.isEmpty() || fn.isEmpty() || email.isEmpty()) {
            SweetAlertHelper.showError(
                requireContext(),
                "Campos Incompletos",
                "RUT, Nombre y Correo son obligatorios."
            )
            return
        }

        if (ValidationUtils.containsScriptOrInjection(rut) ||
            ValidationUtils.containsScriptOrInjection(fn) ||
            ValidationUtils.containsScriptOrInjection(ln) ||
            ValidationUtils.containsScriptOrInjection(email) ||
            ValidationUtils.containsScriptOrInjection(phone) ||
            ValidationUtils.containsScriptOrInjection(pass)
        ) {
            SweetAlertHelper.showError(
                requireContext(),
                "Seguridad - Entrada Inválida",
                "No se permiten scripts, comandos ni caracteres peligrosos en las celdas."
            )
            return
        }

        if (!ValidationUtils.isValidName(fn)) {
            SweetAlertHelper.showError(
                requireContext(),
                "Nombre Inválido",
                "El nombre solo debe contener letras. No se permiten números (como 12345) ni símbolos."
            )
            return
        }

        if (ln.isNotEmpty() && !ValidationUtils.isValidName(ln)) {
            SweetAlertHelper.showError(
                requireContext(),
                "Apellido Inválido",
                "El apellido solo debe contener letras. No se permiten números (como 12345) ni símbolos."
            )
            return
        }

        if (!ValidationUtils.isValidEmail(email)) {
            SweetAlertHelper.showError(
                requireContext(),
                "Correo Inválido",
                "Por favor ingrese un correo electrónico válido."
            )
            return
        }

        val newUser = User(
            rut = rut,
            firstname = fn,
            lastname = ln,
            email = email,
            phone = phone,
            password = pass.ifEmpty { "Clave123!" }
        )
        dbHelper.insertUser(newUser)

        SweetAlertHelper.showProgressToSuccess(
            context = requireContext(),
            loadingTitle = "Guardando usuario...",
            successTitle = "¡Usuario Ingresado!",
            successMessage = "El usuario se ha registrado correctamente.",
            durationMs = 1200
        )

        // Clear form fields
        etRut.text.clear()
        etFirstName.text.clear()
        etLastName.text.clear()
        etEmail.text.clear()
        etPhone.text.clear()
        etPassword.text.clear()

        // Switch to Search Mode Tab
        switchTab(isSearchTab = true)
    }

    private fun showEditUserDialog(user: User) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_register, null)
        val etR = dialogView.findViewById<EditText>(R.id.et_reg_rut)
        val etFn = dialogView.findViewById<EditText>(R.id.et_reg_firstname)
        val etLn = dialogView.findViewById<EditText>(R.id.et_reg_lastname)
        val etEm = dialogView.findViewById<EditText>(R.id.et_reg_email)
        val etPh = dialogView.findViewById<EditText>(R.id.et_reg_phone)
        val etPs = dialogView.findViewById<EditText>(R.id.et_reg_pass)
        val btnRegSave = dialogView.findViewById<MaterialButton>(R.id.btn_reg_save)
        val btnRegCancel = dialogView.findViewById<MaterialButton>(R.id.btn_reg_cancel)

        etR.setText(user.rut)
        etFn.setText(user.firstname)
        etLn.setText(user.lastname)
        etEm.setText(user.email)
        etPh.setText(user.phone)
        etPs.setText(user.password)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnRegSave.setOnClickListener {
            val rut = etR.text.toString().trim()
            val fn = etFn.text.toString().trim()
            val ln = etLn.text.toString().trim()
            val email = etEm.text.toString().trim()
            val phone = etPh.text.toString().trim()
            val pass = etPs.text.toString().trim()

            if (rut.isEmpty() || fn.isEmpty() || email.isEmpty()) {
                SweetAlertHelper.showError(
                    requireContext(),
                    "Error",
                    "RUT, Nombre y Correo son obligatorios."
                )
                return@setOnClickListener
            }

            if (ValidationUtils.containsScriptOrInjection(rut) ||
                ValidationUtils.containsScriptOrInjection(fn) ||
                ValidationUtils.containsScriptOrInjection(ln) ||
                ValidationUtils.containsScriptOrInjection(email) ||
                ValidationUtils.containsScriptOrInjection(phone) ||
                ValidationUtils.containsScriptOrInjection(pass)
            ) {
                SweetAlertHelper.showError(
                    requireContext(),
                    "Seguridad - Entrada Inválida",
                    "No se permiten scripts, comandos ni caracteres peligrosos en las celdas."
                )
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidName(fn)) {
                SweetAlertHelper.showError(
                    requireContext(),
                    "Nombre Inválido",
                    "El nombre solo debe contener letras. No se permiten números (como 12345) ni símbolos."
                )
                return@setOnClickListener
            }

            if (ln.isNotEmpty() && !ValidationUtils.isValidName(ln)) {
                SweetAlertHelper.showError(
                    requireContext(),
                    "Apellido Inválido",
                    "El apellido solo debe contener letras. No se permiten números (como 12345) ni símbolos."
                )
                return@setOnClickListener
            }

            val updatedUser = user.copy(
                rut = rut,
                firstname = fn,
                lastname = ln,
                email = email,
                phone = phone,
                password = pass
            )
            dbHelper.updateUser(updatedUser)
            alertDialog.dismiss()

            SweetAlertHelper.showSuccess(
                requireContext(),
                "¡Usuario Actualizado!",
                "Los datos se han guardado exitosamente."
            )

            loadUsers(etSearch.text.toString())
        }

        btnRegCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun showDeleteConfirmation(user: User) {
        SweetAlertHelper.showConfirmation(
            context = requireContext(),
            title = "¿Eliminar Usuario?",
            message = "Esta acción eliminará a ${user.fullName} del sistema.",
            confirmText = "Sí, Eliminar",
            cancelText = "Cancelar",
            onConfirm = {
                dbHelper.deleteUser(user.id)
                loadUsers(etSearch.text.toString())
                SweetAlertHelper.showSuccess(
                    requireContext(),
                    "¡Eliminado!",
                    "El usuario ha sido eliminado correctamente."
                )
            }
        )
    }
}
