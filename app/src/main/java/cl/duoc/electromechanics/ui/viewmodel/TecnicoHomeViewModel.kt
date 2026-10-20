package cl.duoc.electromechanics.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.electromechanics.data.repository.AuthRepository
import cl.duoc.electromechanics.data.repository.JornadaRepository
import cl.duoc.electromechanics.data.repository.OrdenRepository
import cl.duoc.electromechanics.data.repository.SucursalRepository
import cl.duoc.electromechanics.data.session.SessionManager
import cl.duoc.electromechanics.device.GpsResult
import cl.duoc.electromechanics.device.Linterna
import cl.duoc.electromechanics.device.LocationProvider
import cl.duoc.electromechanics.device.Vibrador
import cl.duoc.electromechanics.domain.util.GeoUtils
import cl.duoc.electromechanics.model.MarcacionPendiente
import cl.duoc.electromechanics.model.TecnicoHomeModel
import kotlinx.coroutines.launch

class TecnicoHomeViewModel(
    session: SessionManager,
    ordenRepository: OrdenRepository,
    private val jornadaRepository: JornadaRepository,
    private val sucursalRepository: SucursalRepository,
    private val locationProvider: LocationProvider,
    private val vibrador: Vibrador,
    private val linterna: Linterna,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val sesion = session.actual()
    private val idEmpleado = sesion?.idEmpleado.orEmpty()

    var uiState by mutableStateOf(
        TecnicoHomeModel(nombre = sesion?.nombre.orEmpty(), linternaDisponible = linterna.disponible)
    )
        private set

    init {
        viewModelScope.launch {
            ordenRepository.observeHojaDeRuta(idEmpleado).collect { ordenes ->
                uiState = uiState.copy(hojaDeRuta = ordenes, cargando = false)
            }
        }
        viewModelScope.launch {
            jornadaRepository.observeUltimaDeHoy(idEmpleado).collect { ultima ->
                uiState = uiState.copy(ultimaMarcacion = ultima)
            }
        }
    }

    fun onMarcarClick() {
        if (uiState.capturandoGps) return
        uiState = uiState.copy(capturandoGps = true, gpsDesactivado = false, permisoDenegado = false)
        viewModelScope.launch {
            val tipo = jornadaRepository.siguienteMarcacion(idEmpleado)
            when (val gps = locationProvider.getCurrentLocation()) {
                is GpsResult.Success -> {
                    val cercana = sucursalRepository.masCercana(gps.latitud, gps.longitud)
                    uiState = uiState.copy(
                        capturandoGps = false,
                        marcacionPendiente = MarcacionPendiente(
                            tipo = tipo,
                            latitud = gps.latitud,
                            longitud = gps.longitud,
                            precisionMetros = gps.precisionMetros,
                            simulada = gps.simulada,
                            sucursalCercana = cercana?.first?.nombreCliente,
                            distanciaMetros = cercana?.second
                        )
                    )
                }
                GpsResult.GpsDisabled -> {
                    vibrador.error()
                    uiState = uiState.copy(capturandoGps = false, gpsDesactivado = true)
                }
                GpsResult.PermissionDenied -> {
                    vibrador.error()
                    uiState = uiState.copy(capturandoGps = false, permisoDenegado = true)
                }
                GpsResult.Unavailable -> {
                    vibrador.error()
                    uiState = uiState.copy(
                        capturandoGps = false,
                        mensaje = "No se pudo obtener señal GPS. Intenta al aire libre."
                    )
                }
            }
        }
    }

    fun onPermisoDenegado() {
        uiState = uiState.copy(permisoDenegado = true)
    }

    fun onCerrarAviso() {
        uiState = uiState.copy(gpsDesactivado = false, permisoDenegado = false)
    }

    fun onFotoRespaldo(path: String) {
        uiState = uiState.copy(marcacionPendiente = uiState.marcacionPendiente?.copy(fotoPath = path))
    }

    fun onCancelarMarcacion() {
        uiState = uiState.copy(marcacionPendiente = null)
    }

    fun onConfirmarMarcacion() {
        val pendiente = uiState.marcacionPendiente ?: return
        if (uiState.guardandoMarcacion) return
        uiState = uiState.copy(guardandoMarcacion = true)
        viewModelScope.launch {
            val registro = jornadaRepository.registrar(
                idEmpleado = idEmpleado,
                tipo = pendiente.tipo,
                lat = pendiente.latitud,
                lon = pendiente.longitud,
                precision = pendiente.precisionMetros,
                simulada = pendiente.simulada,
                fotoPath = pendiente.fotoPath
            )
            vibrador.confirmacion()
            val ubicacion = registro.distanciaMetros
                ?.let { " a ${GeoUtils.formatDistance(it)} de la sucursal más cercana" }
                .orEmpty()
            uiState = uiState.copy(
                guardandoMarcacion = false,
                marcacionPendiente = null,
                mensaje = "${pendiente.tipo.etiqueta} registrada$ubicacion"
            )
        }
    }

    fun onLinternaClick() {
        val encender = !uiState.linternaEncendida
        uiState = if (linterna.cambiar(encender)) {
            uiState.copy(linternaEncendida = encender)
        } else {
            uiState.copy(mensaje = "No se pudo usar la linterna en este equipo")
        }
    }

    fun onMensajeMostrado() {
        uiState = uiState.copy(mensaje = null)
    }

    fun onCerrarSesion(onLogout: () -> Unit) {
        if (uiState.linternaEncendida) linterna.cambiar(false)
        authRepository.logout()
        onLogout()
    }

    override fun onCleared() {
        if (uiState.linternaEncendida) linterna.cambiar(false)
        super.onCleared()
    }
}
