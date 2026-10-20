package cl.duoc.electromechanics.model

enum class Rol(val etiqueta: String) {
    EMPLEADO("Técnico"),
    SUPERVISOR("Supervisor"),
    ADMIN("Administrador");

    companion object {
        fun from(value: String?): Rol? = entries.firstOrNull { it.name == value }
    }
}

enum class EstadoServicio(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    EN_PROCESO("En Proceso"),
    FINALIZADO("Finalizado");

    companion object {
        fun from(value: String): EstadoServicio =
            entries.firstOrNull { it.name == value } ?: PENDIENTE
    }
}

enum class TipoMarcacion(val etiqueta: String) {
    ENTRADA("Entrada"),
    SALIDA("Salida");

    companion object {
        fun from(value: String): TipoMarcacion =
            entries.firstOrNull { it.name == value } ?: ENTRADA
    }
}

enum class Prioridad(val etiqueta: String) {
    NORMAL("Normal"),
    URGENTE("Urgente");

    companion object {
        fun from(value: String): Prioridad =
            entries.firstOrNull { it.name == value } ?: NORMAL
    }
}

enum class EstadoEquipo(val etiqueta: String) {
    OPERATIVO("Operativo"),
    CON_OBSERVACIONES("Operativo con observaciones"),
    FUERA_DE_SERVICIO("Fuera de servicio");

    companion object {
        fun from(value: String): EstadoEquipo =
            entries.firstOrNull { it.name == value } ?: OPERATIVO
    }
}

object Especialidades {
    const val NO_APLICA = "No aplica"
    val tecnicas = listOf(
        "Eléctrica",
        "Mecánica",
        "Electromecánica",
        "Instrumentación y control",
        "Climatización (HVAC)"
    )
}
