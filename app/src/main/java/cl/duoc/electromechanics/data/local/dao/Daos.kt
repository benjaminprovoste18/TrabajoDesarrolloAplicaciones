package cl.duoc.electromechanics.data.local.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import cl.duoc.electromechanics.data.local.AppDatabase
import cl.duoc.electromechanics.data.local.bool
import cl.duoc.electromechanics.data.local.double
import cl.duoc.electromechanics.data.local.doubleOrNull
import cl.duoc.electromechanics.data.local.entity.ActividadEntity
import cl.duoc.electromechanics.data.local.entity.EmpleadoEntity
import cl.duoc.electromechanics.data.local.entity.OrdenTrabajoEntity
import cl.duoc.electromechanics.data.local.entity.RegistroJornadaEntity
import cl.duoc.electromechanics.data.local.entity.SucursalEntity
import cl.duoc.electromechanics.data.local.float
import cl.duoc.electromechanics.data.local.long
import cl.duoc.electromechanics.data.local.longOrNull
import cl.duoc.electromechanics.data.local.mapRows
import cl.duoc.electromechanics.data.local.relation.OrdenConDetalle
import cl.duoc.electromechanics.data.local.relation.RegistroConEmpleado
import cl.duoc.electromechanics.data.local.str
import cl.duoc.electromechanics.data.local.strOrNull
import kotlinx.coroutines.flow.Flow

class EmpleadoDao(private val database: AppDatabase) {
    fun mapear(c: Cursor) = EmpleadoEntity(
        idEmpleado = c.str("id_empleado"),
        rut = c.str("rut_trabajador"),
        nombreCompleto = c.str("nombre_completo"),
        rol = c.str("rol"),
        especialidadTecnica = c.str("especialidad_tecnica"),
        correoCorporativo = c.str("correo_corporativo"),
        passwordHash = c.str("password_hash"),
        activo = c.bool("activo")
    )

    private fun valores(e: EmpleadoEntity) = ContentValues().apply {
        put("id_empleado", e.idEmpleado)
        put("rut_trabajador", e.rut)
        put("nombre_completo", e.nombreCompleto)
        put("rol", e.rol)
        put("especialidad_tecnica", e.especialidadTecnica)
        put("correo_corporativo", e.correoCorporativo)
        put("password_hash", e.passwordHash)
        put("activo", if (e.activo) 1 else 0)
    }

    fun todos(db: SQLiteDatabase): List<EmpleadoEntity> =
        db.rawQuery("SELECT * FROM empleado", null).mapRows(::mapear)

    suspend fun findByRut(rut: String): EmpleadoEntity? = database.leer { db ->
        db.rawQuery("SELECT * FROM empleado WHERE rut_trabajador = ? LIMIT 1", arrayOf(rut))
            .mapRows(::mapear).firstOrNull()
    }

    suspend fun countByCorreo(correo: String): Int = database.leer { db ->
        db.rawQuery("SELECT COUNT(*) FROM empleado WHERE lower(correo_corporativo) = lower(?)", arrayOf(correo))
            .mapRows { it.getInt(0) }.first()
    }

    fun observeAll(): Flow<List<EmpleadoEntity>> = database.observar { db ->
        db.rawQuery("SELECT * FROM empleado ORDER BY rol, nombre_completo", null).mapRows(::mapear)
    }

    fun observeTecnicosActivos(): Flow<List<EmpleadoEntity>> = database.observar { db ->
        db.rawQuery(
            "SELECT * FROM empleado WHERE rol = 'EMPLEADO' AND activo = 1 ORDER BY nombre_completo",
            null
        ).mapRows(::mapear)
    }

    suspend fun setActivo(id: String, activo: Boolean) {
        database.escribir { db ->
            val v = ContentValues().apply { put("activo", if (activo) 1 else 0) }
            db.update("empleado", v, "id_empleado = ?", arrayOf(id))
        }
    }

    suspend fun insert(empleado: EmpleadoEntity) {
        database.escribir { db -> db.insertOrThrow("empleado", null, valores(empleado)) }
    }

    suspend fun insertAll(empleados: List<EmpleadoEntity>) {
        database.escribir { db ->
            empleados.forEach {
                db.insertWithOnConflict("empleado", null, valores(it), SQLiteDatabase.CONFLICT_REPLACE)
            }
        }
    }

    suspend fun count(): Int = database.leer { db ->
        db.rawQuery("SELECT COUNT(*) FROM empleado", null).mapRows { it.getInt(0) }.first()
    }
}

