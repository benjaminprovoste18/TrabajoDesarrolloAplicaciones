package cl.duoc.electromechanics.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.electromechanics.data.repository.EmpleadoRepository
import cl.duoc.electromechanics.domain.validation.FormValidators
import cl.duoc.electromechanics.domain.validation.RutValidator
import cl.duoc.electromechanics.domain.validation.ValidationResult
import cl.duoc.electromechanics.model.Especialidades
import cl.duoc.electromechanics.model.Rol
import cl.duoc.electromechanics.model.UsuarioFormModel
import kotlinx.coroutines.launch

class UsuarioFormViewModel(private val repository: EmpleadoRepository) : ViewModel() {
    var uiState by mutableStateOf(UsuarioFormModel())
        private set

    private fun validarConfirmacion(v: String) = FormValidators.confirmPassword(uiState.password.value, v)

    private fun validarEspecialidad(s: UsuarioFormModel): ValidationResult =
        if (s.requiereEspecialidad && s.especialidad == null) ValidationResult.error("Selecciona la especialidad técnica")
        else ValidationResult.OK

    fun onRutChanged(v: String) {
        val filtrado = v.filter { it.isDigit() || it == 'k' || it == 'K' || it == '.' || it == '-' }.take(12)
        uiState = uiState.copy(rut = uiState.rut.withValue(filtrado, RutValidator::validate))
    }

    fun onRutFocusLost() {
        val rut = uiState.rut.validated(RutValidator::validate)
        uiState = uiState.copy(rut = if (rut.hasError) rut else rut.copy(value = RutValidator.format(rut.value)))
    }

    fun onNombreChanged(v: String) {
        uiState = uiState.copy(nombre = uiState.nombre.withValue(v, FormValidators::nombreCompleto))
    }

    fun onNombreFocusLost() {
        uiState = uiState.copy(nombre = uiState.nombre.validated(FormValidators::nombreCompleto))
    }

    fun onCorreoChanged(v: String) {
        uiState = uiState.copy(correo = uiState.correo.withValue(v.trim(), FormValidators::email))
    }

    fun onCorreoFocusLost() {
        uiState = uiState.copy(correo = uiState.correo.validated(FormValidators::email))
    }

    fun onRolChanged(rol: Rol) {
        val especialidad = if (rol == Rol.EMPLEADO) {
            uiState.especialidad?.takeIf { it != Especialidades.NO_APLICA }
        } else {
            Especialidades.NO_APLICA
        }
        uiState = uiState.copy(rol = rol, rolError = null, especialidad = especialidad, especialidadError = null)
    }

    fun onEspecialidadChanged(especialidad: String) {
        uiState = uiState.copy(especialidad = especialidad, especialidadError = null)
    }

    fun onPasswordChanged(v: String) {
        val confirmacion = if (uiState.confirmacion.value.isNotEmpty() && uiState.confirmacion.hasError) {
            uiState.confirmacion.copy(error = FormValidators.confirmPassword(v, uiState.confirmacion.value).errorMessage)
        } else {
            uiState.confirmacion
        }
        uiState = uiState.copy(
            password = uiState.password.withValue(v, FormValidators::newPassword),
            confirmacion = confirmacion
        )
    }

    fun onPasswordFocusLost() {
        uiState = uiState.copy(password = uiState.password.validated(FormValidators::newPassword))
    }

    fun onConfirmacionChanged(v: String) {
        uiState = uiState.copy(confirmacion = uiState.confirmacion.withValue(v, ::validarConfirmacion))
    }

    fun onConfirmacionFocusLost() {
        uiState = uiState.copy(confirmacion = uiState.confirmacion.validated(::validarConfirmacion))
    }

    fun onTogglePasswordVisible() {
        uiState = uiState.copy(passwordVisible = !uiState.passwordVisible)
    }

    fun onMensajeMostrado() {
        uiState = uiState.copy(mensaje = null)
    }

    fun onGuardarClick(onSuccess: () -> Unit) {
        if (uiState.guardando) return
        val inicial = uiState
        var v = inicial.copy(
            rut = inicial.rut.validated(RutValidator::validate),
            nombre = inicial.nombre.validated(FormValidators::nombreCompleto),
            correo = inicial.correo.validated(FormValidators::email),
            rolError = FormValidators.selection(inicial.rol, "el rol del usuario").errorMessage,
            especialidadError = validarEspecialidad(inicial).errorMessage,
            password = inicial.password.validated(FormValidators::newPassword),
            confirmacion = inicial.confirmacion.validated { FormValidators.confirmPassword(inicial.password.value, it) }
        )
        uiState = uiState.copy(guardando = true)

        viewModelScope.launch {
            if (!v.rut.hasError && repository.existeRut(v.rut.value)) {
                v = v.copy(rut = v.rut.copy(error = "Ya existe un usuario con este RUT"))
            }
            if (!v.correo.hasError && repository.existeCorreo(v.correo.value)) {
                v = v.copy(correo = v.correo.copy(error = "Este correo ya está registrado"))
            }
            val hayErrores = v.rut.hasError || v.nombre.hasError || v.correo.hasError || v.rolError != null ||
                v.especialidadError != null || v.password.hasError || v.confirmacion.hasError
            if (hayErrores) {
                uiState = v.copy(
                    guardando = false,
                    shakeTrigger = inicial.shakeTrigger + 1,
                    mensaje = "Revisa los campos marcados en rojo"
                )
                return@launch
            }
            repository.crear(
                rut = v.rut.value,
                nombre = v.nombre.value,
                correo = v.correo.value,
                rol = v.rol!!,
                especialidad = v.especialidad ?: Especialidades.NO_APLICA,
                password = v.password.value
            )
            uiState = v.copy(guardando = false)
            onSuccess()
        }
    }
}
