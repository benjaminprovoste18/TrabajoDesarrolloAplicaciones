package cl.duoc.electromechanics.model

import cl.duoc.electromechanics.data.local.relation.OrdenConDetalle
import cl.duoc.electromechanics.domain.rules.Requisito
import cl.duoc.electromechanics.domain.rules.ServicioRules

data class ServicioDetalleModel(
    val cargando: Boolean = true,
    val orden: OrdenConDetalle? = null,
    val rol: Rol = Rol.EMPLEADO,
    val jornadaActiva: Boolean = false,
    val procesando: Boolean = false,
    val mensaje: String? = null
) {
    val estado: EstadoServicio
        get() = orden?.let { EstadoServicio.from(it.orden.estado) } ?: EstadoServicio.PENDIENTE

    val esTecnico: Boolean get() = rol == Rol.EMPLEADO

    val requisitosInicio: List<Requisito>
        get() = ServicioRules.requisitosParaIniciar(estado, jornadaActiva, orden?.orden?.fotoInicialPath != null)

    val requisitosFinal: List<Requisito>
        get() = ServicioRules.requisitosParaFinalizar(
            estado,
            orden?.actividades?.size ?: 0,
            orden?.orden?.fotoFinalPath != null
        )

    val puedeIniciar: Boolean get() = esTecnico && ServicioRules.cumpleTodos(requisitosInicio)
    val puedeFinalizar: Boolean get() = esTecnico && ServicioRules.cumpleTodos(requisitosFinal)
    val puedeRegistrarActividad: Boolean get() = esTecnico && ServicioRules.puedeRegistrarActividad(estado)
    val puedeFotoInicial: Boolean get() = esTecnico && ServicioRules.puedeTomarFotoInicial(estado)
    val puedeFotoFinal: Boolean get() = esTecnico && ServicioRules.puedeTomarFotoFinal(estado)
    val puedeAprobar: Boolean
        get() = rol == Rol.SUPERVISOR && estado == EstadoServicio.FINALIZADO && orden?.orden?.aprobada == false
}

data class ActividadFormModel(
    val detalle: FieldState = FieldState(),
    val repuestos: FieldState = FieldState(),
    val comentarios: FieldState = FieldState(),
    val estadoEquipo: EstadoEquipo? = null,
    val estadoEquipoError: String? = null,
    val guardando: Boolean = false,
    val shakeTrigger: Int = 0,
    val mensaje: String? = null
)
