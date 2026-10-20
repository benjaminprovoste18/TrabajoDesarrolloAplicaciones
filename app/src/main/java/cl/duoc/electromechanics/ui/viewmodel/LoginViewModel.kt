package cl.duoc.electromechanics.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.electromechanics.data.repository.AuthRepository
import cl.duoc.electromechanics.data.repository.LoginResult
import cl.duoc.electromechanics.device.Vibrador
import cl.duoc.electromechanics.domain.rules.LoginRules
import cl.duoc.electromechanics.domain.validation.FormValidators
import cl.duoc.electromechanics.domain.validation.RutValidator
import cl.duoc.electromechanics.model.FieldState
import cl.duoc.electromechanics.model.LoginModel
import cl.duoc.electromechanics.model.Rol
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val vibrador: Vibrador
) : ViewModel() {
    var uiState by mutableStateOf(LoginModel())
        private set

    fun onRutChanged(rut: String) {
        val filtrado = rut.filter { it.isDigit() || it == 'k' || it == 'K' || it == '.' || it == '-' }.take(12)
        uiState = uiState.copy(rut = uiState.rut.withValue(filtrado, RutValidator::validate), errorGeneral = null)
    }

    fun onRutFocusLost() {
        val resultado = RutValidator.validate(uiState.rut.value)
        val valor = if (resultado.isValid) RutValidator.format(uiState.rut.value) else uiState.rut.value
        val error = if (uiState.rut.value.isEmpty()) null else resultado.errorMessage
        uiState = uiState.copy(rut = FieldState(valor, error))
    }

    fun onPasswordChanged(password: String) {
        uiState = uiState.copy(
            password = uiState.password.withValue(password, FormValidators::loginPassword),
            errorGeneral = null
        )
    }

    fun onTogglePasswordVisible() {
        uiState = uiState.copy(passwordVisible = !uiState.passwordVisible)
    }

    fun onLoginClick(onSuccess: (Rol) -> Unit) {
        if (uiState.loading || uiState.bloqueadoSegundos > 0) return

        val rut = uiState.rut.validated(RutValidator::validate)
        val password = uiState.password.validated(FormValidators::loginPassword)
        if (rut.hasError || password.hasError) {
            vibrador.error()
            uiState = uiState.copy(rut = rut, password = password, shakeTrigger = uiState.shakeTrigger + 1)
            return
        }

        uiState = uiState.copy(rut = rut, password = password, loading = true, errorGeneral = null)
        viewModelScope.launch {
            when (val resultado = authRepository.login(rut.value, password.value)) {
                is LoginResult.Success -> {
                    uiState = uiState.copy(loading = false)
                    onSuccess(resultado.sesion.rol)
                }
                LoginResult.InactiveUser -> fallo("Tu cuenta está desactivada. Contacta al administrador.", contarIntento = false)
                LoginResult.InvalidCredentials -> fallo("RUT o contraseña incorrectos", contarIntento = true)
            }
        }
    }

    private fun fallo(mensaje: String, contarIntento: Boolean) {
        vibrador.error()
        val intentos = uiState.intentosFallidos + if (contarIntento) 1 else 0
        uiState = uiState.copy(
            loading = false,
            errorGeneral = mensaje,
            password = FieldState(),
            intentosFallidos = intentos,
            shakeTrigger = uiState.shakeTrigger + 1
        )
        if (LoginRules.debeBloquear(intentos)) iniciarBloqueo()
    }

    private fun iniciarBloqueo() {
        viewModelScope.launch {
            var segundos = (LoginRules.BLOQUEO_MS / 1000).toInt()
            while (segundos > 0) {
                uiState = uiState.copy(
                    bloqueadoSegundos = segundos,
                    errorGeneral = "Demasiados intentos fallidos. Espera $segundos s para reintentar."
                )
                delay(1000)
                segundos--
            }
            uiState = uiState.copy(bloqueadoSegundos = 0, intentosFallidos = 0, errorGeneral = null)
        }
    }
}
