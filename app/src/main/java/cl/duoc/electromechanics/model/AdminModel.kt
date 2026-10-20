package cl.duoc.electromechanics.model

import cl.duoc.electromechanics.data.local.entity.EmpleadoEntity
import cl.duoc.electromechanics.data.local.entity.SucursalEntity
import cl.duoc.electromechanics.domain.validation.FormValidators

data class AdminModel(
    val nombre: String = "",
    val idActual: String = "",
    val usuarios: List<EmpleadoEntity> = emptyList(),
    val sucursales: List<SucursalEntity> = emptyList(),
    val mensaje: String? = null
)

data class UsuarioFormModel(
    val rut: FieldState = FieldState(),
    val nombre: FieldState = FieldState(),
    val correo: FieldState = FieldState(),
    val rol: Rol? = null,
    val rolError: String? = null,
    val especialidad: String? = null,
    val especialidadError: String? = null,
    val password: FieldState = FieldState(),
    val confirmacion: FieldState = FieldState(),
    val passwordVisible: Boolean = false,
    val guardando: Boolean = false,
    val shakeTrigger: Int = 0,
    val mensaje: String? = null
) {
    val fuerzaPassword: Int get() = FormValidators.passwordStrength(password.value)
    val requiereEspecialidad: Boolean get() = rol == Rol.EMPLEADO
}

data class SucursalFormModel(
    val nombre: FieldState = FieldState(),
    val direccion: FieldState = FieldState(),
    val latitud: FieldState = FieldState(),
    val longitud: FieldState = FieldState(),
    val obteniendoGps: Boolean = false,
    val guardando: Boolean = false,
    val shakeTrigger: Int = 0,
    val mensaje: String? = null
)
