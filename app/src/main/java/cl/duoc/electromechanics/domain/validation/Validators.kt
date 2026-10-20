package cl.duoc.electromechanics.domain.validation

import cl.duoc.electromechanics.domain.util.DateUtils
import java.time.LocalDate

data class ValidationResult(val isValid: Boolean, val errorMessage: String? = null) {
    companion object {
        val OK = ValidationResult(true)
        fun error(message: String) = ValidationResult(false, message)
    }
}

object RutValidator {
    fun clean(rut: String): String =
        rut.replace(".", "").replace("-", "").replace(" ", "").trim().uppercase()

    fun format(rut: String): String {
        val c = clean(rut)
        if (c.length < 2) return c
        val body = c.dropLast(1)
        val dv = c.last()
        val withDots = body.reversed().chunked(3).joinToString(".").reversed()
        return "$withDots-$dv"
    }

    fun computeDv(body: Long): Char {
        var n = body
        var sum = 0L
        var multiplier = 2
        while (n > 0) {
            sum += (n % 10) * multiplier
            n /= 10
            multiplier = if (multiplier == 7) 2 else multiplier + 1
        }
        return when (val r = 11 - (sum % 11).toInt()) {
            11 -> '0'
            10 -> 'K'
            else -> ('0' + r)
        }
    }

    fun validate(rut: String): ValidationResult {
        val c = clean(rut)
        if (c.isEmpty()) return ValidationResult.error("Ingresa el RUT")
        if (c.length !in 8..9) return ValidationResult.error("El RUT debe tener 7 u 8 dígitos más el dígito verificador")
        val body = c.dropLast(1)
        val dv = c.last()
        if (!body.all { it.isDigit() }) return ValidationResult.error("El RUT solo admite números antes del guion")
        if (!(dv.isDigit() || dv == 'K')) return ValidationResult.error("El dígito verificador debe ser un número o K")
        if (computeDv(body.toLong()) != dv) return ValidationResult.error("El dígito verificador no corresponde a este RUT")
        return ValidationResult.OK
    }
}

object FormValidators {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val NOMBRE_REGEX = Regex("^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ' ]+$")

    const val LAT_MIN = -34.30
    const val LAT_MAX = -32.90
    const val LON_MIN = -71.75
    const val LON_MAX = -69.75

    fun required(value: String, campo: String): ValidationResult =
        if (value.isBlank()) ValidationResult.error("$campo es obligatorio") else ValidationResult.OK

    fun text(value: String, campo: String, min: Int, max: Int, required: Boolean = true): ValidationResult {
        val v = value.trim()
        if (v.isEmpty()) {
            return if (required) ValidationResult.error("$campo es obligatorio") else ValidationResult.OK
        }
        if (v.length < min) return ValidationResult.error("$campo debe tener al menos $min caracteres")
        if (v.length > max) return ValidationResult.error("$campo no puede superar $max caracteres")
        return ValidationResult.OK
    }

    fun loginPassword(value: String): ValidationResult =
        if (value.isEmpty()) ValidationResult.error("Ingresa tu contraseña") else ValidationResult.OK

    fun nombreCompleto(value: String): ValidationResult {
        val base = text(value, "El nombre", 5, 60)
        if (!base.isValid) return base
        val v = value.trim()
        if (!NOMBRE_REGEX.matches(v)) return ValidationResult.error("El nombre solo admite letras y espacios")
        if (v.split(" ").filter { it.isNotBlank() }.size < 2) return ValidationResult.error("Ingresa nombre y apellido")
        return ValidationResult.OK
    }

    fun email(value: String): ValidationResult {
        val v = value.trim()
        if (v.isEmpty()) return ValidationResult.error("El correo es obligatorio")
        if (!EMAIL_REGEX.matches(v)) return ValidationResult.error("Formato de correo inválido (ej: nombre@empresa.cl)")
        return ValidationResult.OK
    }

    fun newPassword(value: String): ValidationResult {
        if (value.isEmpty()) return ValidationResult.error("La contraseña es obligatoria")
        if (value.length < 8) return ValidationResult.error("Debe tener al menos 8 caracteres")
        if (!value.any { it.isLetter() }) return ValidationResult.error("Debe incluir al menos una letra")
        if (!value.any { it.isDigit() }) return ValidationResult.error("Debe incluir al menos un número")
        return ValidationResult.OK
    }

    fun confirmPassword(password: String, confirm: String): ValidationResult = when {
        confirm.isEmpty() -> ValidationResult.error("Confirma la contraseña")
        password != confirm -> ValidationResult.error("Las contraseñas no coinciden")
        else -> ValidationResult.OK
    }

    fun passwordStrength(value: String): Int {
        var score = 0
        if (value.length >= 8) score++
        if (value.any { it.isDigit() } && value.any { it.isLetter() }) score++
        if (value.any { it.isUpperCase() } && value.any { it.isLowerCase() }) score++
        if (value.any { !it.isLetterOrDigit() }) score++
        return score
    }

    fun latitud(value: String): ValidationResult {
        val v = value.trim().replace(",", ".")
        if (v.isEmpty()) return ValidationResult.error("La latitud es obligatoria")
        val d = v.toDoubleOrNull() ?: return ValidationResult.error("Ingresa un número decimal (ej: -33.51)")
        if (d !in LAT_MIN..LAT_MAX) return ValidationResult.error("Fuera de la Región Metropolitana ($LAT_MIN a $LAT_MAX)")
        return ValidationResult.OK
    }

    fun longitud(value: String): ValidationResult {
        val v = value.trim().replace(",", ".")
        if (v.isEmpty()) return ValidationResult.error("La longitud es obligatoria")
        val d = v.toDoubleOrNull() ?: return ValidationResult.error("Ingresa un número decimal (ej: -70.75)")
        if (d !in LON_MIN..LON_MAX) return ValidationResult.error("Fuera de la Región Metropolitana ($LON_MIN a $LON_MAX)")
        return ValidationResult.OK
    }

    fun fechaProgramada(value: String, today: LocalDate = LocalDate.now()): ValidationResult {
        if (value.isBlank()) return ValidationResult.error("La fecha es obligatoria")
        val date = DateUtils.parseFecha(value)
            ?: return ValidationResult.error("Usa el formato dd/mm/aaaa con una fecha real")
        if (date.isBefore(today)) return ValidationResult.error("La fecha no puede ser anterior a hoy")
        if (date.isAfter(today.plusYears(1))) return ValidationResult.error("La fecha no puede superar un año")
        return ValidationResult.OK
    }

    fun <T> selection(value: T?, campo: String): ValidationResult =
        if (value == null) ValidationResult.error("Selecciona $campo") else ValidationResult.OK
}
