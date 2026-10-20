@file:OptIn(ExperimentalMaterial3Api::class)

package cl.duoc.electromechanics.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.electromechanics.data.local.relation.RegistroConEmpleado
import cl.duoc.electromechanics.domain.util.DateUtils
import cl.duoc.electromechanics.domain.util.GeoUtils
import cl.duoc.electromechanics.model.TipoMarcacion
import cl.duoc.electromechanics.ui.components.EmptyState
import cl.duoc.electromechanics.ui.components.FotoArchivo
import cl.duoc.electromechanics.ui.components.StatusPill
import cl.duoc.electromechanics.ui.theme.InfoBlue
import cl.duoc.electromechanics.ui.theme.InfoContainer
import cl.duoc.electromechanics.ui.theme.SuccessContainer
import cl.duoc.electromechanics.ui.theme.SuccessGreen
import cl.duoc.electromechanics.ui.theme.WarningContainer
import cl.duoc.electromechanics.ui.theme.WarningOrange
import cl.duoc.electromechanics.ui.viewmodel.AppViewModelProvider
import cl.duoc.electromechanics.ui.viewmodel.HistorialViewModel

@Composable
fun HistorialJornadaScreen(
    onBack: () -> Unit,
    viewModel: HistorialViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val registros = viewModel.registros
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de jornada") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                }
            )
        }
    ) { padding ->
        if (registros.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.EventBusy,
                title = "Aún no tienes marcaciones",
                subtitle = "Tus entradas y salidas con GPS aparecerán aquí.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(registros, key = { it.registro.idRegistro }) { item ->
                    RegistroJornadaCard(item, mostrarEmpleado = false, modifier = Modifier.animateItem())
                }
            }
        }
    }
}

@Composable
fun RegistroJornadaCard(
    item: RegistroConEmpleado,
    mostrarEmpleado: Boolean,
    modifier: Modifier = Modifier,
    onAprobar: (() -> Unit)? = null
) {
    val r = item.registro
    val esEntrada = TipoMarcacion.from(r.tipoMarcacion) == TipoMarcacion.ENTRADA
    Card(modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (esEntrada) Icons.AutoMirrored.Filled.Login else Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                tint = if (esEntrada) SuccessGreen else InfoBlue,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                if (mostrarEmpleado) {
                    Text(item.empleado?.nombreCompleto ?: "Empleado desconocido", style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    "${TipoMarcacion.from(r.tipoMarcacion).etiqueta} · ${DateUtils.formatFechaHora(r.fechaHoraMarcacion)}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "${GeoUtils.formatCoord(r.latitudMarcacion)}, ${GeoUtils.formatCoord(r.longitudMarcacion)} (±${r.precisionMetros.toInt()} m)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                item.sucursal?.let {
                    Text(
                        "${it.nombreCliente} · ${GeoUtils.formatDistance(r.distanciaMetros ?: 0.0)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (r.dentroDeRadio) StatusPill("En sucursal", SuccessContainer, SuccessGreen)
                    else StatusPill("Fuera de radio", WarningContainer, WarningOrange)
                    if (r.aprobado) StatusPill("Aprobado", InfoContainer, InfoBlue)
                    if (r.ubicacionSimulada) StatusPill("GPS simulado", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error)
                }
                if (onAprobar != null) {
                    TextButton(onClick = onAprobar) { Text("Aprobar marcación") }
                }
            }
            r.fotoRespaldoPath?.let { path ->
                Spacer(Modifier.width(8.dp))
                FotoArchivo(
                    path = path,
                    contentDescription = "Foto de respaldo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }
        }
    }
}
