package cl.duoc.electromechanics.device

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log

class Linterna(context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private val idCamara: String? by lazy {
        try {
            cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: Exception) {
            null
        }
    }

    val disponible: Boolean get() = idCamara != null

    fun cambiar(encender: Boolean): Boolean {
        val id = idCamara ?: return false
        return try {
            cameraManager.setTorchMode(id, encender)
            Log.d("Linterna", "Linterna ${if (encender) "ENCENDIDA" else "APAGADA"}")
            true
        } catch (e: Exception) {
            false
        }
    }
}
