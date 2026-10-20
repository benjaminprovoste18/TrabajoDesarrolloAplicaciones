package cl.duoc.electromechanics.data.repository

import cl.duoc.electromechanics.data.local.dao.ActividadDao
import cl.duoc.electromechanics.data.local.dao.EmpleadoDao
import cl.duoc.electromechanics.data.local.dao.OrdenTrabajoDao
import cl.duoc.electromechanics.data.local.dao.RegistroJornadaDao
import cl.duoc.electromechanics.data.local.dao.SucursalDao
import cl.duoc.electromechanics.data.local.entity.ActividadEntity
import cl.duoc.electromechanics.data.local.entity.EmpleadoEntity
import cl.duoc.electromechanics.data.local.entity.OrdenTrabajoEntity
import cl.duoc.electromechanics.data.local.entity.RegistroJornadaEntity
import cl.duoc.electromechanics.data.local.entity.SucursalEntity
import cl.duoc.electromechanics.data.local.relation.OrdenConDetalle
import cl.duoc.electromechanics.data.local.relation.RegistroConEmpleado
import cl.duoc.electromechanics.data.session.Sesion
import cl.duoc.electromechanics.data.session.SessionManager
import cl.duoc.electromechanics.domain.rules.JornadaRules
import cl.duoc.electromechanics.domain.util.DateUtils
import cl.duoc.electromechanics.domain.util.GeoUtils
import cl.duoc.electromechanics.domain.util.PasswordHasher
import cl.duoc.electromechanics.domain.validation.RutValidator
import cl.duoc.electromechanics.model.EstadoServicio
import cl.duoc.electromechanics.model.Prioridad
import cl.duoc.electromechanics.model.Rol
import cl.duoc.electromechanics.model.TipoMarcacion
import kotlinx.coroutines.flow.Flow
import java.util.UUID

sealed interface LoginResult {
    data class Success(val sesion: Sesion) : LoginResult
    data object InvalidCredentials : LoginResult
    data object InactiveUser : LoginResult
}

class AuthRepository(
    private val empleadoDao: EmpleadoDao,
    private val session: SessionManager
) {
    suspend fun login(rut: String, password: String): LoginResult {
        val rutFormateado = RutValidator.format(rut)
        val empleado = empleadoDao.findByRut(rutFormateado) ?: return LoginResult.InvalidCredentials
        val salt = RutValidator.clean(rutFormateado)
        if (!PasswordHasher.matches(password, salt, empleado.passwordHash)) return LoginResult.InvalidCredentials
        if (!empleado.activo) return LoginResult.InactiveUser
        val rol = Rol.from(empleado.rol) ?: return LoginResult.InvalidCredentials
        val sesion = Sesion(empleado.idEmpleado, rol, empleado.nombreCompleto)
        session.guardar(sesion)
        return LoginResult.Success(sesion)
    }

    fun sesionActual(): Sesion? = session.actual()

    fun logout() = session.cerrar()
}

class EmpleadoRepository(private val dao: EmpleadoDao) {
    fun observeAll(): Flow<List<EmpleadoEntity>> = dao.observeAll()
    fun observeTecnicosActivos(): Flow<List<EmpleadoEntity>> = dao.observeTecnicosActivos()

    suspend fun existeRut(rut: String): Boolean = dao.findByRut(RutValidator.format(rut)) != null
    suspend fun existeCorreo(correo: String): Boolean = dao.countByCorreo(correo.trim()) > 0

    suspend fun crear(
        rut: String,
        nombre: String,
        correo: String,
        rol: Rol,
        especialidad: String,
        password: String
    ) {
        val rutFormateado = RutValidator.format(rut)
        dao.insert(
            EmpleadoEntity(
                idEmpleado = UUID.randomUUID().toString(),
                rut = rutFormateado,
                nombreCompleto = nombre.trim(),
                rol = rol.name,
                especialidadTecnica = especialidad,
                correoCorporativo = correo.trim().lowercase(),
                passwordHash = PasswordHasher.hash(password, RutValidator.clean(rutFormateado))
            )
        )
    }

    suspend fun setActivo(id: String, activo: Boolean) = dao.setActivo(id, activo)
}

class SucursalRepository(private val dao: SucursalDao) {
    fun observeAll(): Flow<List<SucursalEntity>> = dao.observeAll()

    suspend fun crear(nombre: String, direccion: String, lat: Double, lon: Double) {
        dao.insert(
            SucursalEntity(
                idSucursal = UUID.randomUUID().toString(),
                nombreCliente = nombre.trim(),
                direccionSucursal = direccion.trim(),
                latitudRef = lat,
                longitudRef = lon
            )
        )
    }

