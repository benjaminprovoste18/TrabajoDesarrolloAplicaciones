package cl.duoc.electromechanics.data.local

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class AppDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    private val cambios = MutableStateFlow(0L)

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE empleado (
                id_empleado TEXT PRIMARY KEY NOT NULL,
                rut_trabajador TEXT NOT NULL UNIQUE,
                nombre_completo TEXT NOT NULL,
                rol TEXT NOT NULL,
                especialidad_tecnica TEXT NOT NULL,
                correo_corporativo TEXT NOT NULL,
                password_hash TEXT NOT NULL,
                activo INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE sucursal (
                id_sucursal TEXT PRIMARY KEY NOT NULL,
                nombre_cliente TEXT NOT NULL,
                direccion_sucursal TEXT NOT NULL,
                latitud_ref REAL NOT NULL,
                longitud_ref REAL NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE orden_trabajo (
                id_servicio TEXT PRIMARY KEY NOT NULL,
                id_empleado_asignado TEXT NOT NULL REFERENCES empleado(id_empleado),
                id_sucursal_cliente TEXT NOT NULL REFERENCES sucursal(id_sucursal),
                fecha_programada INTEGER NOT NULL,
                estado TEXT NOT NULL,
                descripcion_trabajo TEXT NOT NULL,
                actividades_requeridas TEXT NOT NULL,
                prioridad TEXT NOT NULL,
                foto_inicial_path TEXT,
                foto_final_path TEXT,
                hora_inicio INTEGER,
                hora_termino INTEGER,
                aprobada INTEGER NOT NULL DEFAULT 0,
                creada_en INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE registro_actividad (
                id_actividad TEXT PRIMARY KEY NOT NULL,
                id_servicio TEXT NOT NULL REFERENCES orden_trabajo(id_servicio) ON DELETE CASCADE,
                detalle_tarea_ejecutada TEXT NOT NULL,
                repuestos_utilizados TEXT NOT NULL,
                estado_equipo TEXT NOT NULL,
                hora_registro INTEGER NOT NULL,
                comentarios_tecnicos TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE registro_jornada (
                id_registro TEXT PRIMARY KEY NOT NULL,
                id_empleado TEXT NOT NULL REFERENCES empleado(id_empleado),
                tipo_marcacion TEXT NOT NULL,
                fecha_hora_marcacion INTEGER NOT NULL,
                latitud_marcacion REAL NOT NULL,
                longitud_marcacion REAL NOT NULL,
                precision_metros REAL NOT NULL,
                foto_respaldo_path TEXT,
                id_sucursal_cercana TEXT REFERENCES sucursal(id_sucursal),
                distancia_metros REAL,
                dentro_de_radio INTEGER NOT NULL DEFAULT 0,
                ubicacion_simulada INTEGER NOT NULL DEFAULT 0,
                aprobado INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_orden_empleado ON orden_trabajo(id_empleado_asignado)")
        db.execSQL("CREATE INDEX idx_orden_sucursal ON orden_trabajo(id_sucursal_cliente)")
        db.execSQL("CREATE INDEX idx_actividad_servicio ON registro_actividad(id_servicio)")
        db.execSQL("CREATE INDEX idx_jornada_empleado ON registro_jornada(id_empleado)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        listOf("registro_jornada", "registro_actividad", "orden_trabajo", "sucursal", "empleado")
            .forEach { db.execSQL("DROP TABLE IF EXISTS $it") }
        onCreate(db)
    }

    suspend fun <T> leer(consulta: (SQLiteDatabase) -> T): T =
        withContext(Dispatchers.IO) { consulta(readableDatabase) }

    suspend fun <T> escribir(operacion: (SQLiteDatabase) -> T): T = withContext(Dispatchers.IO) {
        val db = writableDatabase
        db.beginTransaction()
        val resultado = try {
            val r = operacion(db)
            db.setTransactionSuccessful()
            r
        } finally {
            db.endTransaction()
        }
        cambios.update { it + 1 }
        resultado
    }

    fun <T> observar(consulta: (SQLiteDatabase) -> T): Flow<T> =
        cambios.map { consulta(readableDatabase) }.flowOn(Dispatchers.IO)

    companion object {
        private const val DB_NAME = "electromechanics_terreno.db"
        private const val DB_VERSION = 1

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: AppDatabase(context.applicationContext).also { instance = it }
            }
    }
}

fun Cursor.str(col: String): String = getString(getColumnIndexOrThrow(col))
fun Cursor.strOrNull(col: String): String? = getColumnIndexOrThrow(col).let { if (isNull(it)) null else getString(it) }
fun Cursor.long(col: String): Long = getLong(getColumnIndexOrThrow(col))
fun Cursor.longOrNull(col: String): Long? = getColumnIndexOrThrow(col).let { if (isNull(it)) null else getLong(it) }
fun Cursor.double(col: String): Double = getDouble(getColumnIndexOrThrow(col))
fun Cursor.doubleOrNull(col: String): Double? = getColumnIndexOrThrow(col).let { if (isNull(it)) null else getDouble(it) }
fun Cursor.float(col: String): Float = getFloat(getColumnIndexOrThrow(col))
fun Cursor.bool(col: String): Boolean = getInt(getColumnIndexOrThrow(col)) == 1

inline fun <T> Cursor.mapRows(transform: (Cursor) -> T): List<T> = use { c ->
    val list = ArrayList<T>(c.count)
    while (c.moveToNext()) list.add(transform(c))
    list
}
