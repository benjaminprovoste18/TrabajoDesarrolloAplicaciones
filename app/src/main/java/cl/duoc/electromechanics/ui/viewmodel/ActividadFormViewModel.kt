package cl.duoc.electromechanics.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.electromechanics.data.repository.OrdenRepository
import cl.duoc.electromechanics.domain.rules.ServicioRules
import cl.duoc.electromechanics.domain.validation.FormValidators
import cl.duoc.electromechanics.domain.validation.ValidationResult
import cl.duoc.electromechanics.model.ActividadFormModel
import cl.duoc.electromechanics.model.EstadoEquipo
import cl.duoc.electromechanics.model.EstadoServicio
import cl.duoc.electromechanics.ui.navigation.Routes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ActividadFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val ordenRepository: OrdenRepository
) : ViewModel() {
    private val idServicio: String = checkNotNull(savedStateHandle[Routes.ARG_SERVICIO])

    var uiState by mutableStateOf(ActividadFormModel())
        private set

    private fun validarDetalle(v: String) = FormValidators.text(v, "El detalle de la tarea", 10, 500)
    private fun validarRepuestos(v: String) = FormValidators.text(v, "Repuestos", 3, 300, required = false)
    private fun validarComentarios(v: String) = FormValidators.text(v, "Los comentarios", 5, 500, required = false)
    private fun validarEstado(v: EstadoEquipo?): ValidationResult = FormValidators.selection(v, "el estado del equipo")

    fun onDetalleChanged(v: String) {
        uiState = uiState.copy(detalle = uiState.detalle.withValue(v, ::validarDetalle))
    }

    fun onRepuestosChanged(v: String) {
        uiState = uiState.copy(repuestos = uiState.repuestos.withValue(v, ::validarRepuestos))
    }

    fun onComentariosChanged(v: String) {
        uiState = uiState.copy(comentarios = uiState.comentarios.withValue(v, ::validarComentarios))
    }

    fun onEstadoEquipoChanged(v: EstadoEquipo) {
        uiState = uiState.copy(estadoEquipo = v, estadoEquipoError = null)
    }

    fun onDetalleFocusLost() {
        uiState = uiState.copy(detalle = uiState.detalle.validated(::validarDetalle))
    }

    fun onRepuestosFocusLost() {
        uiState = uiState.copy(repuestos = uiState.repuestos.validated(::validarRepuestos))
    }

    fun onComentariosFocusLost() {
        uiState = uiState.copy(comentarios = uiState.comentarios.validated(::validarComentarios))
    }

    fun onMensajeMostrado() {
        uiState = uiState.copy(mensaje = null)
    }

    fun onGuardarClick(onSuccess: () -> Unit) {
        if (uiState.guardando) return
        val validado = uiState.copy(
            detalle = uiState.detalle.validated(::validarDetalle),
            repuestos = uiState.repuestos.validated(::validarRepuestos),
            comentarios = uiState.comentarios.validated(::validarComentarios),
            estadoEquipoError = validarEstado(uiState.estadoEquipo).errorMessage
        )
        val hayErrores = validado.detalle.hasError || validado.repuestos.hasError ||
            validado.comentarios.hasError || validado.estadoEquipoError != null
        if (hayErrores) {
            uiState = validado.copy(
                shakeTrigger = uiState.shakeTrigger + 1,
                mensaje = "Revisa los campos marcados en rojo"
            )
            return
        }

        uiState = validado.copy(guardando = true)
        viewModelScope.launch {
            val orden = ordenRepository.observeById(idServicio).first()
            val estado = orden?.let { EstadoServicio.from(it.orden.estado) }
            if (estado == null || !ServicioRules.puedeRegistrarActividad(estado)) {
                uiState = uiState.copy(
                    guardando = false,
                    mensaje = "El servicio debe estar En Proceso para registrar actividades"
                )
                return@launch
            }
            ordenRepository.registrarActividad(
                idServicio = idServicio,
                detalle = validado.detalle.value,
                repuestos = validado.repuestos.value,
                estadoEquipo = validado.estadoEquipo!!.name,
                comentarios = validado.comentarios.value
            )
            uiState = uiState.copy(guardando = false)
            onSuccess()
        }
    }
}
