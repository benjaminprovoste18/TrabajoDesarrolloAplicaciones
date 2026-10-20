@file:OptIn(ExperimentalMaterial3Api::class)

package cl.duoc.electromechanics.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.electromechanics.data.local.entity.EmpleadoEntity
import cl.duoc.electromechanics.data.local.entity.SucursalEntity
import cl.duoc.electromechanics.domain.util.GeoUtils
import cl.duoc.electromechanics.model.Rol
import cl.duoc.electromechanics.ui.components.MensajeToast
import cl.duoc.electromechanics.ui.components.StatusPill
import cl.duoc.electromechanics.ui.theme.InfoBlue
import cl.duoc.electromechanics.ui.theme.InfoContainer
import cl.duoc.electromechanics.ui.theme.SuccessContainer
import cl.duoc.electromechanics.ui.theme.SuccessGreen
import cl.duoc.electromechanics.ui.theme.WarningContainer
import cl.duoc.electromechanics.ui.theme.WarningOrange
import cl.duoc.electromechanics.ui.viewmodel.AdminViewModel
import cl.duoc.electromechanics.ui.viewmodel.AppViewModelProvider

@Composable
fun AdminHomeScreen(
    onNuevoUsuario: () -> Unit,
    onNuevaSucursal: () -> Unit,
    onLogout: () -> Unit,
    viewModel: AdminViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state = viewModel.uiState
    val usuarios = state.usuarios
    val sucursales = state.sucursales
    var tab by rememberSaveable { mutableIntStateOf(0) }

    MensajeToast(state.mensaje) { viewModel.onMensajeMostrado() }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text("Administración del sistema")
                            Text(state.nombre, style = MaterialTheme.typography.bodySmall)
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.onCerrarSesion(onLogout) }) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                TabRow(selectedTabIndex = tab) {
                    Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Usuarios (${usuarios.size})") }, icon = { Icon(Icons.Filled.Group, null) })
                    Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Sucursales (${sucursales.size})") }, icon = { Icon(Icons.Filled.Business, null) })
                }
            }
        },
        floatingActionButton = {
            AnimatedContent(targetState = tab, label = "adminFab") { t ->
                ExtendedFloatingActionButton(
                    onClick = if (t == 0) onNuevoUsuario else onNuevaSucursal,
                    icon = { Icon(if (t == 0) Icons.Filled.PersonAdd else Icons.Filled.AddLocation, contentDescription = null) },
                    text = { Text(if (t == 0) "Nuevo usuario" else "Nueva sucursal") },
                    modifier = Modifier.heightIn(min = 56.dp)
                )
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = tab,
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally { it * dir } + fadeIn()) togetherWith (slideOutHorizontally { -it * dir } + fadeOut())
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            label = "adminTabs"
        ) { selected ->
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selected == 0) {
                    items(usuarios, key = { it.idEmpleado }) { u ->
                        UsuarioCard(
                            usuario = u,
                            esActual = u.idEmpleado == state.idActual,
                            onActivo = { activo -> viewModel.onActivoChanged(u, activo) },
                            modifier = Modifier.animateItem()
                        )
                    }
                } else {
                    items(sucursales, key = { it.idSucursal }) { s ->
                        SucursalCard(s, Modifier.animateItem())
                    }
                }
            }
        }
    }
}

@Composable
private fun UsuarioCard(
    usuario: EmpleadoEntity,
    esActual: Boolean,
    onActivo: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val rol = Rol.from(usuario.rol) ?: Rol.EMPLEADO
    val icon = when (rol) {
        Rol.EMPLEADO -> Icons.Filled.Engineering
        Rol.SUPERVISOR -> Icons.Filled.SupervisorAccount
        Rol.ADMIN -> Icons.Filled.AdminPanelSettings
    }
    Card(modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = rol.etiqueta, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column(
                Modifier
                    .weight(1f)
                    .alpha(if (usuario.activo) 1f else 0.5f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(usuario.nombreCompleto, style = MaterialTheme.typography.titleMedium)
                Text("${usuario.rut} · ${usuario.correoCorporativo}", style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    when (rol) {
                        Rol.EMPLEADO -> StatusPill(usuario.especialidadTecnica, InfoContainer, InfoBlue)
                        Rol.SUPERVISOR -> StatusPill(rol.etiqueta, WarningContainer, WarningOrange)
                        Rol.ADMIN -> StatusPill(rol.etiqueta, SuccessContainer, SuccessGreen)
                    }
                    if (esActual) StatusPill("Tú", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Switch(
                checked = usuario.activo,
                onCheckedChange = onActivo,
                enabled = !esActual
            )
        }
    }
}

@Composable
private fun SucursalCard(sucursal: SucursalEntity, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(sucursal.nombreCliente, style = MaterialTheme.typography.titleMedium)
                Text(sucursal.direccionSucursal, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Lat ${GeoUtils.formatCoord(sucursal.latitudRef)} · Lon ${GeoUtils.formatCoord(sucursal.longitudRef)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
