package com.example.myapplication.util

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import cn.pedant.SweetAlert.SweetAlertDialog

object SweetAlertHelper {

    private fun isContextInvalid(context: Context): Boolean {
        if (context is Activity) {
            return context.isFinishing || context.isDestroyed
        }
        return false
    }

    fun showWarning(
        context: Context,
        title: String,
        message: String,
        confirmText: String = "Entiendo",
        onConfirm: (() -> Unit)? = null
    ) {
        if (isContextInvalid(context)) return
        try {
            val dialog = SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
            dialog.titleText = title
            dialog.contentText = message
            dialog.confirmText = confirmText
            dialog.setConfirmClickListener { d ->
                try {
                    (d as? SweetAlertDialog)?.dismissWithAnimation()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                onConfirm?.invoke()
            }
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
            onConfirm?.invoke()
        }
    }

    fun showSuccess(
        context: Context,
        title: String,
        message: String,
        confirmText: String = "Aceptar",
        onConfirm: (() -> Unit)? = null
    ) {
        if (isContextInvalid(context)) return
        try {
            val dialog = SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
            dialog.titleText = title
            dialog.contentText = message
            dialog.confirmText = confirmText
            dialog.setConfirmClickListener { d ->
                try {
                    (d as? SweetAlertDialog)?.dismissWithAnimation()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                onConfirm?.invoke()
            }
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
            onConfirm?.invoke()
        }
    }

    fun showError(
        context: Context,
        title: String,
        message: String,
        confirmText: String = "Cerrar",
        onConfirm: (() -> Unit)? = null
    ) {
        if (isContextInvalid(context)) return
        try {
            val dialog = SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
            dialog.titleText = title
            dialog.contentText = message
            dialog.confirmText = confirmText
            dialog.setConfirmClickListener { d ->
                try {
                    (d as? SweetAlertDialog)?.dismissWithAnimation()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                onConfirm?.invoke()
            }
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
            onConfirm?.invoke()
        }
    }

    fun showProgress(
        context: Context,
        title: String
    ): SweetAlertDialog? {
        if (isContextInvalid(context)) return null
        return try {
            val dialog = SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE)
            dialog.titleText = title
            dialog.setCancelable(false)
            dialog.show()
            dialog
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun showConfirmation(
        context: Context,
        title: String,
        message: String,
        confirmText: String = "Sí",
        cancelText: String = "No",
        onConfirm: () -> Unit,
        onCancel: (() -> Unit)? = null
    ) {
        if (isContextInvalid(context)) return
        try {
            val dialog = SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
            dialog.titleText = title
            dialog.contentText = message
            dialog.confirmText = confirmText
            dialog.cancelText = cancelText
            dialog.setConfirmClickListener { d ->
                try {
                    (d as? SweetAlertDialog)?.dismissWithAnimation()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                onConfirm.invoke()
            }
            dialog.setCancelClickListener { d ->
                try {
                    (d as? SweetAlertDialog)?.dismissWithAnimation()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                onCancel?.invoke()
            }
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
            onConfirm.invoke()
        }
    }

    fun showProgressToSuccess(
        context: Context,
        loadingTitle: String,
        successTitle: String,
        successMessage: String,
        durationMs: Long = 1500L,
        onConfirm: (() -> Unit)? = null
    ) {
        if (isContextInvalid(context)) return
        try {
            val pDialog = showProgress(context, loadingTitle)
            if (pDialog == null) {
                onConfirm?.invoke()
                return
            }
            Handler(Looper.getMainLooper()).postDelayed({
                if (isContextInvalid(context)) return@postDelayed
                try {
                    pDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                    pDialog.titleText = successTitle
                    pDialog.contentText = successMessage
                    pDialog.confirmText = "Aceptar"
                    pDialog.setConfirmClickListener { d ->
                        try {
                            (d as? SweetAlertDialog)?.dismissWithAnimation()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        onConfirm?.invoke()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    onConfirm?.invoke()
                }
            }, durationMs)
        } catch (e: Exception) {
            e.printStackTrace()
            onConfirm?.invoke()
        }
    }
}
