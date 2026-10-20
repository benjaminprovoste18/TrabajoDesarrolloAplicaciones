package cl.duoc.electromechanics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.duoc.electromechanics.ui.navigation.AppNavigation
import cl.duoc.electromechanics.ui.theme.EMTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EMTheme {
                AppNavigation()
            }
        }
    }
}