class SucursalDao(private val database: AppDatabase) {
    fun mapear(c: Cursor) = SucursalEntity(
        idSucursal = c.str("id_sucursal"),
        nombreCliente = c.str("nombre_cliente"),
        direccionSucursal = c.str("direccion_sucursal"),
        latitudRef = c.double("latitud_ref"),
        longitudRef = c.double("longitud_ref")
    )

    private fun valores(s: SucursalEntity) = ContentValues().apply {
        put("id_sucursal", s.idSucursal)
        put("nombre_cliente", s.nombreCliente)
        put("direccion_sucursal", s.direccionSucursal)
        put("latitud_ref", s.latitudRef)
        put("longitud_ref", s.longitudRef)
    }

    fun todas(db: SQLiteDatabase): List<SucursalEntity> =
        db.rawQuery("SELECT * FROM sucursal ORDER BY nombre_cliente", null).mapRows(::mapear)

    fun observeAll(): Flow<List<SucursalEntity>> = database.observar { db -> todas(db) }

    suspend fun getAll(): List<SucursalEntity> = database.leer { db -> todas(db) }

    suspend fun insert(sucursal: SucursalEntity) {
        database.escribir { db -> db.insertOrThrow("sucursal", null, valores(sucursal)) }
    }

    suspend fun insertAll(sucursales: List<SucursalEntity>) {
        database.escribir { db ->
            sucursales.forEach {
                db.insertWithOnConflict("sucursal", null, valores(it), SQLiteDatabase.CONFLICT_REPLACE)
            }
        }
    }
}

class ActividadDao(private val database: AppDatabase) {
    fun mapear(c: Cursor) = ActividadEntity(
        idActividad = c.str("id_actividad"),
        idServicio = c.str("id_servicio"),
        detalleTareaEjecutada = c.str("detalle_tarea_ejecutada"),
        repuestosUtilizados = c.str("repuestos_utilizados"),
        estadoEquipo = c.str("estado_equipo"),
        horaRegistro = c.long("hora_registro"),
        comentariosTecnicos = c.str("comentarios_tecnicos")
    )

    fun porServicios(db: SQLiteDatabase, ids: List<String>): Map<String, List<ActividadEntity>> {
        if (ids.isEmpty()) return emptyMap()
        val marcadores = ids.joinToString(",") { "?" }
        return db.rawQuery(
            "SELECT * FROM registro_actividad WHERE id_servicio IN ($marcadores) ORDER BY hora_registro DESC",
            ids.toTypedArray()
        ).mapRows(::mapear).groupBy { it.idServicio }
    }

    suspend fun insert(actividad: ActividadEntity) {
        database.escribir { db ->
            db.insertOrThrow("registro_actividad", null, ContentValues().apply {
                put("id_actividad", actividad.idActividad)
                put("id_servicio", actividad.idServicio)
                put("detalle_tarea_ejecutada", actividad.detalleTareaEjecutada)
                put("repuestos_utilizados", actividad.repuestosUtilizados)
                put("estado_equipo", actividad.estadoEquipo)
                put("hora_registro", actividad.horaRegistro)
                put("comentarios_tecnicos", actividad.comentariosTecnicos)
            })
        }
    }

    suspend fun countByServicio(idServicio: String): Int = database.leer { db ->
        db.rawQuery("SELECT COUNT(*) FROM registro_actividad WHERE id_servicio = ?", arrayOf(idServicio))
            .mapRows { it.getInt(0) }.first()
    }
}

