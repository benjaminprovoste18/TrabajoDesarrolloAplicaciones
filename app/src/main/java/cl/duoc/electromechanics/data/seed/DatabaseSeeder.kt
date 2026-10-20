package cl.duoc.electromechanics.data.seed

import cl.duoc.electromechanics.data.local.dao.EmpleadoDao
import cl.duoc.electromechanics.data.local.dao.OrdenTrabajoDao
import cl.duoc.electromechanics.data.local.dao.SucursalDao
import cl.duoc.electromechanics.data.local.entity.EmpleadoEntity
import cl.duoc.electromechanics.data.local.entity.OrdenTrabajoEntity
import cl.duoc.electromechanics.data.local.entity.SucursalEntity
import cl.duoc.electromechanics.domain.util.DateUtils
import cl.duoc.electromechanics.domain.util.PasswordHasher
import cl.duoc.electromechanics.domain.validation.RutValidator
import cl.duoc.electromechanics.model.Especialidades
import cl.duoc.electromechanics.model.EstadoServicio
import cl.duoc.electromechanics.model.Prioridad
import cl.duoc.electromechanics.model.Rol
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class DatabaseSeeder(
    private val empleadoDao: EmpleadoDao,
    private val sucursalDao: SucursalDao,
    private val ordenDao: OrdenTrabajoDao
) {
    private val mutex = Mutex()

    suspend fun seedIfEmpty() = mutex.withLock {
        if (empleadoDao.count() > 0) return@withLock

        val empleados = crearEmpleados()
        val sucursales = crearSucursales()
        empleadoDao.insertAll(empleados)
        sucursalDao.insertAll(sucursales)
        ordenDao.insertAll(crearOrdenes(empleados, sucursales))
    }

    private fun empleado(
        rut: String,
        nombre: String,
        rol: Rol,
        especialidad: String,
        correo: String
    ): EmpleadoEntity {
        val rutFormateado = RutValidator.format(rut)
        return EmpleadoEntity(
            idEmpleado = UUID.randomUUID().toString(),
            rut = rutFormateado,
            nombreCompleto = nombre,
            rol = rol.name,
            especialidadTecnica = especialidad,
            correoCorporativo = correo,
            passwordHash = PasswordHasher.hash(DEMO_PASSWORD, RutValidator.clean(rutFormateado))
        )
    }

    private fun crearEmpleados(): List<EmpleadoEntity> {
        val esp = Especialidades.tecnicas
        val tecnicos = listOf(
            "33333333-3" to "Operario Alfa Demo",
            "55555555-5" to "Operario Bravo Demo",
            "66666666-6" to "Operario Charlie Demo",
            "77777777-7" to "Operario Delta Demo",
            "88888888-8" to "Operario Echo Demo",
            "99999999-9" to "Operario Foxtrot Demo",
            "10101010-4" to "Operario Golf Demo",
            "12121212-9" to "Operario Hotel Demo",
            "13131313-6" to "Operario India Demo",
            "14141414-3" to "Operario Juliet Demo",
            "15151515-0" to "Operario Kilo Demo",
            "16161616-8" to "Operario Lima Demo"
        ).mapIndexed { i, (rut, nombre) ->
            val alias = nombre.split(" ")[1].lowercase()
            empleado(rut, nombre, Rol.EMPLEADO, esp[i % esp.size], "op.$alias@emdemo.cl")
        }
        return listOf(
            empleado("11111111-1", "Administrador Sistema Demo", Rol.ADMIN, Especialidades.NO_APLICA, "admin@emdemo.cl"),
            empleado("22222222-2", "Supervisor Norte Demo", Rol.SUPERVISOR, Especialidades.NO_APLICA, "sup.norte@emdemo.cl"),
            empleado("44444444-4", "Supervisor Sur Demo", Rol.SUPERVISOR, Especialidades.NO_APLICA, "sup.sur@emdemo.cl")
        ) + tecnicos
    }

    private fun crearSucursales(): List<SucursalEntity> = listOf(
        Triple("Cliente Demo A - Planta Maipú", "Calle Ficticia 100, Maipú", -33.5110 to -70.7580),
        Triple("Cliente Demo B - Centro Logístico Pudahuel", "Av. Simulada 2200, Pudahuel", -33.4440 to -70.7700),
        Triple("Cliente Demo C - Planta Quilicura", "Pasaje Prueba 45, Quilicura", -33.3600 to -70.7300),
        Triple("Cliente Demo D - Bodega San Bernardo", "Camino Ejemplo 900, San Bernardo", -33.5920 to -70.7000),
        Triple("Cliente Demo E - Industria Renca", "Calle Modelo 310, Renca", -33.4050 to -70.7280),
        Triple("Oficina Técnica Electromechanics (demo)", "Av. Referencia 1500, Maipú", -33.4950 to -70.7570)
    ).map { (nombre, direccion, coord) ->
        SucursalEntity(
            idSucursal = UUID.randomUUID().toString(),
            nombreCliente = nombre,
            direccionSucursal = direccion,
            latitudRef = coord.first,
            longitudRef = coord.second
        )
    }

    private fun crearOrdenes(
        empleados: List<EmpleadoEntity>,
        sucursales: List<SucursalEntity>
    ): List<OrdenTrabajoEntity> {
        val hoy = DateUtils.startOfToday()
        val manana = hoy + 24 * 60 * 60 * 1000L
        val clientes = sucursales.filterNot { it.nombreCliente.startsWith("Oficina") }
        val tecnicos = empleados.filter { it.rol == Rol.EMPLEADO.name }
        val trabajos = listOf(
            "Mantención preventiva de tablero eléctrico general" to "Inspección termográfica, reapriete de conexiones, limpieza de barras",
            "Reparación correctiva de motor de correa transportadora" to "Diagnóstico de rodamientos, cambio de repuestos, prueba en vacío",
            "Revisión de sistema de iluminación de nave industrial" to "Medición de luminarias, reemplazo de equipos defectuosos",
            "Mantención de compresor de aire" to "Cambio de filtros y aceite, revisión de válvulas de seguridad",
            "Calibración de sensores de temperatura" to "Contraste con patrón, ajuste de transmisores, registro de valores"
        )
        val ordenes = mutableListOf<OrdenTrabajoEntity>()
        var n = 0L
        tecnicos.forEachIndexed { i, tecnico ->
            val cantidad = if (i == 0) 3 else 1
            repeat(cantidad) { j ->
                val trabajo = trabajos[(i + j) % trabajos.size]
                ordenes += OrdenTrabajoEntity(
                    idServicio = UUID.randomUUID().toString(),
                    idEmpleadoAsignado = tecnico.idEmpleado,
                    idSucursalCliente = clientes[(i + j) % clientes.size].idSucursal,
                    fechaProgramada = if (j == 2) manana else hoy,
                    estado = EstadoServicio.PENDIENTE.name,
                    descripcionTrabajo = trabajo.first,
                    actividadesRequeridas = trabajo.second,
                    prioridad = if (j == 1) Prioridad.URGENTE.name else Prioridad.NORMAL.name,
                    creadaEn = System.currentTimeMillis() + n++
                )
            }
        }
        return ordenes
    }

    companion object {
        const val DEMO_PASSWORD = "Demo2026"
    }
}
