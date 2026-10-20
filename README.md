# EM Terreno: Registro de Jornada y Seguimiento de Mantenciones

Aplicación móvil nativa Android para **ELECTROMECHANICS INGENIERÍA SPA** (caso DSY1105, CITT Sede Maipú). Centraliza el control de asistencia con GPS, la hoja de ruta de cada técnico y la bitácora de mantenciones con evidencia fotográfica, reemplazando los mensajes de WhatsApp, correos y planillas en papel.

> Evaluación Parcial 2 (avance intermedio del MVP). Todos los datos son **ficticios**, en cumplimiento de la Ley N.º 19.628 y la Ley N.º 21.719.

## Integrantes

| Nombre | Rol en el equipo |
|---|---|
| Benjamin Provoste | Datos, reglas de negocio y recursos nativos |
| Alonso Pino | Interfaz, navegación y pantallas |

## Problema que resuelve

Hoy la asignación de trabajos es verbal o por correo, la entrada y salida se marca por WhatsApp o planillas firmadas, y los avances quedan repartidos en apuntes y chats. Esto genera incertidumbre en la asistencia, pérdida de información técnica, sobrecarga del supervisor y falta de trazabilidad ante reclamos o auditorías.

## Funcionalidades implementadas

### Perfil Técnico (app móvil)
- **Login con RUT y contraseña** con validación del dígito verificador, bloqueo de 30 s tras 5 intentos fallidos y contraseña guardada como hash (nunca en texto plano).
- **Control de jornada con GPS**: marcación de entrada y salida capturando hora y coordenadas en el momento exacto. Calcula la sucursal más cercana, la distancia y si está dentro del radio de 300 m. Detecta ubicaciones simuladas y permite agregar una foto de respaldo opcional.
- **Hoja de ruta** agrupada por día (Hoy, Mañana…) con estado y prioridad de cada servicio.
- **Detalle del servicio** con flujo de estados `Pendiente → En Proceso → Finalizado` y lista de requisitos que se marca sola:
  - Para iniciar: jornada activa + foto del estado inicial.
  - Para finalizar: al menos una actividad registrada + foto del resultado final.
- **Registro de actividades** (formulario estructurado): detalle de la tarea, estado del equipo, repuestos y comentarios.
- **Evidencia fotográfica** con la cámara del dispositivo (estado inicial y final).
- **Historial de jornada** con coordenadas, distancia y estado de aprobación.
- **Abrir la sucursal en la app de mapas**.
- **Vibración** que confirma la marcación (útil con guantes y protección auditiva) y avisa errores.
- **Linterna** en la barra superior para revisar tableros o salas con poca luz.

### Perfil Supervisor
- **Crear y despachar órdenes de trabajo**: sucursal, técnico (según especialidad), fecha, prioridad, descripción y actividades mínimas.
- **Notificación** al técnico cuando se le asigna una orden (y aviso especial si es urgente).
- **Seguimiento** con contadores por estado y filtros.
- **Auditoría**: horas trabajadas hoy por técnico (suma de pares entrada/salida), marcaciones fuera de radio y aprobación de marcaciones y servicios finalizados.

### Perfil Administrador
- **Gestión de usuarios**: crear cuentas (RUT y correo únicos, rol, especialidad, contraseña con medidor de seguridad) y activar/desactivar cuentas.
- **Repositorio de sucursales**: crear sucursales con coordenadas validadas dentro de la Región Metropolitana, ingresadas a mano o con el GPS del dispositivo.

## Cumplimiento de la pauta (EP2)

| Indicador | Dónde se evidencia |
|---|---|
| **IE 2.1.1** Interfaz y navegación | Material Design 3, jerarquía visual por tarjetas, botones de 56 a 60 dp (sobre los 48 dp exigidos por el caso para uso con guantes), navegación por perfil en `ui/navigation/AppNavHost.kt`. |
| **IE 2.1.2** Formularios validados | 5 formularios (`Login`, `Actividad`, `Orden de trabajo`, `Usuario`, `Sucursal`). Cada campo muestra borde rojo, ícono de error, mensaje y contador; check verde cuando es válido (`ui/components/FormComponents.kt`). |
| **IE 2.2.1** Lógica desacoplada | Toda validación está en `domain/validation` y `domain/rules` (Kotlin puro, sin Android). Los ViewModels (`ui/viewmodel`) exponen `uiState` (modelos de `model/`) y la UI (`ui/screen`) solo lo pinta. Pruebas unitarias en `app/src/test`. |
| **IE 2.2.2** Animaciones | Splash animado, transiciones entre pantallas, sacudida del formulario al enviar con errores, mensajes de error que aparecen/desaparecen, chips de estado con color animado, ícono GPS pulsante, contadores animados, barra de seguridad de contraseña, entrada/salida de elementos de listas (`animateItem`). |
| **IE 2.3.1** Modularidad y persistencia | Arquitectura MVVM + repositorios + SQLite (5 tablas relacionadas con llaves foráneas, según la taxonomía del caso). Inyección de dependencias manual en `di/AppContainer.kt`. |
| **IE 2.3.2** GitHub + Trello | Ver `docs/PLAN_GITHUB_TRELLO.md`. |
| **IE 2.4.1** Recursos nativos | **GPS** (Fused Location), **Cámara** (FileProvider), **Notificaciones**, **Vibración** y **Linterna**, con permisos pedidos en tiempo de ejecución y mensajes si se niegan. |

