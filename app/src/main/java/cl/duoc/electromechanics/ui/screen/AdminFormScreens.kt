@file:OptIn(ExperimentalMaterial3Api::class)

package cl.duoc.electromechanics.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.electromechanics.domain.validation.FormValidators
import cl.duoc.electromechanics.model.Especialidades
import cl.duoc.electromechanics.model.Rol
import cl.duoc.electromechanics.ui.components.BigButton
import cl.duoc.electromechanics.ui.components.BigOutlinedButton
import cl.duoc.electromechanics.ui.components.DropdownField
import cl.duoc.electromechanics.ui.components.MensajeToast
import cl.duoc.electromechanics.ui.components.PasswordStrengthBar
import cl.duoc.electromechanics.ui.components.PulsingIcon
import cl.duoc.electromechanics.ui.components.SectionHeader
import cl.duoc.electromechanics.ui.components.ValidatedTextField
import cl.duoc.electromechanics.ui.components.rememberLocationPermission
import cl.duoc.electromechanics.ui.components.shake
import cl.duoc.electromechanics.ui.viewmodel.AppViewModelProvider
import cl.duoc.electromechanics.ui.viewmodel.SucursalFormViewModel
import cl.duoc.electromechanics.ui.viewmodel.UsuarioFormViewModel

@Composable
private fun FormScaffold(
    title: String,
    onBack: () -> Unit,
    shakeTrigger: Int,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
                .shake(shakeTrigger),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            content()
        }
    }
}

