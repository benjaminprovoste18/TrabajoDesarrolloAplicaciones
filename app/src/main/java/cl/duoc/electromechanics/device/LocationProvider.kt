package cl.duoc.electromechanics.device

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

sealed interface GpsResult {
    data class Success(
        val latitud: Double,
        val longitud: Double,
        val precisionMetros: Float,
        val simulada: Boolean
    ) : GpsResult

    data object PermissionDenied : GpsResult
    data object GpsDisabled : GpsResult
    data object Unavailable : GpsResult
}

class LocationProvider(private val context: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(context)

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    fun isGpsEnabled(): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(lm)
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): GpsResult {
        if (!hasPermission()) return GpsResult.PermissionDenied
        if (!isGpsEnabled()) return GpsResult.GpsDisabled
        return try {
            val cts = CancellationTokenSource()
            val location: Location? = withTimeoutOrNull(TIMEOUT_MS) {
                client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token).esperar()
            }
            if (location == null) {
                cts.cancel()
                GpsResult.Unavailable
            } else {
                GpsResult.Success(
                    latitud = location.latitude,
                    longitud = location.longitude,
                    precisionMetros = if (location.hasAccuracy()) location.accuracy else 0f,
                    simulada = isMock(location)
                )
            }
        } catch (e: Exception) {
            GpsResult.Unavailable
        }
    }

    @Suppress("DEPRECATION")
    private fun isMock(location: Location): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) location.isMock else location.isFromMockProvider

    private companion object {
        const val TIMEOUT_MS = 15_000L
    }
}

private suspend fun <T> Task<T>.esperar(): T? = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { resultado -> if (cont.isActive) cont.resume(resultado) }
    addOnFailureListener { if (cont.isActive) cont.resume(null) }
    addOnCanceledListener { if (cont.isActive) cont.resume(null) }
}
