@file:OptIn(ExperimentalMaterial3Api::class)

package cl.duoc.electromechanics.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.electromechanics.model.Prioridad
import cl.duoc.electromechanics.ui.components.BigButton
import cl.duoc.electromechanics.ui.components.DropdownField
import cl.duoc.electromechanics.ui.components.MensajeToast
import cl.duoc.electromechanics.ui.components.SectionHeader
import cl.duoc.electromechanics.ui.components.ValidatedTextField
import cl.duoc.electromechanics.ui.components.shake
import cl.duoc.electromechanics.ui.viewmodel.AppViewModelProvider
import cl.duoc.electromechanics.ui.viewmodel.OrdenFormViewModel

private val UrgenteIcon: @Composable () -> Unit = {
    Icon(Icons.Filled.PriorityHigh, contentDescription = null)
}

@Composable
fun OrdenFormScreen(
    onBack: () -> Unit,
    viewModel: OrdenFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state = viewModel.uiState

    MensajeToast(state.mensaje) { viewModel.onMensajeMostrado() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva orden de trabajo") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .shake(state.shakeTrigger),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SectionHeader("Cliente y asignación")
            DropdownField(
                label = "Sucursal del cliente *",
                options = state.sucursales,
                selected = state.sucursal,
                optionLabel = { it.nombreCliente },
                optionSupporting = { it.direccionSucursal },
                onSelected = { viewModel.onSucursalChanged(it) },
                leadingIcon = Icons.Filled.Store,
                error = state.sucursalError
            )
            DropdownField(
                label = "Técnico asignado *",
                options = state.tecnicos,
                selected = state.tecnico,
                optionLabel = { it.nombreCompleto },
                optionSupporting = { "Especialidad: ${it.especialidadTecnica}" },
                onSelected = { viewModel.onTecnicoChanged(it) },
                leadingIcon = Icons.Filled.Engineering,
                error = state.tecnicoError,
                hint = state.tecnico?.let { "Especialidad: ${it.especialidadTecnica}" } ?: "Elige según la complejidad del trabajo"
            )

            SectionHeader("Programación")
            ValidatedTextField(
                field = state.fecha,
                onValueChange = { viewModel.onFechaChanged(it) },
                label = "Fecha programada *",
                leadingIcon = Icons.Filled.Event,
                hint = "Formato dd/mm/aaaa",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                onFocusLost = { viewModel.onFechaFocusLost() }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = { viewModel.onFechaRapida(0) }, label = { Text("Hoy") })
                AssistChip(onClick = { viewModel.onFechaRapida(1) }, label = { Text("Mañana") })
                AssistChip(onClick = { viewModel.onFechaRapida(7) }, label = { Text("En 1 semana") })
            }

            Text("Prioridad", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Prioridad.entries.forEach { p ->
                    FilterChip(
                        selected = state.prioridad == p,
                        onClick = { viewModel.onPrioridadChanged(p) },
                        label = { Text(p.etiqueta) },
                        leadingIcon = if (p == Prioridad.URGENTE) UrgenteIcon else null,
                        colors = if (p == Prioridad.URGENTE) FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                        ) else FilterChipDefaults.filterChipColors(),
                        modifier = Modifier.heightIn(min = 48.dp)
                    )
                }
            }

            SectionHeader("Trabajo a realizar")
            ValidatedTextField(
                field = state.descripcion,
                onValueChange = { viewModel.onDescripcionChanged(it) },
                label = "Descripción del trabajo *",
                leadingIcon = Icons.AutoMirrored.Filled.Assignment,
                hint = "Ej: Mantención preventiva de tablero eléctrico",
                maxChars = 300,
                singleLine = false,
                minLines = 2,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                onFocusLost = { viewModel.onDescripcionFocusLost() }
            )
            ValidatedTextField(
                field = state.actividades,
                onValueChange = { viewModel.onActividadesChanged(it) },
                label = "Actividades mínimas requeridas *",
                leadingIcon = Icons.Filled.FactCheck,
                hint = "Lo que el técnico debe ejecutar como mínimo",
                maxChars = 500,
                singleLine = false,
                minLines = 3,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                onFocusLost = { viewModel.onActividadesFocusLost() }
            )

            BigButton(
                text = "Crear y asignar orden",
                icon = Icons.AutoMirrored.Filled.Send,
                onClick = { viewModel.onGuardarClick(onSuccess = onBack) },
                loading = state.guardando,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
