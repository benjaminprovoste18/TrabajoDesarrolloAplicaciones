package cl.duoc.electromechanics.domain.rules

import cl.duoc.electromechanics.model.EstadoServicio
import cl.duoc.electromechanics.model.TipoMarcacion

data class Requisito(val descripcion: String, val cumplido: Boolean)

object ServicioRules {
    fun requisitosParaIniciar(
        estado: EstadoServicio,
        jornadaActiva: Boolean,
        tieneFotoInicial: Boolean
    ): List<Requisito> = listOf(
        Requisito("El servicio está pendiente", estado == EstadoServicio.PENDIENTE),
        Requisito("Jornada iniciada (marcación de entrada)", jornadaActiva),
        Requisito("Foto del estado inicial del equipo", tieneFotoInicial)
    )

    fun requisitosParaFinalizar(
        estado: EstadoServicio,
        cantidadActividades: Int,
        tieneFotoFinal: Boolean
    ): List<Requisito> = listOf(
        Requisito("El servicio está en proceso", estado == EstadoServicio.EN_PROCESO),
        Requisito("Al menos una actividad registrada", cantidadActividades > 0),
        Requisito("Foto del resultado final", tieneFotoFinal)
    )

    fun puedeRegistrarActividad(estado: EstadoServicio): Boolean = estado == EstadoServicio.EN_PROCESO

    fun puedeTomarFotoInicial(estado: EstadoServicio): Boolean = estado == EstadoServicio.PENDIENTE

    fun puedeTomarFotoFinal(estado: EstadoServicio): Boolean = estado == EstadoServicio.EN_PROCESO

    fun cumpleTodos(requisitos: List<Requisito>): Boolean = requisitos.all { it.cumplido }
}

object JornadaRules {
    const val RADIO_VALIDO_METROS = 300.0

    const val PRECISION_MAXIMA_METROS = 100f

    fun siguienteMarcacion(ultimaDelDia: TipoMarcacion?): TipoMarcacion =
        if (ultimaDelDia == TipoMarcacion.ENTRADA) TipoMarcacion.SALIDA else TipoMarcacion.ENTRADA

    fun jornadaActiva(ultimaDelDia: TipoMarcacion?): Boolean = ultimaDelDia == TipoMarcacion.ENTRADA

    fun dentroDeRadio(distanciaMetros: Double?): Boolean =
        distanciaMetros != null && distanciaMetros <= RADIO_VALIDO_METROS

    fun precisionAceptable(precisionMetros: Float): Boolean = precisionMetros <= PRECISION_MAXIMA_METROS

    fun horasTrabajadas(marcaciones: List<Pair<TipoMarcacion, Long>>, ahora: Long): Long {
        var total = 0L
        var entradaAbierta: Long? = null
        for ((tipo, hora) in marcaciones) {
            when (tipo) {
                TipoMarcacion.ENTRADA -> if (entradaAbierta == null) entradaAbierta = hora
                TipoMarcacion.SALIDA -> entradaAbierta?.let {
                    total += (hora - it).coerceAtLeast(0)
                    entradaAbierta = null
                }
            }
        }
        entradaAbierta?.let { total += (ahora - it).coerceAtLeast(0) }
        return total
    }
}

object LoginRules {
    const val MAX_INTENTOS = 5
    const val BLOQUEO_MS = 30_000L

    fun debeBloquear(intentosFallidos: Int): Boolean = intentosFallidos >= MAX_INTENTOS
}
