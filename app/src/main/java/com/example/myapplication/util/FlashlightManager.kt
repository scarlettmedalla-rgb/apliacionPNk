package com.example.myapplication.util

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

object FlashlightManager {

    private var isFlashlightOn: Boolean = false

    fun isTorchOn(): Boolean = isFlashlightOn

    fun toggleFlashlight(context: Context): Boolean {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            SweetAlertHelper.showError(
                context,
                "Sin Flash",
                "Este dispositivo no cuenta con flash o linterna integrada."
            )
            return false
        }

        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        if (cameraManager == null) {
            SweetAlertHelper.showError(
                context,
                "Error",
                "No fue posible acceder al servicio de cámara."
            )
            return false
        }

        try {
            val cameraId = getCameraWithFlashId(cameraManager)
            if (cameraId == null) {
                SweetAlertHelper.showError(
                    context,
                    "Sin Linterna",
                    "No se encontró una cámara con flash disponible."
                )
                return false
            }

            val targetState = !isFlashlightOn
            cameraManager.setTorchMode(cameraId, targetState)
            isFlashlightOn = targetState
            return true
        } catch (e: CameraAccessException) {
            SweetAlertHelper.showError(
                context,
                "Error de Acceso",
                "La linterna está siendo usada por otra aplicación o no está disponible."
            )
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun turnOff(context: Context) {
        if (!isFlashlightOn) return
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return
        try {
            val cameraId = getCameraWithFlashId(cameraManager) ?: return
            cameraManager.setTorchMode(cameraId, false)
            isFlashlightOn = false
        } catch (e: Exception) {
            // silent ignore
        }
    }

    private fun getCameraWithFlashId(cameraManager: CameraManager): String? {
        for (id in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                return id
            }
        }
        for (id in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(id)
            if (characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true) {
                return id
            }
        }
        return null
    }
}
