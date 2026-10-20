package cl.duoc.electromechanics.data.local.entity

data class EmpleadoEntity(
    val idEmpleado: String,
    val rut: String,
    val nombreCompleto: String,
    val rol: String,
    val especialidadTecnica: String,
    val correoCorporativo: String,
    val passwordHash: String,
    val activo: Boolean = true
)

data class SucursalEntity(
    val idSucursal: String,
    val nombreCliente: String,
    val direccionSucursal: String,
    val latitudRef: Double,
    val longitudRef: Double
)

data class OrdenTrabajoEntity(
    val idServicio: String,
    val idEmpleadoAsignado: String,
    val idSucursalCliente: String,
    val fechaProgramada: Long,
    val estado: String,
    val descripcionTrabajo: String,
    val actividadesRequeridas: String,
    val prioridad: String,
    val fotoInicialPath: String? = null,
    val fotoFinalPath: String? = null,
    val horaInicio: Long? = null,
    val horaTermino: Long? = null,
    val aprobada: Boolean = false,
    val creadaEn: Long
)

data class ActividadEntity(
    val idActividad: String,
    val idServicio: String,
    val detalleTareaEjecutada: String,
    val repuestosUtilizados: String,
    val estadoEquipo: String,
    val horaRegistro: Long,
    val comentariosTecnicos: String
)

data class RegistroJornadaEntity(
    val idRegistro: String,
    val idEmpleado: String,
    val tipoMarcacion: String,
    val fechaHoraMarcacion: Long,
    val latitudMarcacion: Double,
    val longitudMarcacion: Double,
    val precisionMetros: Float,
    val fotoRespaldoPath: String? = null,
    val idSucursalCercana: String? = null,
    val distanciaMetros: Double? = null,
    val dentroDeRadio: Boolean = false,
    val ubicacionSimulada: Boolean = false,
    val aprobado: Boolean = false
)