## Arquitectura

Sigue la misma organización vista en clases (`model`, `ui/screen`, `ui/viewmodel`, `ui/components`, `ui/navigation`, `ui/theme`) y suma capas para datos, reglas de negocio y recursos nativos:

```
app/src/main/java/cl/duoc/electromechanics/
├── model/            # Enums del negocio y estados de cada pantalla (LoginModel, TecnicoModel...)
├── ui/
│   ├── screen/       # Pantallas Compose (Login, TecnicoHome, ServicioDetalle, OrdenForm...)
│   ├── viewmodel/    # Un ViewModel por pantalla + AppViewModelProvider (fábrica)
│   ├── components/   # Componentes reutilizables, lanzadores de permisos y previews
│   ├── navigation/   # Routes y AppNavigation con transiciones animadas
│   └── theme/        # Color.kt, Type.kt y Theme.kt (Material 3)
├── data/
│   ├── local/        # SQLite: AppDatabase (tablas), entidades, DAOs y relaciones
│   ├── repository/   # Repositorios (única puerta de acceso a datos)
│   ├── seed/         # Datos ficticios de prueba (15 usuarios, 6 sucursales)
│   └── session/      # Sesión del usuario autenticado
├── domain/
│   ├── rules/        # Reglas de negocio puras (jornada, servicio, login)
│   ├── util/         # Fechas, distancia GPS (Haversine), hash de contraseñas
│   └── validation/   # Validadores de formularios (RUT, correo, fechas…)
├── device/           # Recursos nativos: GPS, cámara, notificaciones, vibración, linterna
└── di/               # AppContainer (inyección de dependencias)
```

Flujo MVVM, con el mismo patrón visto en clases: cada ViewModel expone `var uiState by mutableStateOf(Modelo())` con `private set`, la pantalla lee `viewModel.uiState` y llama funciones como `onRutChanged(...)` u `onGuardarClick(onSuccess = ...)`. Los mensajes al usuario se muestran con `Toast`.

`Screen (Compose)` → `ViewModel` (valida con `domain`) → `Repository` → `DAO` → `SQLite`. Cada escritura en la base avisa a las consultas observadas, por lo que las listas se actualizan sin recargar.

## Stack

Mismas versiones que los proyectos del curso: **AGP 9.3.3, Gradle 9.5.0, Kotlin 2.2.10, Compose BOM 2026.02.01, compileSdk 37, JDK 21**.

Kotlin · Jetpack Compose · Material Design 3 · MVVM · SQLite (`SQLiteOpenHelper`) · Navigation Compose · Coroutines/Flow · Google Play Services Location · JUnit.

## Cómo ejecutar

1. Clonar el repositorio y abrirlo en **Android Studio** (`File > Open` y elegir la carpeta del proyecto).
2. Esperar la sincronización de Gradle (descarga las dependencias la primera vez).
3. Ejecutar en un emulador con **Google Play** (API 26 o superior) o en un teléfono físico con depuración USB. La linterna y la vibración solo se notan en un teléfono físico.
4. Ingresar con un usuario de prueba (también aparecen en la pantalla de login, en "Usuarios de prueba"):

| Perfil | RUT | Contraseña |
|---|---|---|
| Técnico | 33.333.333-3 | Demo2026 |
| Supervisor | 22.222.222-2 | Demo2026 |
| Administrador | 11.111.111-1 | Demo2026 |

### Probar el GPS en el emulador
El emulador parte en California, por lo que la marcación saldrá "Fuera de radio". Para simular que el técnico está en la sucursal:
`Extended controls (⋯) > Location`, ingresar por ejemplo **-33.5110, -70.7580** (Cliente Demo A, Maipú) y presionar **Set location**.

### Flujo de demostración sugerido
1. **Supervisor** crea una orden para "Operario Alfa Demo" → llega la notificación.
2. **Técnico** marca entrada (GPS) → abre la orden → toma la foto inicial → inicia el servicio → registra una actividad → toma la foto final → finaliza.
3. **Supervisor** revisa la pestaña Auditoría, aprueba la marcación y aprueba el servicio.

### Pruebas unitarias
`app/src/test/.../ValidatorsTest.kt` cubre RUT, correo, contraseña, coordenadas, fechas y reglas de jornada/servicio. Se ejecutan con clic derecho sobre la carpeta `test` > **Run Tests**, o con `./gradlew test`.

### Generar el APK firmado (release)
`Build > Generate Signed App Bundle / APK > APK`, crear o elegir un keystore y seleccionar la variante **release**. El keystore **no se sube** al repositorio (está en `.gitignore`).

## Próximos pasos (EFT)
- Consumir los microservicios Spring Boot con **Retrofit** y sincronizar con la base local (modo offline).
- Notificaciones push reales (FCM) en lugar de notificaciones locales.
- Exportar reportes de servicios para el cliente.
