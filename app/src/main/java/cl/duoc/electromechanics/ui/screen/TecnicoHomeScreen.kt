@file:OptIn(ExperimentalMaterial3Api::class)

package cl.duoc.electromechanics.ui.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.WorkOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.electromechanics.data.local.relation.OrdenConDetalle
import cl.duoc.electromechanics.domain.util.DateUtils
import cl.duoc.electromechanics.domain.util.GeoUtils
import cl.duoc.electromechanics.model.EstadoServicio
import cl.duoc.electromechanics.model.MarcacionPendiente
import cl.duoc.electromechanics.model.Prioridad
import cl.duoc.electromechanics.model.TecnicoHomeModel
import cl.duoc.electromechanics.model.TipoMarcacion
import cl.duoc.electromechanics.ui.components.BigButton
import cl.duoc.electromechanics.ui.components.EmptyState
import cl.duoc.electromechanics.ui.components.EstadoChip
import cl.duoc.electromechanics.ui.components.FotoArchivo
import cl.duoc.electromechanics.ui.components.InfoRow
import cl.duoc.electromechanics.ui.components.MensajeToast
import cl.duoc.electromechanics.ui.components.PulsingIcon
import cl.duoc.electromechanics.ui.components.RequestNotificationPermissionOnce
import cl.duoc.electromechanics.ui.components.SectionHeader
import cl.duoc.electromechanics.ui.components.StatusPill
import cl.duoc.electromechanics.ui.components.UrgenteBadge
import cl.duoc.electromechanics.ui.components.openAppSettings
import cl.duoc.electromechanics.ui.components.openLocationSettings
import cl.duoc.electromechanics.ui.components.rememberCameraCapture
import cl.duoc.electromechanics.ui.components.rememberLocationPermission
import cl.duoc.electromechanics.ui.theme.Amber
import cl.duoc.electromechanics.ui.theme.SuccessContainer
import cl.duoc.electromechanics.ui.theme.SuccessGreen
import cl.duoc.electromechanics.ui.theme.WarningContainer
import cl.duoc.electromechanics.ui.theme.WarningOrange
import cl.duoc.electromechanics.ui.viewmodel.AppViewModelProvider
import cl.duoc.electromechanics.ui.viewmodel.TecnicoHomeViewModel

