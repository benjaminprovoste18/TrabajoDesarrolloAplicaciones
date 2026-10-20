package cl.duoc.electromechanics.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.electromechanics.ElectroApp
import cl.duoc.electromechanics.di.AppContainer

private fun CreationExtras.container(): AppContainer =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ElectroApp).container

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { SplashViewModel(container().seeder, container().authRepository) }
        initializer { LoginViewModel(container().authRepository, container().vibrador) }
        initializer {
            val c = container()
            TecnicoHomeViewModel(
                c.session, c.ordenRepository, c.jornadaRepository, c.sucursalRepository,
                c.locationProvider, c.vibrador, c.linterna, c.authRepository
            )
        }
        initializer { HistorialViewModel(container().session, container().jornadaRepository) }
        initializer {
            val c = container()
            ServicioDetalleViewModel(createSavedStateHandle(), c.session, c.ordenRepository, c.jornadaRepository)
        }
        initializer { ActividadFormViewModel(createSavedStateHandle(), container().ordenRepository) }
        initializer {
            val c = container()
            SupervisorViewModel(c.session, c.ordenRepository, c.jornadaRepository, c.authRepository)
        }
        initializer {
            val c = container()
            OrdenFormViewModel(c.ordenRepository, c.empleadoRepository, c.sucursalRepository, c.notificationHelper)
        }
        initializer {
            val c = container()
            AdminViewModel(c.session, c.empleadoRepository, c.sucursalRepository, c.authRepository)
        }
        initializer { UsuarioFormViewModel(container().empleadoRepository) }
        initializer { SucursalFormViewModel(container().sucursalRepository, container().locationProvider) }
    }
}
