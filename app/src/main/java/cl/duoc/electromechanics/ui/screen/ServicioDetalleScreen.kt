@file:OptIn(ExperimentalMaterial3Api::class)

package cl.duoc.electromechanics.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.electromechanics.data.local.entity.ActividadEntity
import cl.duoc.electromechanics.data.local.relation.OrdenConDetalle
import cl.duoc.electromechanics.domain.util.DateUtils
import cl.duoc.electromechanics.model.EstadoEquipo
import cl.duoc.electromechanics.model.EstadoServicio
import cl.duoc.electromechanics.model.Prioridad
import cl.duoc.electromechanics.model.ServicioDetalleModel
import cl.duoc.electromechanics.ui.components.BigButton
import cl.duoc.electromechanics.ui.components.BigOutlinedButton
import cl.duoc.electromechanics.ui.components.EstadoChip
import cl.duoc.electromechanics.ui.components.InfoRow
import cl.duoc.electromechanics.ui.components.MensajeToast
import cl.duoc.electromechanics.ui.components.PhotoSlot
import cl.duoc.electromechanics.ui.components.RequisitosChecklist
import cl.duoc.electromechanics.ui.components.SectionHeader
import cl.duoc.electromechanics.ui.components.UrgenteBadge
import cl.duoc.electromechanics.ui.components.openInMaps
import cl.duoc.electromechanics.ui.components.rememberCameraCapture
import cl.duoc.electromechanics.ui.theme.Amber
import cl.duoc.electromechanics.ui.theme.SuccessContainer
import cl.duoc.electromechanics.ui.theme.SuccessGreen
import cl.duoc.electromechanics.ui.viewmodel.AppViewModelProvider
import cl.duoc.electromechanics.ui.viewmodel.ServicioDetalleViewModel

@Composable
fun ServicioDetalleScreen(
    onBack: () -> Unit,
    onRegistrarActividad: (String) -> Unit,
    viewModel: ServicioDetalleViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state = viewModel.uiState
    val context = LocalContext.current

    MensajeToast(state.mensaje) { viewModel.onMensajeMostrado() }

    val sinPermisoCamara = { viewModel.mostrarMensaje("Se necesita el permiso de cámara para la evidencia fotográfica") }
    val fotoInicial = rememberCameraCapture("inicial", { viewModel.onFotoInicial(it) }, sinPermisoCamara)
    val fotoFinal = rememberCameraCapture("final", { viewModel.onFotoFinal(it) }, sinPermisoCamara)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del servicio") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                }
            )
        },
        bottomBar = {
            if (state.orden != null) {
                AccionPrincipal(
                    state = state,
                    onIniciar = { viewModel.onIniciarClick() },
                    onFinalizar = { viewModel.onFinalizarClick() },
                    onAprobar = { viewModel.onAprobarClick() }
                )
            }
        }
    ) { padding ->
        val orden = state.orden
        if (orden == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                if (state.cargando) CircularProgressIndicator() else Text("La orden no existe")
            }
            return@Scaffold
        }

        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                InfoServicioCard(orden, onMapa = {
                    orden.sucursal?.let { s ->
                        if (!context.openInMaps(s.latitudRef, s.longitudRef, s.nombreCliente)) {
                            viewModel.mostrarMensaje("No hay una app de mapas instalada")
                        }
                    }
                })
            }

            item { SectionHeader("Evidencia fotográfica") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PhotoSlot(
                        title = "Estado inicial",
                        path = orden.orden.fotoInicialPath,
                        enabled = state.puedeFotoInicial,
                        onTakePhoto = fotoInicial,
                        modifier = Modifier.weight(1f)
                    )
                    PhotoSlot(
                        title = "Resultado final",
                        path = orden.orden.fotoFinalPath,
                        enabled = state.puedeFotoFinal,
                        onTakePhoto = fotoFinal,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (state.esTecnico && state.estado != EstadoServicio.FINALIZADO) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                if (state.estado == EstadoServicio.PENDIENTE) "Para iniciar el servicio" else "Para finalizar el servicio",
                                style = MaterialTheme.typography.titleMedium
                            )
                            RequisitosChecklist(
                                if (state.estado == EstadoServicio.PENDIENTE) state.requisitosInicio else state.requisitosFinal
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader("Bitácora de actividades (${orden.actividades.size})")
            }
            if (state.puedeRegistrarActividad) {
                item {
                    BigOutlinedButton(
                        text = "Registrar actividad",
                        icon = Icons.Filled.Edit,
                        onClick = { onRegistrarActividad(orden.orden.idServicio) }
                    )
                }
            }
            if (orden.actividades.isEmpty()) {
                item {
                    Text(
                        if (state.estado == EstadoServicio.PENDIENTE) "Inicia el servicio para registrar las tareas ejecutadas."
                        else "Aún no hay actividades registradas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(orden.actividades, key = { it.idActividad }) { actividad ->
                ActividadCard(actividad, Modifier.animateItem())
            }
        }
    }
}

