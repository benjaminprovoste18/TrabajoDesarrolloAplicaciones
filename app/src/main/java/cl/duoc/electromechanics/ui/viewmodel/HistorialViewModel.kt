package cl.duoc.electromechanics.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.electromechanics.data.local.relation.RegistroConEmpleado
import cl.duoc.electromechanics.data.repository.JornadaRepository
import cl.duoc.electromechanics.data.session.SessionManager
import kotlinx.coroutines.launch

class HistorialViewModel(
    session: SessionManager,
    jornadaRepository: JornadaRepository
) : ViewModel() {
    var registros by mutableStateOf<List<RegistroConEmpleado>>(emptyList())
        private set

    init {
        val idEmpleado = session.actual()?.idEmpleado.orEmpty()
        viewModelScope.launch {
            jornadaRepository.observeHistorial(idEmpleado).collect { registros = it }
        }
    }
}
