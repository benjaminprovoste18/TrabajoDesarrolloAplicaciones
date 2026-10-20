package cl.duoc.electromechanics.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.electromechanics.data.repository.JornadaRepository
import cl.duoc.electromechanics.data.repository.OrdenRepository
import cl.duoc.electromechanics.data.session.SessionManager
import cl.duoc.electromechanics.domain.rules.JornadaRules
import cl.duoc.electromechanics.model.Rol
import cl.duoc.electromechanics.model.ServicioDetalleModel
import cl.duoc.electromechanics.model.TipoMarcacion
import cl.duoc.electromechanics.ui.navigation.Routes
import kotlinx.coroutines.launch

class ServicioDetalleViewModel(
    savedStateHandle: SavedStateHandle,
    session: SessionManager,
    private val ordenRepository: OrdenRepository,
    jornadaRepository: JornadaRepository
) : ViewModel() {
    private val idServicio: String = checkNotNull(savedStateHandle[Routes.ARG_SERVICIO])
    private val sesion = session.actual()

    var uiState by mutableStateOf(ServicioDetalleModel(rol = sesion?.rol ?: Rol.EMPLEADO))
        private set

    init {
        viewModelScope.launch {
            ordenRepository.observeById(idServicio).collect { orden ->
                uiState = uiState.copy(
                    cargando = false,
                    orden = orden?.let { o -> o.copy(actividades = o.actividades.sortedByDescending { it.horaRegistro }) }
                )
            }
        }
        if (sesion != null && sesion.rol == Rol.EMPLEADO) {
            viewModelScope.launch {
                jornadaRepository.observeUltimaDeHoy(sesion.idEmpleado).collect { ultima ->
                    val tipo = ultima?.let { TipoMarcacion.from(it.tipoMarcacion) }
                    uiState = uiState.copy(jornadaActiva = JornadaRules.jornadaActiva(tipo))
                }
            }
        }
    }

    fun onFotoInicial(path: String) = ejecutar("Foto inicial guardada") {
        ordenRepository.setFotoInicial(idServicio, path)
    }

    fun onFotoFinal(path: String) = ejecutar("Foto final guardada") {
        ordenRepository.setFotoFinal(idServicio, path)
    }

    fun onIniciarClick() {
        if (!uiState.puedeIniciar) {
            mostrarMensaje("Completa los requisitos para iniciar el servicio")
            return
        }
        ejecutar("Servicio iniciado: ya puedes registrar actividades") { ordenRepository.iniciar(idServicio) }
    }

    fun onFinalizarClick() {
        if (!uiState.puedeFinalizar) {
            mostrarMensaje("Completa los requisitos para finalizar el servicio")
            return
        }
        ejecutar("Servicio finalizado y enviado a revisión del supervisor") { ordenRepository.finalizar(idServicio) }
    }

    fun onAprobarClick() {
        if (!uiState.puedeAprobar) return
        ejecutar("Servicio aprobado") { ordenRepository.aprobar(idServicio) }
    }

    fun mostrarMensaje(texto: String) {
        uiState = uiState.copy(mensaje = texto)
    }

    fun onMensajeMostrado() {
        uiState = uiState.copy(mensaje = null)
    }

    private fun ejecutar(exito: String, accion: suspend () -> Unit) {
        if (uiState.procesando) return
        uiState = uiState.copy(procesando = true)
        viewModelScope.launch {
            val mensaje = try {
                accion()
                exito
            } catch (e: Exception) {
                "No se pudo guardar el cambio. Intenta nuevamente."
            }
            uiState = uiState.copy(procesando = false, mensaje = mensaje)
        }
    }
}