class OrdenTrabajoDao(
    private val database: AppDatabase,
    private val empleadoDao: EmpleadoDao,
    private val sucursalDao: SucursalDao,
    private val actividadDao: ActividadDao
) {
    private fun mapear(c: Cursor) = OrdenTrabajoEntity(
        idServicio = c.str("id_servicio"),
        idEmpleadoAsignado = c.str("id_empleado_asignado"),
        idSucursalCliente = c.str("id_sucursal_cliente"),
        fechaProgramada = c.long("fecha_programada"),
        estado = c.str("estado"),
        descripcionTrabajo = c.str("descripcion_trabajo"),
        actividadesRequeridas = c.str("actividades_requeridas"),
        prioridad = c.str("prioridad"),
        fotoInicialPath = c.strOrNull("foto_inicial_path"),
        fotoFinalPath = c.strOrNull("foto_final_path"),
        horaInicio = c.longOrNull("hora_inicio"),
        horaTermino = c.longOrNull("hora_termino"),
        aprobada = c.bool("aprobada"),
        creadaEn = c.long("creada_en")
    )

    private fun valores(o: OrdenTrabajoEntity) = ContentValues().apply {
        put("id_servicio", o.idServicio)
        put("id_empleado_asignado", o.idEmpleadoAsignado)
        put("id_sucursal_cliente", o.idSucursalCliente)
        put("fecha_programada", o.fechaProgramada)
        put("estado", o.estado)
        put("descripcion_trabajo", o.descripcionTrabajo)
        put("actividades_requeridas", o.actividadesRequeridas)
        put("prioridad", o.prioridad)
        put("foto_inicial_path", o.fotoInicialPath)
        put("foto_final_path", o.fotoFinalPath)
        put("hora_inicio", o.horaInicio)
        put("hora_termino", o.horaTermino)
        put("aprobada", if (o.aprobada) 1 else 0)
        put("creada_en", o.creadaEn)
    }

    private fun conDetalle(db: SQLiteDatabase, ordenes: List<OrdenTrabajoEntity>): List<OrdenConDetalle> {
        if (ordenes.isEmpty()) return emptyList()
        val sucursales = sucursalDao.todas(db).associateBy { it.idSucursal }
        val empleados = empleadoDao.todos(db).associateBy { it.idEmpleado }
        val actividades = actividadDao.porServicios(db, ordenes.map { it.idServicio })
        return ordenes.map { o ->
            OrdenConDetalle(
                orden = o,
                sucursal = sucursales[o.idSucursalCliente],
                tecnico = empleados[o.idEmpleadoAsignado],
                actividades = actividades[o.idServicio].orEmpty()
            )
        }
    }

    fun observeHojaDeRuta(idEmpleado: String, desde: Long): Flow<List<OrdenConDetalle>> =
        database.observar { db ->
            val ordenes = db.rawQuery(
                """
                SELECT * FROM orden_trabajo
                WHERE id_empleado_asignado = ?
                  AND (estado != 'FINALIZADO' OR fecha_programada >= ?)
                ORDER BY fecha_programada ASC,
                         CASE prioridad WHEN 'URGENTE' THEN 0 ELSE 1 END,
                         creada_en ASC
                """.trimIndent(),
                arrayOf(idEmpleado, desde.toString())
            ).mapRows(::mapear)
            conDetalle(db, ordenes)
        }

    fun observeAll(): Flow<List<OrdenConDetalle>> = database.observar { db ->
        val ordenes = db.rawQuery(
            "SELECT * FROM orden_trabajo ORDER BY fecha_programada DESC, creada_en DESC",
            null
        ).mapRows(::mapear)
        conDetalle(db, ordenes)
    }

    fun observeById(id: String): Flow<OrdenConDetalle?> = database.observar { db ->
        val ordenes = db.rawQuery("SELECT * FROM orden_trabajo WHERE id_servicio = ? LIMIT 1", arrayOf(id))
            .mapRows(::mapear)
        conDetalle(db, ordenes).firstOrNull()
    }

    suspend fun insert(orden: OrdenTrabajoEntity) {
        database.escribir { db -> db.insertOrThrow("orden_trabajo", null, valores(orden)) }
    }

    suspend fun insertAll(ordenes: List<OrdenTrabajoEntity>) {
        database.escribir { db ->
            ordenes.forEach {
                db.insertWithOnConflict("orden_trabajo", null, valores(it), SQLiteDatabase.CONFLICT_REPLACE)
            }
        }
    }

    private suspend fun actualizar(id: String, valores: ContentValues) {
        database.escribir { db -> db.update("orden_trabajo", valores, "id_servicio = ?", arrayOf(id)) }
    }

    suspend fun marcarEnProceso(id: String, hora: Long) = actualizar(id, ContentValues().apply {
        put("estado", "EN_PROCESO")
        put("hora_inicio", hora)
    })

    suspend fun marcarFinalizado(id: String, hora: Long) = actualizar(id, ContentValues().apply {
        put("estado", "FINALIZADO")
        put("hora_termino", hora)
    })

    suspend fun setFotoInicial(id: String, path: String) =
        actualizar(id, ContentValues().apply { put("foto_inicial_path", path) })

    suspend fun setFotoFinal(id: String, path: String) =
        actualizar(id, ContentValues().apply { put("foto_final_path", path) })

    suspend fun aprobar(id: String) = actualizar(id, ContentValues().apply { put("aprobada", 1) })
}

