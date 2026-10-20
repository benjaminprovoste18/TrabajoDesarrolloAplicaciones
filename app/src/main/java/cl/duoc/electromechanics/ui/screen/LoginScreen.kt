package cl.duoc.electromechanics.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.electromechanics.data.seed.DatabaseSeeder
import cl.duoc.electromechanics.model.Rol
import cl.duoc.electromechanics.ui.components.BigButton
import cl.duoc.electromechanics.ui.components.ErrorBanner
import cl.duoc.electromechanics.ui.components.ValidatedTextField
import cl.duoc.electromechanics.ui.components.shake
import cl.duoc.electromechanics.ui.theme.Amber
import cl.duoc.electromechanics.ui.theme.Navy
import cl.duoc.electromechanics.ui.theme.NavyLight
import cl.duoc.electromechanics.ui.viewmodel.AppViewModelProvider
import cl.duoc.electromechanics.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoggedIn: (Rol) -> Unit,
    viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state = viewModel.uiState
    val focusManager = LocalFocusManager.current
    var showDemo by rememberSaveable { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Navy, NavyLight)))
            .systemBarsPadding()
            .imePadding()
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier
                    .size(84.dp)
                    .background(Color.White.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Bolt, contentDescription = null, tint = Amber, modifier = Modifier.size(56.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text("EM Terreno", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
            Text(
                "Electromechanics Ingeniería SpA",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shake(state.shakeTrigger),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Iniciar sesión", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Ingresa con el RUT y la contraseña de tu cuenta de operario",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))

                    ValidatedTextField(
                        field = state.rut,
                        onValueChange = { viewModel.onRutChanged(it) },
                        label = "RUT",
                        leadingIcon = Icons.Filled.Badge,
                        hint = "Ej: 12.345.678-5",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        onFocusLost = { viewModel.onRutFocusLost() }
                    )

                    ValidatedTextField(
                        field = state.password,
                        onValueChange = { viewModel.onPasswordChanged(it) },
                        label = "Contraseña",
                        leadingIcon = Icons.Filled.Lock,
                        showSuccess = false,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            viewModel.onLoginClick(onSuccess = onLoggedIn)
                        }),
                        visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { viewModel.onTogglePasswordVisible() }) {
                                Icon(
                                    if (state.passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (state.passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                                )
                            }
                        }
                    )

                    ErrorBanner(state.errorGeneral)
                    Spacer(Modifier.height(4.dp))

                    BigButton(
                        text = if (state.bloqueadoSegundos > 0) "Bloqueado (${state.bloqueadoSegundos} s)" else "Ingresar",
                        icon = Icons.AutoMirrored.Filled.Login,
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.onLoginClick(onSuccess = onLoggedIn)
                        },
                        loading = state.loading,
                        enabled = state.bloqueadoSegundos == 0
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            DemoCredentialsCard(expanded = showDemo, onToggle = { showDemo = !showDemo })
        }
    }
}

@Composable
private fun DemoCredentialsCard(expanded: Boolean, onToggle: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f), contentColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            TextButton(onClick = onToggle, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = Color.White)
                Spacer(Modifier.size(8.dp))
                Text("Usuarios de prueba (datos ficticios)", color = Color.White, modifier = Modifier.weight(1f))
                Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null, tint = Color.White)
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    DemoRow("Técnico", "33.333.333-3")
                    DemoRow("Supervisor", "22.222.222-2")
                    DemoRow("Administrador", "11.111.111-1")
                    Text(
                        "Contraseña para todos: ${DatabaseSeeder.DEMO_PASSWORD}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoRow(perfil: String, rut: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(perfil, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(rut, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
