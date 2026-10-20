package cl.duoc.electromechanics.model

import cl.duoc.electromechanics.data.local.entity.EmpleadoEntity
import cl.duoc.electromechanics.data.local.entity.SucursalEntity
import cl.duoc.electromechanics.data.local.relation.OrdenConDetalle
import cl.duoc.electromechanics.data.local.relation.RegistroConEmpleado
import cl.duoc.electromechanics.domain.util.DateUtils
import java.time.LocalDate

data class ResumenHoras(
    val nombre: String,
    val horasMillis: Long,
    val marcaciones: Int,
    val fueraDeRadio: Int,
    val enJornada: Boolean
)

data class SupervisorModel(
    val nombre: String = "",
    val ordenes: List<OrdenConDetalle> = emptyList(),
    val filtro: EstadoServicio? = null,
    val registros: List<RegistroConEmpleado> = emptyList(),
    val resumen: List<ResumenHoras> = emptyList(),
    val mensaje: String? = null
) {
    val ordenesFiltradas: List<OrdenConDetalle>
        get() = filtro?.let { f -> ordenes.filter { EstadoServicio.from(it.orden.estado) == f } } ?: ordenes

    fun contar(estado: EstadoServicio) = ordenes.count { EstadoServicio.from(it.orden.estado) == estado }

    val porAprobar: Int
        get() = ordenes.count { EstadoServicio.from(it.orden.estado) == EstadoServicio.FINALIZADO && !it.orden.aprobada } +
            registros.count { !it.registro.aprobado }
}

data class OrdenFormModel(
    val sucursales: List<SucursalEntity> = emptyList(),
    val tecnicos: List<EmpleadoEntity> = emptyList(),
    val sucursal: SucursalEntity? = null,
    val sucursalError: String? = null,
    val tecnico: EmpleadoEntity? = null,
    val tecnicoError: String? = null,
    val fecha: FieldState = FieldState(DateUtils.formatFecha(LocalDate.now())),
    val prioridad: Prioridad = Prioridad.NORMAL,
    val descripcion: FieldState = FieldState(),
    val actividades: FieldState = FieldState(),
    val guardando: Boolean = false,
    val shakeTrigger: Int = 0,
    val mensaje: String? = null
)
