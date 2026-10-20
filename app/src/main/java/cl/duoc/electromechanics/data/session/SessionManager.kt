package cl.duoc.electromechanics.data.session

import android.content.Context
import cl.duoc.electromechanics.model.Rol

data class Sesion(val idEmpleado: String, val rol: Rol, val nombre: String)

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun guardar(sesion: Sesion) {
        prefs.edit()
            .putString(KEY_ID, sesion.idEmpleado)
            .putString(KEY_ROL, sesion.rol.name)
            .putString(KEY_NOMBRE, sesion.nombre)
            .apply()
    }

    fun actual(): Sesion? {
        val id = prefs.getString(KEY_ID, null) ?: return null
        val rol = Rol.from(prefs.getString(KEY_ROL, null)) ?: return null
        val nombre = prefs.getString(KEY_NOMBRE, "") ?: ""
        return Sesion(id, rol, nombre)
    }

    fun cerrar() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val PREFS = "em_sesion"
        const val KEY_ID = "id_empleado"
        const val KEY_ROL = "rol"
        const val KEY_NOMBRE = "nombre"
    }
}
