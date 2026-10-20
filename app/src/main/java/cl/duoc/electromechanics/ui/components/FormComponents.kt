package cl.duoc.electromechanics.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import cl.duoc.electromechanics.model.FieldState
import cl.duoc.electromechanics.ui.theme.SuccessGreen
import kotlin.math.roundToInt

@Composable
fun ValidatedTextField(
    field: FieldState,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    hint: String? = null,
    maxChars: Int? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    showSuccess: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null,
    onFocusLost: (() -> Unit)? = null
) {
    var hadFocus by remember { mutableStateOf(false) }
    val iconTint by animateColorAsState(
        targetValue = if (field.hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        label = "iconTint"
    )

    OutlinedTextField(
        value = field.value,
        onValueChange = { newValue ->
            if (maxChars == null || newValue.length <= maxChars) onValueChange(newValue)
        },
        label = { Text(label) },
        leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = iconTint) },
        trailingIcon = {
            if (trailingIcon != null) {
                trailingIcon()
            } else {
                FieldStatusIcon(field = field, showSuccess = showSuccess && !readOnly)
            }
        },
        isError = field.hasError,
        supportingText = {
            Row(Modifier.fillMaxWidth()) {
                AnimatedContent(
                    targetState = field.error ?: hint.orEmpty(),
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                    modifier = Modifier.weight(1f),
                    label = "supporting"
                ) { text -> Text(text) }
                if (maxChars != null) {
                    Text(
                        "${field.value.length}/$maxChars",
                        textAlign = TextAlign.End,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        },
        singleLine = singleLine,
        minLines = minLines,
        readOnly = readOnly,
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { state ->
                if (state.isFocused) {
                    hadFocus = true
                } else if (hadFocus) {
                    hadFocus = false
                    onFocusLost?.invoke()
                }
            }
    )
}

@Composable
private fun FieldStatusIcon(field: FieldState, showSuccess: Boolean) {
    AnimatedContent(
        targetState = when {
            field.hasError -> 1
            showSuccess && field.value.isNotBlank() -> 2
            else -> 0
        },
        transitionSpec = { scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut() },
        label = "trailingState"
    ) { state ->
        when (state) {
            1 -> Icon(Icons.Filled.Error, contentDescription = "Campo con error", tint = MaterialTheme.colorScheme.error)
            2 -> Icon(Icons.Filled.CheckCircle, contentDescription = "Campo válido", tint = SuccessGreen.copy(alpha = 0.7f))
            else -> Spacer(Modifier.size(24.dp))
        }
    }
}

@Composable
fun <T> DropdownField(
    label: String,
    options: List<T>,
    selected: T?,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit,
    leadingIcon: ImageVector,
    error: String?,
    modifier: Modifier = Modifier,
    hint: String? = null,
    optionSupporting: ((T) -> String)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier.fillMaxWidth()) {
        ValidatedTextField(
            field = FieldState(selected?.let(optionLabel) ?: "", error),
            onValueChange = {},
            label = label,
            leadingIcon = leadingIcon,
            hint = hint,
            readOnly = true,
            trailingIcon = {
                Icon(
                    if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = if (expanded) "Cerrar opciones" else "Abrir opciones"
                )
            }
        )
        Box(
            Modifier
                .matchParentSize()
                .semantics { contentDescription = "Seleccionar $label" }
                .clickable { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (options.isEmpty()) {
                DropdownMenuItem(text = { Text("No hay opciones disponibles") }, onClick = { expanded = false })
            }
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(optionLabel(option), style = MaterialTheme.typography.bodyLarge)
                            optionSupporting?.let {
                                Text(it(option), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                )
            }
        }
    }
}

@Composable
fun BigButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
    ) {
        AnimatedContent(targetState = loading, label = "buttonLoading") { isLoading ->
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(26.dp),
                    strokeWidth = 3.dp,
                    color = contentColor
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(26.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(text, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun BigOutlinedButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun ErrorBanner(message: String?, modifier: Modifier = Modifier) {
    val lastMessage = remember { arrayOf("") }
    if (message != null) lastMessage[0] = message
    AnimatedVisibility(
        visible = message != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ErrorOutline, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text(lastMessage[0], style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun PasswordStrengthBar(strength: Int, modifier: Modifier = Modifier) {
    val progress by animateFloatAsState(targetValue = strength / 4f, animationSpec = tween(400), label = "strength")
    val color by animateColorAsState(
        targetValue = when (strength) {
            0, 1 -> MaterialTheme.colorScheme.error
            2 -> Color(0xFFE09B00)
            else -> SuccessGreen
        },
        label = "strengthColor"
    )
    val label = when (strength) {
        0 -> "Muy débil"
        1 -> "Débil"
        2 -> "Aceptable"
        3 -> "Segura"
        else -> "Muy segura"
    }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LinearProgressIndicator(
            progress = { progress },
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
        Text("Seguridad: $label", style = MaterialTheme.typography.bodySmall, color = color)
    }
}

fun Modifier.shake(trigger: Int): Modifier = composed {
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger > 0) {
            for (target in listOf(18f, -18f, 12f, -12f, 6f, -6f, 0f)) {
                shakeOffset.animateTo(target, tween(durationMillis = 45))
            }
        }
    }
    this.offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
}
