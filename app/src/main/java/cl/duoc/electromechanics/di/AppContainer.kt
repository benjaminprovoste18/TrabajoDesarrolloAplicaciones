package cl.duoc.electromechanics.di

import android.content.Context
import cl.duoc.electromechanics.data.local.AppDatabase
import cl.duoc.electromechanics.data.local.dao.ActividadDao
import cl.duoc.electromechanics.data.local.dao.EmpleadoDao
import cl.duoc.electromechanics.data.local.dao.OrdenTrabajoDao
import cl.duoc.electromechanics.data.local.dao.RegistroJornadaDao
import cl.duoc.electromechanics.data.local.dao.SucursalDao
import cl.duoc.electromechanics.data.repository.AuthRepository
import cl.duoc.electromechanics.data.repository.EmpleadoRepository
import cl.duoc.electromechanics.data.repository.JornadaRepository
import cl.duoc.electromechanics.data.repository.OrdenRepository
import cl.duoc.electromechanics.data.repository.SucursalRepository
import cl.duoc.electromechanics.data.seed.DatabaseSeeder
import cl.duoc.electromechanics.data.session.SessionManager
import cl.duoc.electromechanics.device.Linterna
import cl.duoc.electromechanics.device.LocationProvider
import cl.duoc.electromechanics.device.NotificationHelper
import cl.duoc.electromechanics.device.Vibrador

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database = AppDatabase.getInstance(appContext)

    private val empleadoDao = EmpleadoDao(database)
    private val sucursalDao = SucursalDao(database)
    private val actividadDao = ActividadDao(database)
    private val ordenDao = OrdenTrabajoDao(database, empleadoDao, sucursalDao, actividadDao)
    private val jornadaDao = RegistroJornadaDao(database, empleadoDao, sucursalDao)

    val session = SessionManager(appContext)
    val seeder = DatabaseSeeder(empleadoDao, sucursalDao, ordenDao)

    val authRepository = AuthRepository(empleadoDao, session)
    val empleadoRepository = EmpleadoRepository(empleadoDao)
    val sucursalRepository = SucursalRepository(sucursalDao)
    val ordenRepository = OrdenRepository(ordenDao, actividadDao)
    val jornadaRepository = JornadaRepository(jornadaDao, sucursalRepository)

    val locationProvider = LocationProvider(appContext)
    val notificationHelper = NotificationHelper(appContext)
    val vibrador = Vibrador(appContext)
    val linterna = Linterna(appContext)
}
