package cl.duoc.electromechanics.domain.util

import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object PasswordHasher {
    private const val PEPPER = "EMT-v1"

    fun hash(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("$PEPPER:$salt:$password".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun matches(password: String, salt: String, expectedHash: String): Boolean =
        hash(password, salt) == expectedHash
}

object GeoUtils {
    private const val EARTH_RADIUS_M = 6_371_000.0

    fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_M * c
    }

    fun formatDistance(meters: Double): String =
        if (meters < 1000) "${meters.toInt()} m" else String.format(Locale.US, "%.1f km", meters / 1000)

    fun formatCoord(value: Double): String = String.format(Locale.US, "%.5f", value)
}

object DateUtils {
    private val zone: ZoneId get() = ZoneId.systemDefault()
    private val locale: Locale = Locale.forLanguageTag("es-CL")
    private val fechaFmt = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)
    private val horaFmt = DateTimeFormatter.ofPattern("HH:mm")
    private val fechaHoraFmt = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm:ss")
    private val diaFmt = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", locale)

    fun startOfToday(): Long = startOfDay(System.currentTimeMillis())

    fun startOfDay(millis: Long): Long =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli()

    fun toLocalDate(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    fun localDateToMillis(date: LocalDate): Long = date.atStartOfDay(zone).toInstant().toEpochMilli()

    fun parseFecha(text: String): LocalDate? = try {
        LocalDate.parse(text.trim(), fechaFmt)
    } catch (e: DateTimeParseException) {
        null
    }

    fun formatFecha(date: LocalDate): String = date.format(fechaFmt)
    fun formatFecha(millis: Long): String = toLocalDate(millis).format(fechaFmt)
    fun formatHora(millis: Long): String = Instant.ofEpochMilli(millis).atZone(zone).format(horaFmt)
    fun formatFechaHora(millis: Long): String = Instant.ofEpochMilli(millis).atZone(zone).format(fechaHoraFmt)

    fun etiquetaDia(millis: Long): String {
        val date = toLocalDate(millis)
        val today = LocalDate.now(zone)
        return when (date) {
            today -> "Hoy"
            today.plusDays(1) -> "Mañana"
            today.minusDays(1) -> "Ayer"
            else -> date.format(diaFmt).replaceFirstChar { it.uppercase() }
        }
    }

    fun formatDuracion(millis: Long): String {
        val totalMin = millis / 60_000
        return "${totalMin / 60} h ${totalMin % 60} min"
    }
}
