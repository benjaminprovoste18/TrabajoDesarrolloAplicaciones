package cl.duoc.electromechanics.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.electromechanics.data.local.relation.RegistroConEmpleado
import cl.duoc.electromechanics.data.repository.AuthRepository
import cl.duoc.electromechanics.data.repository.JornadaRepository
import cl.duoc.electromechanics.data.repository.OrdenRepository
import cl.duoc.electromechanics.data.session.SessionManager
import cl.duoc.electromechanics.domain.rules.JornadaRules
import cl.duoc.electromechanics.domain.util.DateUtils
import cl.duoc.electromechanics.model.EstadoServicio
import cl.duoc.electromechanics.model.ResumenHoras
import cl.duoc.electromechanics.model.SupervisorModel
import cl.duoc.electromechanics.model.TipoMarcacion
import kotlinx.coroutines.launch

class SupervisorViewModel(
    session: SessionManager,
    ordenRepository: OrdenRepository,
    private val jornadaRepository: JornadaRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    var uiState by mutableStateOf(SupervisorModel(nombre = session.actual()?.nombre.orEmpty()))
        private set

    init {
        viewModelScope.launch {
            ordenRepository.observeAll().collect { ordenes ->
                uiState = uiState.copy(ordenes = ordenes)
            }
        }
        viewModelScope.launch {
            jornadaRepository.observeAll().collect { registros ->
                uiState = uiState.copy(registros = registros, resumen = calcularResumen(registros))
            }
        }
    }

    fun onFiltroChanged(estado: EstadoServicio?) {
        uiState = uiState.copy(filtro = estado)
    }

    fun onAprobarRegistro(id: String) {
        viewModelScope.launch {
            jornadaRepository.aprobar(id)
            uiState = uiState.copy(mensaje = "Marcación aprobada")
        }
    }

    fun onMensajeMostrado() {
        uiState = uiState.copy(mensaje = null)
    }

    fun onCerrarSesion(onLogout: () -> Unit) {
        authRepository.logout()
        onLogout()
    }

    private fun calcularResumen(registros: List<RegistroConEmpleado>): List<ResumenHoras> {
        val hoy = DateUtils.startOfToday()
        val ahora = System.currentTimeMillis()
        return registros
            .filter { it.registro.fechaHoraMarcacion >= hoy }
            .groupBy { it.registro.idEmpleado }
            .map { (_, lista) ->
                val ordenadas = lista.sortedBy { it.registro.fechaHoraMarcacion }
                val marcas = ordenadas.map { TipoMarcacion.from(it.registro.tipoMarcacion) to it.registro.fechaHoraMarcacion }
                ResumenHoras(
                    nombre = ordenadas.first().empleado?.nombreCompleto ?: "Empleado",
                    horasMillis = JornadaRules.horasTrabajadas(marcas, ahora),
                    marcaciones = lista.size,
                    fueraDeRadio = lista.count { !it.registro.dentroDeRadio },
                    enJornada = marcas.lastOrNull()?.first == TipoMarcacion.ENTRADA
                )
            }
            .sortedByDescending { it.horasMillis }
    }
}