@Composable
private fun InfoServicioCard(orden: OrdenConDetalle, onMapa: () -> Unit) {
    val o = orden.orden
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EstadoChip(EstadoServicio.from(o.estado))
                if (Prioridad.from(o.prioridad) == Prioridad.URGENTE) UrgenteBadge()
                if (o.aprobada) {
                    Icon(Icons.Filled.Verified, contentDescription = "Aprobado por supervisor", tint = SuccessGreen)
                }
            }
            Text(o.descripcionTrabajo, style = MaterialTheme.typography.titleLarge)
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            InfoRow(Icons.Filled.Place, "Cliente / sucursal", orden.sucursal?.nombreCliente ?: "-")
            InfoRow(Icons.Filled.Map, "Dirección", orden.sucursal?.direccionSucursal ?: "-")
            InfoRow(Icons.Filled.CalendarToday, "Fecha programada", "${DateUtils.etiquetaDia(o.fechaProgramada)} · ${DateUtils.formatFecha(o.fechaProgramada)}")
            InfoRow(Icons.Filled.Engineering, "Técnico asignado", orden.tecnico?.let { "${it.nombreCompleto} (${it.especialidadTecnica})" } ?: "-")
            InfoRow(Icons.AutoMirrored.Filled.Assignment, "Actividades mínimas requeridas", o.actividadesRequeridas)
            if (o.horaInicio != null) {
                val fin = o.horaTermino?.let { " · Término ${DateUtils.formatHora(it)}" } ?: ""
                InfoRow(Icons.Filled.Schedule, "Ejecución", "Inicio ${DateUtils.formatHora(o.horaInicio)}$fin")
            }
            if (orden.sucursal != null) {
                BigOutlinedButton(text = "Ver ubicación en mapa", icon = Icons.Filled.Map, onClick = onMapa)
            }
        }
    }
}

@Composable
private fun ActividadCard(actividad: ActividadEntity, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    DateUtils.formatFechaHora(actividad.horaRegistro),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f)
                )
                Text(EstadoEquipo.from(actividad.estadoEquipo).etiqueta, style = MaterialTheme.typography.labelMedium)
            }
            Text(actividad.detalleTareaEjecutada, style = MaterialTheme.typography.bodyLarge)
            InfoRow(Icons.Filled.Category, "Repuestos", actividad.repuestosUtilizados)
            if (actividad.comentariosTecnicos.isNotBlank()) {
                InfoRow(Icons.AutoMirrored.Filled.Notes, "Comentarios", actividad.comentariosTecnicos)
            }
        }
    }
}

@Composable
private fun AccionPrincipal(
    state: ServicioDetalleModel,
    onIniciar: () -> Unit,
    onFinalizar: () -> Unit,
    onAprobar: () -> Unit
) {
    Surface(tonalElevation = 3.dp, shadowElevation = 6.dp) {
        Box(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            AnimatedContent(
                targetState = Triple(state.estado, state.esTecnico, state.orden?.orden?.aprobada == true),
                transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.9f)) togetherWith fadeOut() },
                label = "accionPrincipal"
            ) { (estado, esTecnico, aprobada) ->
                when {
                    esTecnico && estado == EstadoServicio.PENDIENTE -> BigButton(
                        text = "Iniciar servicio",
                        icon = Icons.Filled.PlayArrow,
                        onClick = onIniciar,
                        enabled = state.puedeIniciar,
                        loading = state.procesando
                    )
                    esTecnico && estado == EstadoServicio.EN_PROCESO -> BigButton(
                        text = "Finalizar servicio",
                        icon = Icons.Filled.DoneAll,
                        onClick = onFinalizar,
                        enabled = state.puedeFinalizar,
                        loading = state.procesando,
                        containerColor = Amber,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                    !esTecnico && estado == EstadoServicio.FINALIZADO && !aprobada -> BigButton(
                        text = "Aprobar servicio",
                        icon = Icons.Filled.Verified,
                        onClick = onAprobar,
                        loading = state.procesando,
                        containerColor = SuccessGreen
                    )
                    else -> EstadoFinal(estado, aprobada)
                }
            }
        }
    }
}

@Composable
private fun EstadoFinal(estado: EstadoServicio, aprobada: Boolean) {
    val texto = when {
        aprobada -> "Servicio aprobado por el supervisor"
        estado == EstadoServicio.FINALIZADO -> "Servicio finalizado · pendiente de aprobación"
        else -> "Servicio ${estado.etiqueta.lowercase()} · en manos del técnico"
    }
    Surface(color = SuccessContainer, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen)
            Spacer(Modifier.width(10.dp))
            Text(texto, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = SuccessGreen)
        }
    }
}
