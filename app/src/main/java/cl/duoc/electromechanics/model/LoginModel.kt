package cl.duoc.electromechanics.model

data class LoginModel(
    val rut: FieldState = FieldState(),
    val password: FieldState = FieldState(),
    val passwordVisible: Boolean = false,
    val loading: Boolean = false,
    val errorGeneral: String? = null,
    val shakeTrigger: Int = 0,
    val intentosFallidos: Int = 0,
    val bloqueadoSegundos: Int = 0
)
