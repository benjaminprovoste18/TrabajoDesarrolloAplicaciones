package cl.duoc.electromechanics.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.electromechanics.model.Rol
import cl.duoc.electromechanics.ui.screen.ActividadFormScreen
import cl.duoc.electromechanics.ui.screen.AdminHomeScreen
import cl.duoc.electromechanics.ui.screen.HistorialJornadaScreen
import cl.duoc.electromechanics.ui.screen.LoginScreen
import cl.duoc.electromechanics.ui.screen.OrdenFormScreen
import cl.duoc.electromechanics.ui.screen.ServicioDetalleScreen
import cl.duoc.electromechanics.ui.screen.SplashScreen
import cl.duoc.electromechanics.ui.screen.SucursalFormScreen
import cl.duoc.electromechanics.ui.screen.SupervisorHomeScreen
import cl.duoc.electromechanics.ui.screen.TecnicoHomeScreen
import cl.duoc.electromechanics.ui.screen.UsuarioFormScreen

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val TECNICO_HOME = "tecnico"
    const val TECNICO_HISTORIAL = "tecnico/historial"
    const val SERVICIO_DETALLE = "servicio/{idServicio}"
    const val ACTIVIDAD_FORM = "servicio/{idServicio}/actividad"
    const val SUPERVISOR_HOME = "supervisor"
    const val ORDEN_FORM = "supervisor/orden/nueva"
    const val ADMIN_HOME = "admin"
    const val USUARIO_FORM = "admin/usuario/nuevo"
    const val SUCURSAL_FORM = "admin/sucursal/nueva"

    const val ARG_SERVICIO = "idServicio"

    fun servicioDetalle(id: String) = "servicio/$id"
    fun actividadForm(id: String) = "servicio/$id/actividad"

    fun homeFor(rol: Rol): String = when (rol) {
        Rol.EMPLEADO -> TECNICO_HOME
        Rol.SUPERVISOR -> SUPERVISOR_HOME
        Rol.ADMIN -> ADMIN_HOME
    }
}

fun NavHostController.navigateClearing(route: String) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavHostController.safePop() {
    if (previousBackStackEntry != null) popBackStack()
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val duration = 320
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(duration)) + fadeIn(tween(duration))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(duration)) + fadeOut(tween(duration))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(duration)) + fadeIn(tween(duration))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(duration)) + fadeOut(tween(duration))
        }
    ) {
        composable(
            Routes.SPLASH,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut(tween(400)) }
        ) {
            SplashScreen(onReady = { route -> navController.navigateClearing(route) })
        }

        composable(Routes.LOGIN, enterTransition = { fadeIn(tween(500)) }) {
            LoginScreen(onLoggedIn = { rol -> navController.navigateClearing(Routes.homeFor(rol)) })
        }

        composable(Routes.TECNICO_HOME) {
            TecnicoHomeScreen(
                onOpenServicio = { id -> navController.navigate(Routes.servicioDetalle(id)) },
                onOpenHistorial = { navController.navigate(Routes.TECNICO_HISTORIAL) },
                onLogout = { navController.navigateClearing(Routes.LOGIN) }
            )
        }
        composable(Routes.TECNICO_HISTORIAL) {
            HistorialJornadaScreen(onBack = { navController.safePop() })
        }
        composable(
            Routes.SERVICIO_DETALLE,
            arguments = listOf(navArgument(Routes.ARG_SERVICIO) { type = NavType.StringType })
        ) {
            ServicioDetalleScreen(
                onBack = { navController.safePop() },
                onRegistrarActividad = { id -> navController.navigate(Routes.actividadForm(id)) }
            )
        }
        composable(
            Routes.ACTIVIDAD_FORM,
            arguments = listOf(navArgument(Routes.ARG_SERVICIO) { type = NavType.StringType })
        ) {
            ActividadFormScreen(onBack = { navController.safePop() })
        }

        composable(Routes.SUPERVISOR_HOME) {
            SupervisorHomeScreen(
                onNuevaOrden = { navController.navigate(Routes.ORDEN_FORM) },
                onOpenServicio = { id -> navController.navigate(Routes.servicioDetalle(id)) },
                onLogout = { navController.navigateClearing(Routes.LOGIN) }
            )
        }
        composable(Routes.ORDEN_FORM) {
            OrdenFormScreen(onBack = { navController.safePop() })
        }

        composable(Routes.ADMIN_HOME) {
            AdminHomeScreen(
                onNuevoUsuario = { navController.navigate(Routes.USUARIO_FORM) },
                onNuevaSucursal = { navController.navigate(Routes.SUCURSAL_FORM) },
                onLogout = { navController.navigateClearing(Routes.LOGIN) }
            )
        }
        composable(Routes.USUARIO_FORM) {
            UsuarioFormScreen(onBack = { navController.safePop() })
        }
        composable(Routes.SUCURSAL_FORM) {
            SucursalFormScreen(onBack = { navController.safePop() })
        }
    }
}
