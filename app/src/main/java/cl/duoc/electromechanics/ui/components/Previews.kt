package cl.duoc.electromechanics.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.electromechanics.domain.rules.Requisito
import cl.duoc.electromechanics.model.EstadoServicio
import cl.duoc.electromechanics.model.FieldState
import cl.duoc.electromechanics.ui.theme.EMTheme

@Preview(showBackground = true, name = "Campo con error y campo válido")
@Composable
fun ValidatedTextFieldPreview() {
    EMTheme {
        Surface {
            Column(Modifier.padding(16.dp)) {
                ValidatedTextField(
                    field = FieldState("12.345.678-9", "El dígito verificador no corresponde a este RUT"),
                    onValueChange = {},
                    label = "RUT",
                    leadingIcon = Icons.Filled.Badge
                )
                ValidatedTextField(
                    field = FieldState("12.345.678-5"),
                    onValueChange = {},
                    label = "RUT",
                    leadingIcon = Icons.Filled.Badge,
                    hint = "Ej: 12.345.678-5"
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Estados y botón principal")
@Composable
fun EstadosPreview() {
    EMTheme {
        Surface {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EstadoServicio.entries.forEach { EstadoChip(it) }
                }
                UrgenteBadge()
                RequisitosChecklist(
                    listOf(
                        Requisito("Jornada iniciada (marcación de entrada)", true),
                        Requisito("Foto del estado inicial del equipo", false)
                    )
                )
                BigButton(text = "Marcar entrada", icon = Icons.Filled.MyLocation, onClick = {})
            }
        }
    }
}
