package cl.duoc.electromechanics.model

import cl.duoc.electromechanics.data.local.entity.RegistroJornadaEntity
import cl.duoc.electromechanics.data.local.relation.OrdenConDetalle
import cl.duoc.electromechanics.domain.rules.JornadaRules

data class MarcacionPendiente(
    val tipo: TipoMarcacion,
    val latitud: Double,
    val longitud: Double,
    val precisionMetros: Float,
    val simulada: Boolean,
    val sucursalCercana: String?,
    val distanciaMetros: Double?,
    val fotoPath: String? = null
) {
    val dentroDeRadio: Boolean get() = JornadaRules.dentroDeRadio(distanciaMetros)
    val precisionBaja: Boolean get() = !JornadaRules.precisionAceptable(precisionMetros)
}

data class TecnicoHomeModel(
    val nombre: String = "",
    val cargando: Boolean = true,
    val ultimaMarcacion: RegistroJornadaEntity? = null,
    val hojaDeRuta: List<OrdenConDetalle> = emptyList(),
    val capturandoGps: Boolean = false,
    val guardandoMarcacion: Boolean = false,
    val marcacionPendiente: MarcacionPendiente? = null,
    val gpsDesactivado: Boolean = false,
    val permisoDenegado: Boolean = false,
    val linternaDisponible: Boolean = false,
    val linternaEncendida: Boolean = false,
    val mensaje: String? = null
) {
    private val ultimoTipo: TipoMarcacion?
        get() = ultimaMarcacion?.let { TipoMarcacion.from(it.tipoMarcacion) }

    val jornadaActiva: Boolean get() = JornadaRules.jornadaActiva(ultimoTipo)
    val siguienteMarcacion: TipoMarcacion get() = JornadaRules.siguienteMarcacion(ultimoTipo)
}
