@file:OptIn(ExperimentalMaterial3Api::class)

package cl.duoc.electromechanics.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.electromechanics.model.EstadoEquipo
import cl.duoc.electromechanics.ui.components.BigButton
import cl.duoc.electromechanics.ui.components.DropdownField
import cl.duoc.electromechanics.ui.components.MensajeToast
import cl.duoc.electromechanics.ui.components.ValidatedTextField
import cl.duoc.electromechanics.ui.components.shake
import cl.duoc.electromechanics.ui.viewmodel.ActividadFormViewModel
import cl.duoc.electromechanics.ui.viewmodel.AppViewModelProvider

@Composable
fun ActividadFormScreen(
    onBack: () -> Unit,
    viewModel: ActividadFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state = viewModel.uiState

    MensajeToast(state.mensaje) { viewModel.onMensajeMostrado() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar actividad") },
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
            Text(
                "Describe lo ejecutado para dejar trazabilidad técnica del servicio.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ValidatedTextField(
                field = state.detalle,
                onValueChange = { viewModel.onDetalleChanged(it) },
                label = "Detalle de la tarea ejecutada *",
                leadingIcon = Icons.Filled.Build,
                hint = "Mínimo 10 caracteres",
                maxChars = 500,
                singleLine = false,
                minLines = 3,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                onFocusLost = { viewModel.onDetalleFocusLost() }
            )

            DropdownField(
                label = "Estado del equipo intervenido *",
                options = EstadoEquipo.entries,
                selected = state.estadoEquipo,
                optionLabel = { it.etiqueta },
                onSelected = { viewModel.onEstadoEquipoChanged(it) },
                leadingIcon = Icons.Filled.Settings,
                error = state.estadoEquipoError
            )

            ValidatedTextField(
                field = state.repuestos,
                onValueChange = { viewModel.onRepuestosChanged(it) },
                label = "Repuestos utilizados",
                leadingIcon = Icons.Filled.Category,
                hint = "Opcional. Ej: 2 rodamientos 6205, 1 contactor 25A",
                maxChars = 300,
                singleLine = false,
                minLines = 2,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                onFocusLost = { viewModel.onRepuestosFocusLost() }
            )

            ValidatedTextField(
                field = state.comentarios,
                onValueChange = { viewModel.onComentariosChanged(it) },
                label = "Comentarios técnicos",
                leadingIcon = Icons.AutoMirrored.Filled.Notes,
                hint = "Opcional. Observaciones o recomendaciones",
                maxChars = 500,
                singleLine = false,
                minLines = 2,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                onFocusLost = { viewModel.onComentariosFocusLost() }
            )

            BigButton(
                text = "Guardar actividad",
                icon = Icons.Filled.Save,
                onClick = { viewModel.onGuardarClick(onSuccess = onBack) },
                loading = state.guardando,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
