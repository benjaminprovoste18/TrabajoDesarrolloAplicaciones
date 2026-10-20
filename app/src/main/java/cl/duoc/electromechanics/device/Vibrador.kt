package cl.duoc.electromechanics.device

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class Vibrador(private val context: Context) {
    private val vibrador: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun confirmacion() = vibrar(VibrationEffect.createOneShot(180, VibrationEffect.DEFAULT_AMPLITUDE))

    fun error() = vibrar(VibrationEffect.createWaveform(longArrayOf(0, 90, 80, 90), -1))

    private fun vibrar(efecto: VibrationEffect) {
        if (!vibrador.hasVibrator()) return
        vibrador.vibrate(efecto)
        Log.d("Vibrador", "Vibración enviada")
    }
}