class RegistroJornadaDao(
    private val database: AppDatabase,
    private val empleadoDao: EmpleadoDao,
    private val sucursalDao: SucursalDao
) {
    private fun mapear(c: Cursor) = RegistroJornadaEntity(
        idRegistro = c.str("id_registro"),
        idEmpleado = c.str("id_empleado"),
        tipoMarcacion = c.str("tipo_marcacion"),
        fechaHoraMarcacion = c.long("fecha_hora_marcacion"),
        latitudMarcacion = c.double("latitud_marcacion"),
        longitudMarcacion = c.double("longitud_marcacion"),
        precisionMetros = c.float("precision_metros"),
        fotoRespaldoPath = c.strOrNull("foto_respaldo_path"),
        idSucursalCercana = c.strOrNull("id_sucursal_cercana"),
        distanciaMetros = c.doubleOrNull("distancia_metros"),
        dentroDeRadio = c.bool("dentro_de_radio"),
        ubicacionSimulada = c.bool("ubicacion_simulada"),
        aprobado = c.bool("aprobado")
    )

    private fun conEmpleado(db: SQLiteDatabase, registros: List<RegistroJornadaEntity>): List<RegistroConEmpleado> {
        if (registros.isEmpty()) return emptyList()
        val empleados = empleadoDao.todos(db).associateBy { it.idEmpleado }
        val sucursales = sucursalDao.todas(db).associateBy { it.idSucursal }
        return registros.map {
            RegistroConEmpleado(it, empleados[it.idEmpleado], it.idSucursalCercana?.let { id -> sucursales[id] })
        }
    }

    private fun ultimaDesde(db: SQLiteDatabase, idEmpleado: String, desde: Long): RegistroJornadaEntity? =
        db.rawQuery(
            """
            SELECT * FROM registro_jornada
            WHERE id_empleado = ? AND fecha_hora_marcacion >= ?
            ORDER BY fecha_hora_marcacion DESC LIMIT 1
            """.trimIndent(),
            arrayOf(idEmpleado, desde.toString())
        ).mapRows(::mapear).firstOrNull()

    suspend fun insert(registro: RegistroJornadaEntity) {
        database.escribir { db ->
            db.insertOrThrow("registro_jornada", null, ContentValues().apply {
                put("id_registro", registro.idRegistro)
                put("id_empleado", registro.idEmpleado)
                put("tipo_marcacion", registro.tipoMarcacion)
                put("fecha_hora_marcacion", registro.fechaHoraMarcacion)
                put("latitud_marcacion", registro.latitudMarcacion)
                put("longitud_marcacion", registro.longitudMarcacion)
                put("precision_metros", registro.precisionMetros)
                put("foto_respaldo_path", registro.fotoRespaldoPath)
                put("id_sucursal_cercana", registro.idSucursalCercana)
                put("distancia_metros", registro.distanciaMetros)
                put("dentro_de_radio", if (registro.dentroDeRadio) 1 else 0)
                put("ubicacion_simulada", if (registro.ubicacionSimulada) 1 else 0)
                put("aprobado", if (registro.aprobado) 1 else 0)
            })
        }
    }

    fun observeByEmpleado(idEmpleado: String): Flow<List<RegistroConEmpleado>> = database.observar { db ->
        val registros = db.rawQuery(
            "SELECT * FROM registro_jornada WHERE id_empleado = ? ORDER BY fecha_hora_marcacion DESC",
            arrayOf(idEmpleado)
        ).mapRows(::mapear)
        conEmpleado(db, registros)
    }

    fun observeUltimaDesde(idEmpleado: String, desde: Long): Flow<RegistroJornadaEntity?> =
        database.observar { db -> ultimaDesde(db, idEmpleado, desde) }

    suspend fun ultimaDesde(idEmpleado: String, desde: Long): RegistroJornadaEntity? =
        database.leer { db -> ultimaDesde(db, idEmpleado, desde) }

    fun observeAll(): Flow<List<RegistroConEmpleado>> = database.observar { db ->
        val registros = db.rawQuery("SELECT * FROM registro_jornada ORDER BY fecha_hora_marcacion DESC", null)
            .mapRows(::mapear)
        conEmpleado(db, registros)
    }

    suspend fun aprobar(id: String) {
        database.escribir { db ->
            db.update("registro_jornada", ContentValues().apply { put("aprobado", 1) }, "id_registro = ?", arrayOf(id))
        }
    }
}
