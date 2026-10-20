package cl.duoc.electromechanics.domain

import cl.duoc.electromechanics.domain.rules.JornadaRules
import cl.duoc.electromechanics.domain.rules.ServicioRules
import cl.duoc.electromechanics.domain.util.GeoUtils
import cl.duoc.electromechanics.domain.validation.FormValidators
import cl.duoc.electromechanics.domain.validation.RutValidator
import cl.duoc.electromechanics.model.EstadoServicio
import cl.duoc.electromechanics.model.TipoMarcacion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ValidatorsTest {
    @Test
    fun rut_valido_con_y_sin_formato() {
        assertTrue(RutValidator.validate("12.345.678-5").isValid)
        assertTrue(RutValidator.validate("123456785").isValid)
        assertTrue(RutValidator.validate("11111111-1").isValid)
    }

    @Test
    fun rut_con_dv_incorrecto_es_invalido() {
        assertFalse(RutValidator.validate("12.345.678-9").isValid)
        assertFalse(RutValidator.validate("").isValid)
        assertFalse(RutValidator.validate("12A45678-5").isValid)
    }

    @Test
    fun rut_se_formatea_con_puntos_y_guion() {
        assertEquals("12.345.678-5", RutValidator.format("123456785"))
        assertEquals("1.234.567-4", RutValidator.format("12345674"))
    }

    @Test
    fun correo_valida_formato() {
        assertTrue(FormValidators.email("tecnico.alfa@emdemo.cl").isValid)
        assertFalse(FormValidators.email("tecnico@").isValid)
    }

    @Test
    fun contrasena_nueva_requiere_letras_y_numeros() {
        assertTrue(FormValidators.newPassword("Demo2026!").isValid)
        assertFalse(FormValidators.newPassword("soloLetras").isValid)
        assertFalse(FormValidators.newPassword("123").isValid)
    }

    @Test
    fun coordenadas_deben_estar_en_la_region_metropolitana() {
        assertTrue(FormValidators.latitud("-33.51").isValid)
        assertFalse(FormValidators.latitud("-23.65").isValid)
        assertFalse(FormValidators.longitud("abc").isValid)
    }

    @Test
    fun fecha_programada_no_puede_ser_pasada() {
        val hoy = LocalDate.of(2026, 10, 20)
        assertTrue(FormValidators.fechaProgramada("21/10/2026", hoy).isValid)
        assertFalse(FormValidators.fechaProgramada("19/10/2026", hoy).isValid)
        assertFalse(FormValidators.fechaProgramada("31/02/2026", hoy).isValid)
    }

    @Test
    fun no_se_puede_iniciar_servicio_sin_jornada_activa() {
        val req = ServicioRules.requisitosParaIniciar(EstadoServicio.PENDIENTE, jornadaActiva = false, tieneFotoInicial = true)
        assertFalse(ServicioRules.cumpleTodos(req))
    }

    @Test
    fun siguiente_marcacion_alterna_entrada_y_salida() {
        assertEquals(TipoMarcacion.ENTRADA, JornadaRules.siguienteMarcacion(null))
        assertEquals(TipoMarcacion.SALIDA, JornadaRules.siguienteMarcacion(TipoMarcacion.ENTRADA))
        assertEquals(TipoMarcacion.ENTRADA, JornadaRules.siguienteMarcacion(TipoMarcacion.SALIDA))
    }

    @Test
    fun horas_trabajadas_suma_pares_entrada_salida() {
        val h = 3_600_000L
        val marcas = listOf(
            TipoMarcacion.ENTRADA to 0L,
            TipoMarcacion.SALIDA to 4 * h,
            TipoMarcacion.ENTRADA to 5 * h,
            TipoMarcacion.SALIDA to 9 * h
        )
        assertEquals(8 * h, JornadaRules.horasTrabajadas(marcas, ahora = 10 * h))
    }

    @Test
    fun distancia_haversine_aproximada() {
        val d = GeoUtils.distanceMeters(-33.50, -70.75, -33.51, -70.75)
        assertTrue(d in 1_000.0..1_200.0)
    }
}
