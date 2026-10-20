@file:OptIn(ExperimentalMaterial3Api::class)

package cl.duoc.electromechanics.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.electromechanics.data.local.relation.OrdenConDetalle
import cl.duoc.electromechanics.domain.util.DateUtils
import cl.duoc.electromechanics.model.EstadoServicio
import cl.duoc.electromechanics.model.Prioridad
import cl.duoc.electromechanics.model.SupervisorModel
import cl.duoc.electromechanics.ui.components.EmptyState
import cl.duoc.electromechanics.ui.components.EstadoChip
import cl.duoc.electromechanics.ui.components.InfoRow
import cl.duoc.electromechanics.ui.components.MensajeToast
import cl.duoc.electromechanics.ui.components.SectionHeader
import cl.duoc.electromechanics.ui.components.StatusPill
import cl.duoc.electromechanics.ui.components.UrgenteBadge
import cl.duoc.electromechanics.ui.theme.InfoBlue
import cl.duoc.electromechanics.ui.theme.InfoContainer
import cl.duoc.electromechanics.ui.theme.SuccessContainer
import cl.duoc.electromechanics.ui.theme.SuccessGreen
import cl.duoc.electromechanics.ui.theme.WarningContainer
import cl.duoc.electromechanics.ui.theme.WarningOrange
import cl.duoc.electromechanics.ui.viewmodel.AppViewModelProvider
import cl.duoc.electromechanics.ui.viewmodel.SupervisorViewModel

@Composable
fun SupervisorHomeScreen(
    onNuevaOrden: () -> Unit,
    onOpenServicio: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: SupervisorViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state = viewModel.uiState
    var tab by rememberSaveable { mutableIntStateOf(0) }

    MensajeToast(state.mensaje) { viewModel.onMensajeMostrado() }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text("Supervisión de operaciones")
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
                    Tab(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        text = { Text("Órdenes") },
                        icon = { Icon(Icons.Filled.AssignmentInd, contentDescription = null) }
                    )
                    Tab(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        text = { Text("Auditoría") },
                        icon = {
                            BadgedBox(badge = {
                                if (state.porAprobar > 0) Badge { Text("${state.porAprobar}") }
                            }) {
                                Icon(Icons.Filled.FactCheck, contentDescription = null)
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(visible = tab == 0, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
                ExtendedFloatingActionButton(
                    onClick = onNuevaOrden,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Nueva orden") },
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
            label = "supervisorTabs"
        ) { selected ->
            if (selected == 0) {
                OrdenesTab(state, { viewModel.onFiltroChanged(it) }, onOpenServicio)
            } else {
                AuditoriaTab(state, { viewModel.onAprobarRegistro(it) })
            }
        }
    }
}

@Composable
private fun OrdenesTab(
    state: SupervisorModel,
    onFiltro: (EstadoServicio?) -> Unit,
    onOpenServicio: (String) -> Unit
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ContadorCard("Pendientes", state.contar(EstadoServicio.PENDIENTE), WarningContainer, WarningOrange, Modifier.weight(1f))
                ContadorCard("En proceso", state.contar(EstadoServicio.EN_PROCESO), InfoContainer, InfoBlue, Modifier.weight(1f))
                ContadorCard("Finalizadas", state.contar(EstadoServicio.FINALIZADO), SuccessContainer, SuccessGreen, Modifier.weight(1f))
            }
        }
        item {
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = state.filtro == null, onClick = { onFiltro(null) }, label = { Text("Todas") })
                EstadoServicio.entries.forEach { estado ->
                    FilterChip(
                        selected = state.filtro == estado,
                        onClick = { onFiltro(estado) },
                        label = { Text(estado.etiqueta) }
                    )
                }
            }
        }
        if (state.ordenesFiltradas.isEmpty()) {
            item {
                EmptyState(Icons.Filled.Inbox, "Sin órdenes", "No hay órdenes de trabajo con este filtro.")
            }
        }
        items(state.ordenesFiltradas, key = { it.orden.idServicio }) { orden ->
            OrdenSupervisorCard(orden, onClick = { onOpenServicio(orden.orden.idServicio) }, modifier = Modifier.animateItem())
        }
    }
}

@Composable
private fun ContadorCard(titulo: String, valor: Int, bg: Color, fg: Color, modifier: Modifier = Modifier) {
    val animado by animateIntAsState(targetValue = valor, label = "contador")
    Card(colors = CardDefaults.cardColors(containerColor = bg, contentColor = fg), modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Text("$animado", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(titulo, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun OrdenSupervisorCard(orden: OrdenConDetalle, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val o = orden.orden
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EstadoChip(EstadoServicio.from(o.estado))
                if (Prioridad.from(o.prioridad) == Prioridad.URGENTE) UrgenteBadge()
                Spacer(Modifier.weight(1f))
                if (o.aprobada) Icon(Icons.Filled.Verified, contentDescription = "Aprobada", tint = SuccessGreen)
            }
            Text(o.descripcionTrabajo, style = MaterialTheme.typography.titleMedium)
            InfoRow(Icons.Filled.Engineering, "Técnico", orden.tecnico?.nombreCompleto ?: "-")
            InfoRow(Icons.Filled.Place, "Sucursal", orden.sucursal?.nombreCliente ?: "-")
            InfoRow(
                Icons.Filled.CalendarToday,
                "Fecha · bitácora",
                "${DateUtils.formatFecha(o.fechaProgramada)} · ${orden.actividades.size} actividad(es)"
            )
        }
    }
}

@Composable
private fun AuditoriaTab(state: SupervisorModel, onAprobar: (String) -> Unit) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { SectionHeader("Horas trabajadas hoy") }
        if (state.resumen.isEmpty()) {
            item {
                Text(
                    "Ningún técnico ha marcado entrada hoy.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(state.resumen, key = { "res_${it.nombre}" }) { r ->
            Card(Modifier.fillMaxWidth().animateItem()) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(r.nombre, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${r.marcaciones} marcación(es) · ${r.fueraDeRadio} fuera de radio",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(DateUtils.formatDuracion(r.horasMillis), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (r.enJornada) StatusPill("En jornada", SuccessContainer, SuccessGreen)
                    }
                }
            }
        }

        item { SectionHeader("Marcaciones registradas (${state.registros.size})") }
        if (state.registros.isEmpty()) {
            item { EmptyState(Icons.Filled.FactCheck, "Sin marcaciones", "Las entradas y salidas de los técnicos aparecerán aquí.") }
        }
        items(state.registros, key = { it.registro.idRegistro }) { item ->
            RegistroJornadaCard(
                item = item,
                mostrarEmpleado = true,
                modifier = Modifier.animateItem(),
                onAprobar = if (!item.registro.aprobado) ({ onAprobar(item.registro.idRegistro) }) else null
            )
        }
    }
}
