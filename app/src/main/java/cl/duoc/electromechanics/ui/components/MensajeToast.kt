package cl.duoc.electromechanics.ui.components

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun MensajeToast(mensaje: String?, onMostrado: () -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
            onMostrado()
        }
    }
}
