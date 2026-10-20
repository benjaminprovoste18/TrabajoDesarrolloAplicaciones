package cl.duoc.electromechanics.data.local.relation

import cl.duoc.electromechanics.data.local.entity.ActividadEntity
import cl.duoc.electromechanics.data.local.entity.EmpleadoEntity
import cl.duoc.electromechanics.data.local.entity.OrdenTrabajoEntity
import cl.duoc.electromechanics.data.local.entity.RegistroJornadaEntity
import cl.duoc.electromechanics.data.local.entity.SucursalEntity

data class OrdenConDetalle(
    val orden: OrdenTrabajoEntity,
    val sucursal: SucursalEntity?,
    val tecnico: EmpleadoEntity?,
    val actividades: List<ActividadEntity>
)

data class RegistroConEmpleado(
    val registro: RegistroJornadaEntity,
    val empleado: EmpleadoEntity?,
    val sucursal: SucursalEntity?
)