@Composable
fun UsuarioFormScreen(
    onBack: () -> Unit,
    viewModel: UsuarioFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state = viewModel.uiState
    MensajeToast(state.mensaje) { viewModel.onMensajeMostrado() }

    FormScaffold("Nuevo usuario", onBack, state.shakeTrigger) {
        SectionHeader("Datos personales")
        ValidatedTextField(
            field = state.rut,
            onValueChange = { viewModel.onRutChanged(it) },
            label = "RUT *",
            leadingIcon = Icons.Filled.Badge,
            hint = "Se valida el dígito verificador",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            onFocusLost = { viewModel.onRutFocusLost() }
        )
        ValidatedTextField(
            field = state.nombre,
            onValueChange = { viewModel.onNombreChanged(it) },
            label = "Nombre completo *",
            leadingIcon = Icons.Filled.Person,
            hint = "Nombre y apellido",
            maxChars = 60,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            onFocusLost = { viewModel.onNombreFocusLost() }
        )
        ValidatedTextField(
            field = state.correo,
            onValueChange = { viewModel.onCorreoChanged(it) },
            label = "Correo corporativo *",
            leadingIcon = Icons.Filled.Email,
            hint = "Ej: nombre@empresa.cl",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            onFocusLost = { viewModel.onCorreoFocusLost() }
        )

        SectionHeader("Perfil y privilegios")
        DropdownField(
            label = "Rol *",
            options = Rol.entries,
            selected = state.rol,
            optionLabel = { it.etiqueta },
            optionSupporting = {
                when (it) {
                    Rol.EMPLEADO -> "App móvil: jornada, hoja de ruta y bitácora"
                    Rol.SUPERVISOR -> "Crea órdenes, asigna técnicos y audita"
                    Rol.ADMIN -> "Gestiona usuarios y sucursales"
                }
            },
            onSelected = { viewModel.onRolChanged(it) },
            leadingIcon = Icons.Filled.Security,
            error = state.rolError
        )
        AnimatedVisibility(
            visible = state.requiereEspecialidad,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            DropdownField(
                label = "Especialidad técnica *",
                options = Especialidades.tecnicas,
                selected = state.especialidad,
                optionLabel = { it },
                onSelected = { viewModel.onEspecialidadChanged(it) },
                leadingIcon = Icons.Filled.Engineering,
                error = state.especialidadError
            )
        }

        SectionHeader("Credenciales")
        ValidatedTextField(
            field = state.password,
            onValueChange = { viewModel.onPasswordChanged(it) },
            label = "Contraseña *",
            leadingIcon = Icons.Filled.Lock,
            hint = "Mínimo 8 caracteres, con letras y números",
            showSuccess = false,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { viewModel.onTogglePasswordVisible() }) {
                    Icon(
                        if (state.passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (state.passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            onFocusLost = { viewModel.onPasswordFocusLost() }
        )
        AnimatedVisibility(visible = state.password.value.isNotEmpty()) {
            PasswordStrengthBar(state.fuerzaPassword, Modifier.padding(bottom = 8.dp))
        }
        ValidatedTextField(
            field = state.confirmacion,
            onValueChange = { viewModel.onConfirmacionChanged(it) },
            label = "Confirmar contraseña *",
            leadingIcon = Icons.Filled.Lock,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            onFocusLost = { viewModel.onConfirmacionFocusLost() }
        )

        BigButton(
            text = "Crear usuario",
            icon = Icons.Filled.Save,
            onClick = { viewModel.onGuardarClick(onSuccess = onBack) },
            loading = state.guardando,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun SucursalFormScreen(
    onBack: () -> Unit,
    viewModel: SucursalFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state = viewModel.uiState
    MensajeToast(state.mensaje) { viewModel.onMensajeMostrado() }

    val usarGps = rememberLocationPermission(
        onGranted = { viewModel.onUsarUbicacionClick() },
        onDenied = { viewModel.onPermisoDenegado() }
    )

    FormScaffold("Nueva sucursal", onBack, state.shakeTrigger) {
        SectionHeader("Cliente")
        ValidatedTextField(
            field = state.nombre,
            onValueChange = { viewModel.onNombreChanged(it) },
            label = "Nombre del cliente / sucursal *",
            leadingIcon = Icons.Filled.Business,
            maxChars = 80,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            onFocusLost = { viewModel.onNombreFocusLost() }
        )
        ValidatedTextField(
            field = state.direccion,
            onValueChange = { viewModel.onDireccionChanged(it) },
            label = "Dirección *",
            leadingIcon = Icons.Filled.Home,
            hint = "Usa direcciones ficticias en el entorno de pruebas",
            maxChars = 120,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            onFocusLost = { viewModel.onDireccionFocusLost() }
        )

        SectionHeader("Coordenadas de referencia")
        Text(
            "Se usan para verificar que el técnico marque su jornada dentro de un radio de 300 m.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ValidatedTextField(
                field = state.latitud,
                onValueChange = { viewModel.onLatitudChanged(it) },
                label = "Latitud *",
                leadingIcon = Icons.Filled.Explore,
                hint = "${FormValidators.LAT_MIN} a ${FormValidators.LAT_MAX}",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                onFocusLost = { viewModel.onLatitudFocusLost() },
                modifier = Modifier.weight(1f)
            )
            ValidatedTextField(
                field = state.longitud,
                onValueChange = { viewModel.onLongitudChanged(it) },
                label = "Longitud *",
                leadingIcon = Icons.Filled.Explore,
                hint = "${FormValidators.LON_MIN} a ${FormValidators.LON_MAX}",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                onFocusLost = { viewModel.onLongitudFocusLost() },
                modifier = Modifier.weight(1f)
            )
        }
        if (state.obteniendoGps) {
            Row {
                PulsingIcon(Icons.Filled.MyLocation, MaterialTheme.colorScheme.primary)
                Text("Obteniendo ubicación…", modifier = Modifier.padding(start = 12.dp, top = 10.dp))
            }
        } else {
            BigOutlinedButton(text = "Usar mi ubicación actual", icon = Icons.Filled.MyLocation, onClick = usarGps)
        }

        BigButton(
            text = "Guardar sucursal",
            icon = Icons.Filled.Save,
            onClick = { viewModel.onGuardarClick(onSuccess = onBack) },
            loading = state.guardando,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}