    suspend fun masCercana(lat: Double, lon: Double): Pair<SucursalEntity, Double>? =
        dao.getAll()
            .map { it to GeoUtils.distanceMeters(lat, lon, it.latitudRef, it.longitudRef) }
            .minByOrNull { it.second }
}

class OrdenRepository(
    private val ordenDao: OrdenTrabajoDao,
    private val actividadDao: ActividadDao
) {
    fun observeHojaDeRuta(idEmpleado: String): Flow<List<OrdenConDetalle>> =
        ordenDao.observeHojaDeRuta(idEmpleado, DateUtils.startOfToday())

    fun observeAll(): Flow<List<OrdenConDetalle>> = ordenDao.observeAll()
    fun observeById(id: String): Flow<OrdenConDetalle?> = ordenDao.observeById(id)

    suspend fun crear(
        idEmpleado: String,
        idSucursal: String,
        fechaProgramada: Long,
        descripcion: String,
        actividadesRequeridas: String,
        prioridad: Prioridad
    ): OrdenTrabajoEntity {
        val orden = OrdenTrabajoEntity(
            idServicio = UUID.randomUUID().toString(),
            idEmpleadoAsignado = idEmpleado,
            idSucursalCliente = idSucursal,
            fechaProgramada = fechaProgramada,
            estado = EstadoServicio.PENDIENTE.name,
            descripcionTrabajo = descripcion.trim(),
            actividadesRequeridas = actividadesRequeridas.trim(),
            prioridad = prioridad.name,
            creadaEn = System.currentTimeMillis()
        )
        ordenDao.insert(orden)
        return orden
    }

    suspend fun iniciar(id: String) = ordenDao.marcarEnProceso(id, System.currentTimeMillis())
    suspend fun finalizar(id: String) = ordenDao.marcarFinalizado(id, System.currentTimeMillis())
    suspend fun setFotoInicial(id: String, path: String) = ordenDao.setFotoInicial(id, path)
    suspend fun setFotoFinal(id: String, path: String) = ordenDao.setFotoFinal(id, path)
    suspend fun aprobar(id: String) = ordenDao.aprobar(id)

    suspend fun registrarActividad(
        idServicio: String,
        detalle: String,
        repuestos: String,
        estadoEquipo: String,
        comentarios: String
    ) {
        actividadDao.insert(
            ActividadEntity(
                idActividad = UUID.randomUUID().toString(),
                idServicio = idServicio,
                detalleTareaEjecutada = detalle.trim(),
                repuestosUtilizados = repuestos.trim().ifBlank { "Sin repuestos" },
                estadoEquipo = estadoEquipo,
                horaRegistro = System.currentTimeMillis(),
                comentariosTecnicos = comentarios.trim()
            )
        )
    }
}

class JornadaRepository(
    private val dao: RegistroJornadaDao,
    private val sucursalRepository: SucursalRepository
) {
    fun observeUltimaDeHoy(idEmpleado: String): Flow<RegistroJornadaEntity?> =
        dao.observeUltimaDesde(idEmpleado, DateUtils.startOfToday())

    fun observeHistorial(idEmpleado: String): Flow<List<RegistroConEmpleado>> = dao.observeByEmpleado(idEmpleado)
    fun observeAll(): Flow<List<RegistroConEmpleado>> = dao.observeAll()

    suspend fun siguienteMarcacion(idEmpleado: String): TipoMarcacion {
        val ultima = dao.ultimaDesde(idEmpleado, DateUtils.startOfToday())
        return JornadaRules.siguienteMarcacion(ultima?.let { TipoMarcacion.from(it.tipoMarcacion) })
    }

    suspend fun registrar(
        idEmpleado: String,
        tipo: TipoMarcacion,
        lat: Double,
        lon: Double,
        precision: Float,
        simulada: Boolean,
        fotoPath: String?
    ): RegistroJornadaEntity {
        val cercana = sucursalRepository.masCercana(lat, lon)
        val registro = RegistroJornadaEntity(
            idRegistro = UUID.randomUUID().toString(),
            idEmpleado = idEmpleado,
            tipoMarcacion = tipo.name,
            fechaHoraMarcacion = System.currentTimeMillis(),
            latitudMarcacion = lat,
            longitudMarcacion = lon,
            precisionMetros = precision,
            fotoRespaldoPath = fotoPath,
            idSucursalCercana = cercana?.first?.idSucursal,
            distanciaMetros = cercana?.second,
            dentroDeRadio = JornadaRules.dentroDeRadio(cercana?.second),
            ubicacionSimulada = simulada
        )
        dao.insert(registro)
        return registro
    }

    suspend fun aprobar(id: String) = dao.aprobar(id)
}