@Composable
fun TecnicoHomeScreen(
    onOpenServicio: (String) -> Unit,
    onOpenHistorial: () -> Unit,
    onLogout: () -> Unit,
    viewModel: TecnicoHomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state = viewModel.uiState
    val context = LocalContext.current

    RequestNotificationPermissionOnce()

    MensajeToast(state.mensaje) { viewModel.onMensajeMostrado() }

    val pedirUbicacion = rememberLocationPermission(
        onGranted = { viewModel.onMarcarClick() },
        onDenied = { viewModel.onPermisoDenegado() }
    )
    val tomarFotoRespaldo = rememberCameraCapture(
        prefix = "jornada",
        onPhoto = { viewModel.onFotoRespaldo(it) },
        onDenied = {
            Toast.makeText(context, "Sin permiso de cámara: la foto de respaldo es opcional", Toast.LENGTH_SHORT).show()
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hola, ${state.nombre.substringBefore(" Demo")}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Técnico en terreno", style = MaterialTheme.typography.bodySmall)
                    }
                },
                actions = {
                    if (state.linternaDisponible) {
                        IconButton(onClick = { viewModel.onLinternaClick() }) {
                            Icon(
                                if (state.linternaEncendida) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                                contentDescription = if (state.linternaEncendida) "Apagar linterna" else "Encender linterna",
                                tint = if (state.linternaEncendida) Amber else LocalContentColor.current
                            )
                        }
                    }
                    IconButton(onClick = onOpenHistorial) {
                        Icon(Icons.Filled.History, contentDescription = "Historial de jornada")
                    }
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
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "jornada") {
                JornadaCard(
                    state = state,
                    capturando = state.capturandoGps,
                    onMarcar = pedirUbicacion
                )
            }

            item(key = "ruta_header") {
                SectionHeader("Hoja de ruta") {
                    Text(
                        "${state.hojaDeRuta.count { EstadoServicio.from(it.orden.estado) != EstadoServicio.FINALIZADO }} pendientes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!state.cargando && state.hojaDeRuta.isEmpty()) {
                item(key = "vacio") {
                    EmptyState(
                        icon = Icons.Filled.EventAvailable,
                        title = "Sin servicios asignados",
                        subtitle = "Cuando el supervisor te asigne una orden aparecerá aquí y recibirás una notificación."
                    )
                }
            }

            state.hojaDeRuta
                .groupBy { DateUtils.etiquetaDia(it.orden.fechaProgramada) }
                .forEach { (dia, ordenes) ->
                    item(key = "dia_$dia") {
                        Text(
                            dia,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .animateItem()
                        )
                    }
                    items(ordenes, key = { it.orden.idServicio }) { orden ->
                        OrdenCard(
                            orden = orden,
                            onClick = { onOpenServicio(orden.orden.idServicio) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
        }
    }

    state.marcacionPendiente?.let { pendiente ->
        ConfirmarMarcacionDialog(
            pendiente = pendiente,
            guardando = state.guardandoMarcacion,
            onTomarFoto = tomarFotoRespaldo,
            onConfirmar = { viewModel.onConfirmarMarcacion() },
            onCancelar = { viewModel.onCancelarMarcacion() }
        )
    }
    if (state.gpsDesactivado) {
        AvisoDialog(
            icon = Icons.Filled.LocationOff,
            titulo = "GPS desactivado",
            mensaje = "Para marcar tu jornada debes activar la ubicación del dispositivo.",
            accion = "Activar ubicación",
            onAccion = {
                viewModel.onCerrarAviso()
                context.openLocationSettings()
            },
            onDismiss = { viewModel.onCerrarAviso() }
        )
    }
    if (state.permisoDenegado) {
        AvisoDialog(
            icon = Icons.Filled.LocationOff,
            titulo = "Permiso de ubicación requerido",
            mensaje = "La marcación de entrada y salida registra obligatoriamente tu ubicación GPS. " +
                "Puedes conceder el permiso desde los ajustes de la aplicación.",
            accion = "Abrir ajustes",
            onAccion = {
                viewModel.onCerrarAviso()
                context.openAppSettings()
            },
            onDismiss = { viewModel.onCerrarAviso() }
        )
    }
}

@Composable
private fun JornadaCard(state: TecnicoHomeModel, capturando: Boolean, onMarcar: () -> Unit) {
    val activa = state.jornadaActiva
    val container by animateColorAsState(
        if (activa) SuccessContainer else MaterialTheme.colorScheme.surface,
        tween(600),
        label = "jornadaBg"
    )
    Card(
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AnimatedContent(
                targetState = activa,
                transitionSpec = {
                    (slideInVertically { it / 2 } + fadeIn()) togetherWith (slideOutVertically { -it / 2 } + fadeOut())
                },
                label = "jornadaEstado"
            ) { enJornada ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (enJornada) Icons.Filled.Work else Icons.Filled.WorkOff,
                        contentDescription = null,
                        tint = if (enJornada) SuccessGreen else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            if (enJornada) "Jornada en curso" else "Jornada no iniciada",
                            style = MaterialTheme.typography.titleLarge
                        )
                        val ultima = state.ultimaMarcacion
                        Text(
                            when {
                                ultima == null -> "Marca tu entrada al llegar a la sucursal"
                                enJornada -> "Entrada a las ${DateUtils.formatHora(ultima.fechaHoraMarcacion)}"
                                else -> "Salida a las ${DateUtils.formatHora(ultima.fechaHoraMarcacion)}"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            state.ultimaMarcacion?.let { ultima ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (ultima.dentroDeRadio) {
                        StatusPill("En sucursal", SuccessContainer, SuccessGreen)
                    } else {
                        StatusPill("Fuera de radio", WarningContainer, WarningOrange)
                    }
                    ultima.distanciaMetros?.let {
                        Text("a ${GeoUtils.formatDistance(it)}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (capturando) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PulsingIcon(Icons.Filled.GpsFixed, MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text("Obteniendo ubicación GPS…", style = MaterialTheme.typography.bodyLarge)
                }
            }

            val esEntrada = state.siguienteMarcacion == TipoMarcacion.ENTRADA
            BigButton(
                text = if (esEntrada) "Marcar entrada" else "Marcar salida",
                icon = Icons.Filled.MyLocation,
                onClick = onMarcar,
                loading = capturando,
                containerColor = if (esEntrada) MaterialTheme.colorScheme.primary else Amber,
                contentColor = if (esEntrada) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}

@Composable
private fun OrdenCard(orden: OrdenConDetalle, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val estado = EstadoServicio.from(orden.orden.estado)
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EstadoChip(estado)
                if (Prioridad.from(orden.orden.prioridad) == Prioridad.URGENTE) UrgenteBadge()
            }
            Text(orden.orden.descripcionTrabajo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            HorizontalDivider()
            InfoRow(Icons.Filled.Place, "Cliente", orden.sucursal?.nombreCliente ?: "Sucursal no encontrada")
            InfoRow(Icons.Filled.LocationOn, "Dirección", orden.sucursal?.direccionSucursal ?: "-")
            if (orden.actividades.isNotEmpty()) {
                InfoRow(Icons.Filled.CheckCircle, "Bitácora", "${orden.actividades.size} actividad(es) registradas")
            }
        }
    }
}

@Composable
private fun ConfirmarMarcacionDialog(
    pendiente: MarcacionPendiente,
    guardando: Boolean,
    onTomarFoto: () -> Unit,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!guardando) onCancelar() },
        icon = { Icon(Icons.Filled.GpsFixed, contentDescription = null) },
        title = { Text("Confirmar ${pendiente.tipo.etiqueta.lowercase()}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoRow(Icons.Filled.AccessTime, "Hora", DateUtils.formatFechaHora(System.currentTimeMillis()))
                InfoRow(
                    Icons.Filled.MyLocation,
                    "Coordenadas",
                    "${GeoUtils.formatCoord(pendiente.latitud)}, ${GeoUtils.formatCoord(pendiente.longitud)} (±${pendiente.precisionMetros.toInt()} m)"
                )
                InfoRow(
                    Icons.Filled.Place,
                    "Sucursal más cercana",
                    pendiente.sucursalCercana?.let { "$it · ${GeoUtils.formatDistance(pendiente.distanciaMetros ?: 0.0)}" } ?: "Sin sucursales registradas"
                )
                if (!pendiente.dentroDeRadio) {
                    AvisoLinea("Estás fuera del radio de 300 m de la sucursal. La marcación quedará marcada para revisión del supervisor.")
                }
                if (pendiente.precisionBaja) AvisoLinea("Precisión GPS baja. Si puedes, reintenta al aire libre.")
                if (pendiente.simulada) AvisoLinea("Se detectó una ubicación simulada.")

                Spacer(Modifier.size(4.dp))
                if (pendiente.fotoPath != null) {
                    FotoArchivo(
                        path = pendiente.fotoPath,
                        contentDescription = "Foto de respaldo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                }
                TextButton(onClick = onTomarFoto, enabled = !guardando) {
                    Icon(Icons.Filled.AddAPhoto, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (pendiente.fotoPath == null) "Agregar foto de respaldo (opcional)" else "Cambiar foto")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirmar, enabled = !guardando) {
                if (guardando) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Confirmar", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar, enabled = !guardando) { Text("Cancelar") }
        }
    )
}

@Composable
private fun AvisoLinea(texto: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(Icons.Filled.Warning, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
        Text(texto, style = MaterialTheme.typography.bodySmall, color = WarningOrange)
    }
}

@Composable
fun AvisoDialog(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    mensaje: String,
    accion: String,
    onAccion: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(icon, contentDescription = null) },
        title = { Text(titulo) },
        text = { Text(mensaje) },
        confirmButton = { TextButton(onClick = onAccion) { Text(accion) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Ahora no") } }
    )
}
