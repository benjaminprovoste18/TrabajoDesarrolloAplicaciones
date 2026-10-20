package cl.duoc.electromechanics.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.electromechanics.data.repository.AuthRepository
import cl.duoc.electromechanics.data.seed.DatabaseSeeder
import cl.duoc.electromechanics.ui.navigation.Routes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel(
    private val seeder: DatabaseSeeder,
    private val authRepository: AuthRepository
) : ViewModel() {
    var destino by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            val inicio = System.currentTimeMillis()
            seeder.seedIfEmpty()
            val transcurrido = System.currentTimeMillis() - inicio
            if (transcurrido < 1200) delay(1200 - transcurrido)
            val sesion = authRepository.sesionActual()
            destino = sesion?.let { Routes.homeFor(it.rol) } ?: Routes.LOGIN
        }
    }
}
