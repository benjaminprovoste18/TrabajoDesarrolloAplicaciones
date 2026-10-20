package cl.duoc.electromechanics.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.electromechanics.data.local.entity.EmpleadoEntity
import cl.duoc.electromechanics.data.repository.AuthRepository
import cl.duoc.electromechanics.data.repository.EmpleadoRepository
import cl.duoc.electromechanics.data.repository.SucursalRepository
import cl.duoc.electromechanics.data.session.SessionManager
import cl.duoc.electromechanics.model.AdminModel
import kotlinx.coroutines.launch

class AdminViewModel(
    session: SessionManager,
    private val empleadoRepository: EmpleadoRepository,
    sucursalRepository: SucursalRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    var uiState by mutableStateOf(
        AdminModel(
            nombre = session.actual()?.nombre.orEmpty(),
            idActual = session.actual()?.idEmpleado.orEmpty()
        )
    )
        private set

    init {
        viewModelScope.launch {
            empleadoRepository.observeAll().collect { uiState = uiState.copy(usuarios = it) }
        }
        viewModelScope.launch {
            sucursalRepository.observeAll().collect { uiState = uiState.copy(sucursales = it) }
        }
    }

    fun onActivoChanged(empleado: EmpleadoEntity, activo: Boolean) {
        if (empleado.idEmpleado == uiState.idActual) {
            uiState = uiState.copy(mensaje = "No puedes desactivar tu propia cuenta")
            return
        }
        viewModelScope.launch {
            empleadoRepository.setActivo(empleado.idEmpleado, activo)
            uiState = uiState.copy(
                mensaje = if (activo) "Cuenta activada" else "Cuenta desactivada: ya no podrá iniciar sesión"
            )
        }
    }

    fun onMensajeMostrado() {
        uiState = uiState.copy(mensaje = null)
    }

    fun onCerrarSesion(onLogout: () -> Unit) {
        authRepository.logout()
        onLogout()
    }
}
