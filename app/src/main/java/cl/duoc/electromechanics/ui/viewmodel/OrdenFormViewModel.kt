package cl.duoc.electromechanics.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.electromechanics.data.local.entity.EmpleadoEntity
import cl.duoc.electromechanics.data.local.entity.SucursalEntity
import cl.duoc.electromechanics.data.repository.EmpleadoRepository
import cl.duoc.electromechanics.data.repository.OrdenRepository
import cl.duoc.electromechanics.data.repository.SucursalRepository
import cl.duoc.electromechanics.device.NotificationHelper
import cl.duoc.electromechanics.domain.util.DateUtils
import cl.duoc.electromechanics.domain.validation.FormValidators
import cl.duoc.electromechanics.model.FieldState
import cl.duoc.electromechanics.model.OrdenFormModel
import cl.duoc.electromechanics.model.Prioridad
import kotlinx.coroutines.launch
import java.time.LocalDate

class OrdenFormViewModel(
    private val ordenRepository: OrdenRepository,
    empleadoRepository: EmpleadoRepository,
    sucursalRepository: SucursalRepository,
    private val notificationHelper: NotificationHelper
) : ViewModel() {
    var uiState by mutableStateOf(OrdenFormModel())
        private set

    init {
        viewModelScope.launch {
            sucursalRepository.observeAll().collect { uiState = uiState.copy(sucursales = it) }
        }
        viewModelScope.launch {
            empleadoRepository.observeTecnicosActivos().collect { uiState = uiState.copy(tecnicos = it) }
        }
    }

    private fun validarFecha(v: String) = FormValidators.fechaProgramada(v)
    private fun validarDescripcion(v: String) = FormValidators.text(v, "La descripción", 15, 300)
    private fun validarActividades(v: String) = FormValidators.text(v, "Las actividades mínimas", 10, 500)

    fun onSucursalChanged(sucursal: SucursalEntity) {
        uiState = uiState.copy(sucursal = sucursal, sucursalError = null)
    }

    fun onTecnicoChanged(tecnico: EmpleadoEntity) {
        uiState = uiState.copy(tecnico = tecnico, tecnicoError = null)
    }

    fun onPrioridadChanged(prioridad: Prioridad) {
        uiState = uiState.copy(prioridad = prioridad)
    }

    fun onFechaChanged(v: String) {
        val filtrado = v.filter { it.isDigit() || it == '/' }.take(10)
        uiState = uiState.copy(fecha = uiState.fecha.withValue(filtrado, ::validarFecha))
    }

    fun onFechaRapida(diasDesdeHoy: Long) {
        uiState = uiState.copy(fecha = FieldState(DateUtils.formatFecha(LocalDate.now().plusDays(diasDesdeHoy))))
    }

    fun onDescripcionChanged(v: String) {
        uiState = uiState.copy(descripcion = uiState.descripcion.withValue(v, ::validarDescripcion))
    }

    fun onActividadesChanged(v: String) {
        uiState = uiState.copy(actividades = uiState.actividades.withValue(v, ::validarActividades))
    }

    fun onFechaFocusLost() {
        uiState = uiState.copy(fecha = uiState.fecha.validated(::validarFecha))
    }

    fun onDescripcionFocusLost() {
        uiState = uiState.copy(descripcion = uiState.descripcion.validated(::validarDescripcion))
    }

    fun onActividadesFocusLost() {
        uiState = uiState.copy(actividades = uiState.actividades.validated(::validarActividades))
    }

    fun onMensajeMostrado() {
        uiState = uiState.copy(mensaje = null)
    }

    fun onGuardarClick(onSuccess: () -> Unit) {
        if (uiState.guardando) return
        val v = uiState.copy(
            sucursalError = FormValidators.selection(uiState.sucursal, "la sucursal del cliente").errorMessage,
            tecnicoError = FormValidators.selection(uiState.tecnico, "el técnico asignado").errorMessage,
            fecha = uiState.fecha.validated(::validarFecha),
            descripcion = uiState.descripcion.validated(::validarDescripcion),
            actividades = uiState.actividades.validated(::validarActividades)
        )
        val hayErrores = v.sucursalError != null || v.tecnicoError != null ||
            v.fecha.hasError || v.descripcion.hasError || v.actividades.hasError
        if (hayErrores) {
            uiState = v.copy(shakeTrigger = uiState.shakeTrigger + 1, mensaje = "Revisa los campos marcados en rojo")
            return
        }

        val sucursal = v.sucursal!!
        val tecnico = v.tecnico!!
        val fecha = DateUtils.localDateToMillis(DateUtils.parseFecha(v.fecha.value)!!)
        uiState = v.copy(guardando = true)

        viewModelScope.launch {
            ordenRepository.crear(
                idEmpleado = tecnico.idEmpleado,
                idSucursal = sucursal.idSucursal,
                fechaProgramada = fecha,
                descripcion = v.descripcion.value,
                actividadesRequeridas = v.actividades.value,
                prioridad = v.prioridad
            )
            val urgente = v.prioridad == Prioridad.URGENTE
            notificationHelper.notificarOrden(
                titulo = if (urgente) "Orden URGENTE asignada" else "Nueva orden de trabajo",
                mensaje = "${tecnico.nombreCompleto}: ${v.descripcion.value.trim()} en ${sucursal.nombreCliente} " +
                    "(${DateUtils.etiquetaDia(fecha).lowercase()})."
            )
            uiState = uiState.copy(guardando = false)
            onSuccess()
        }
    }
}
