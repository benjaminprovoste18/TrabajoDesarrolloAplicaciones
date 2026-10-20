package cl.duoc.electromechanics.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.electromechanics.data.repository.SucursalRepository
import cl.duoc.electromechanics.device.GpsResult
import cl.duoc.electromechanics.device.LocationProvider
import cl.duoc.electromechanics.domain.validation.FormValidators
import cl.duoc.electromechanics.model.FieldState
import cl.duoc.electromechanics.model.SucursalFormModel
import kotlinx.coroutines.launch
import java.util.Locale

class SucursalFormViewModel(
    private val repository: SucursalRepository,
    private val locationProvider: LocationProvider
) : ViewModel() {
    var uiState by mutableStateOf(SucursalFormModel())
        private set

    private fun validarNombre(v: String) = FormValidators.text(v, "El nombre del cliente", 3, 80)
    private fun validarDireccion(v: String) = FormValidators.text(v, "La dirección", 5, 120)
    private fun filtrarDecimal(v: String) = v.filter { it.isDigit() || it == '-' || it == '.' || it == ',' }.take(12)

    fun onNombreChanged(v: String) {
        uiState = uiState.copy(nombre = uiState.nombre.withValue(v, ::validarNombre))
    }

    fun onNombreFocusLost() {
        uiState = uiState.copy(nombre = uiState.nombre.validated(::validarNombre))
    }

    fun onDireccionChanged(v: String) {
        uiState = uiState.copy(direccion = uiState.direccion.withValue(v, ::validarDireccion))
    }

    fun onDireccionFocusLost() {
        uiState = uiState.copy(direccion = uiState.direccion.validated(::validarDireccion))
    }

    fun onLatitudChanged(v: String) {
        uiState = uiState.copy(latitud = uiState.latitud.withValue(filtrarDecimal(v), FormValidators::latitud))
    }

    fun onLatitudFocusLost() {
        uiState = uiState.copy(latitud = uiState.latitud.validated(FormValidators::latitud))
    }

    fun onLongitudChanged(v: String) {
        uiState = uiState.copy(longitud = uiState.longitud.withValue(filtrarDecimal(v), FormValidators::longitud))
    }

    fun onLongitudFocusLost() {
        uiState = uiState.copy(longitud = uiState.longitud.validated(FormValidators::longitud))
    }

    fun onUsarUbicacionClick() {
        if (uiState.obteniendoGps) return
        uiState = uiState.copy(obteniendoGps = true)
        viewModelScope.launch {
            uiState = when (val gps = locationProvider.getCurrentLocation()) {
                is GpsResult.Success -> uiState.copy(
                    obteniendoGps = false,
                    latitud = FieldState(String.format(Locale.US, "%.6f", gps.latitud)).validated(FormValidators::latitud),
                    longitud = FieldState(String.format(Locale.US, "%.6f", gps.longitud)).validated(FormValidators::longitud)
                )
                GpsResult.GpsDisabled -> uiState.copy(obteniendoGps = false, mensaje = "Activa la ubicación del dispositivo")
                GpsResult.PermissionDenied -> uiState.copy(obteniendoGps = false, mensaje = "Se necesita el permiso de ubicación")
                GpsResult.Unavailable -> uiState.copy(obteniendoGps = false, mensaje = "No se pudo obtener la ubicación")
            }
        }
    }

    fun onPermisoDenegado() {
        uiState = uiState.copy(mensaje = "Sin permiso de ubicación: ingresa las coordenadas manualmente")
    }

    fun onMensajeMostrado() {
        uiState = uiState.copy(mensaje = null)
    }

    fun onGuardarClick(onSuccess: () -> Unit) {
        if (uiState.guardando) return
        val v = uiState.copy(
            nombre = uiState.nombre.validated(::validarNombre),
            direccion = uiState.direccion.validated(::validarDireccion),
            latitud = uiState.latitud.validated(FormValidators::latitud),
            longitud = uiState.longitud.validated(FormValidators::longitud)
        )
        if (v.nombre.hasError || v.direccion.hasError || v.latitud.hasError || v.longitud.hasError) {
            uiState = v.copy(shakeTrigger = uiState.shakeTrigger + 1, mensaje = "Revisa los campos marcados en rojo")
            return
        }
        uiState = v.copy(guardando = true)
        viewModelScope.launch {
            repository.crear(
                nombre = v.nombre.value,
                direccion = v.direccion.value,
                lat = v.latitud.value.replace(",", ".").toDouble(),
                lon = v.longitud.value.replace(",", ".").toDouble()
            )
            uiState = uiState.copy(guardando = false)
            onSuccess()
        }
    }
}
