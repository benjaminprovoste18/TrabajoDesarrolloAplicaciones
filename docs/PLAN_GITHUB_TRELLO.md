# Plan de trabajo: GitHub + Trello (IE 2.3.2, 20 % de la nota)

La pauta revisa **actividad real**: commits distribuidos entre ambos integrantes, mensajes técnicos y tareas visibles en Trello. Por eso el proyecto **no** trae historial de Git: lo ideal es que cada integrante suba su parte en varios commits a lo largo de las semanas, no todo de una vez al final.

> Ojo con la regla de la pauta: cualquier cambio en el repositorio **después de la fecha de entrega** se evalúa con 1.0 (grupal e individual).

## 1. Configuración inicial (Benjamin Provoste)

```bash
git init
git add .gitignore app/.gitignore README.md settings.gradle.kts build.gradle.kts gradle.properties gradle/ gradlew gradlew.bat app/build.gradle.kts app/src/main/keepRules
git commit -m "chore: configura proyecto Android con Kotlin, Compose, Navigation y Material 3"
git branch -M main
git remote add origin https://github.com/USUARIO/electromechanics-terreno.git
git push -u origin main
```

Luego en GitHub: `Settings > Collaborators` y agregar al compañero/a y al docente. El repositorio debe ser **público**.

Cada integrante trabaja en una rama por funcionalidad (`feature/...`) y la une a `main` con un Pull Request. Así quedan commits, PRs e issues de ambos.

## 2. Reparto sugerido y commits

Formato de mensajes: `tipo: descripción técnica en presente` (`feat`, `fix`, `refactor`, `test`, `docs`, `style`, `chore`).

### Benjamin Provoste: datos, dominio y recursos nativos

| # | Rama | Archivos | Mensaje de commit |
|---|---|---|---|
| 1 | `feature/dominio` | `model/Enums.kt` | `feat: agrega modelos de dominio (roles, estados de servicio y marcación)` |
| 2 | `feature/dominio` | `domain/validation/Validators.kt` | `feat: implementa validador de RUT con dígito verificador y validadores de formularios` |
| 3 | `feature/dominio` | `domain/rules/BusinessRules.kt`, `domain/util/Utils.kt` | `feat: agrega reglas de negocio de jornada y servicio, cálculo Haversine y hash de contraseñas` |
| 4 | `feature/dominio` | `app/src/test/...` | `test: agrega pruebas unitarias de validadores y reglas de negocio` |
| 5 | `feature/sqlite` | `data/local/entity`, `data/local/relation` | `feat: crea entidades según taxonomía del caso (empleado, sucursal, orden, actividad, jornada)` |
| 6 | `feature/sqlite` | `data/local/dao`, `data/local/AppDatabase.kt` | `feat: crea base SQLite con llaves foráneas y DAOs con consultas reactivas Flow` |
| 7 | `feature/sqlite` | `data/repository`, `data/session`, `di/` | `feat: implementa repositorios, sesión e inyección de dependencias manual` |
| 8 | `feature/sqlite` | `data/seed/DatabaseSeeder.kt` | `feat: agrega datos ficticios anonimizados para pruebas (15 usuarios y 6 sucursales)` |
| 9 | `feature/nativos` | `device/LocationProvider.kt`, `AndroidManifest.xml` | `feat: integra GPS con Fused Location y detección de ubicación simulada` |
| 10 | `feature/nativos` | `device/CameraFileHelper.kt`, `res/xml/file_paths.xml` | `feat: integra cámara con FileProvider para evidencia fotográfica` |
| 11 | `feature/nativos` | `device/NotificationHelper.kt` | `feat: agrega canal y notificaciones de nuevas órdenes` |
| 12 | `feature/nativos` | `device/Vibrador.kt`, `device/Linterna.kt` | `feat: agrega vibración al marcar jornada y linterna para trabajo con poca luz` |
| 13 | `feature/nativos` | `ui/components/NativeLaunchers.kt`, `ui/components/FotoArchivo.kt` | `feat: solicita permisos en tiempo de ejecución y muestra fotos de evidencia` |

### Alonso Pino: interfaz, navegación y pantallas

| # | Rama | Archivos | Mensaje de commit |
|---|---|---|---|
| 1 | `feature/ui-base` | `ui/theme/*`, `res/values`, `res/drawable`, `res/mipmap-anydpi-v26` | `style: define tema Material 3 de alto contraste e ícono de la app` |
| 2 | `feature/ui-base` | `ui/components/FormComponents.kt` | `feat: crea campo validado con ícono, mensaje y contador reutilizable` |
| 3 | `feature/ui-base` | `ui/components/DisplayComponents.kt`, `ui/components/Previews.kt` | `feat: agrega componentes de estado, chips animados, slots de fotos y previews` |
| 4 | `feature/ui-base` | `model/FormModel.kt`, `ui/components/MensajeToast.kt`, `ui/viewmodel/AppViewModelProvider.kt` | `refactor: centraliza FieldState, mensajes Toast y fábrica de ViewModels` |
| 5 | `feature/navegacion` | `ui/navigation/AppNavigation.kt`, `MainActivity.kt`, `ElectroApp.kt` | `feat: configura navegación por perfil con transiciones animadas` |
| 6 | `feature/login` | `model/LoginModel.kt`, `ui/viewmodel/SplashViewModel.kt`, `ui/viewmodel/LoginViewModel.kt`, `ui/screen/SplashScreen.kt`, `ui/screen/LoginScreen.kt` | `feat: implementa splash animado y login con validación y bloqueo por intentos` |
| 7 | `feature/tecnico` | `model/TecnicoModel.kt`, `ui/viewmodel/TecnicoHomeViewModel.kt`, `ui/screen/TecnicoHomeScreen.kt` | `feat: agrega marcación de jornada con GPS y hoja de ruta del técnico` |
| 8 | `feature/tecnico` | `ui/viewmodel/HistorialViewModel.kt`, `ui/screen/HistorialJornadaScreen.kt` | `feat: agrega historial de marcaciones del técnico` |
| 9 | `feature/servicio` | `model/ServicioModel.kt`, `ui/viewmodel/ServicioDetalleViewModel.kt`, `ui/screen/ServicioDetalleScreen.kt` | `feat: implementa detalle de servicio con flujo de estados y evidencia fotográfica` |
| 10 | `feature/servicio` | `ui/viewmodel/ActividadFormViewModel.kt`, `ui/screen/ActividadFormScreen.kt` | `feat: agrega formulario de registro de actividades con validaciones` |
| 11 | `feature/supervisor` | `model/SupervisorModel.kt`, `ui/viewmodel/SupervisorViewModel.kt`, `ui/viewmodel/OrdenFormViewModel.kt`, `ui/screen/SupervisorHomeScreen.kt`, `ui/screen/OrdenFormScreen.kt` | `feat: agrega panel de supervisor con creación de órdenes y auditoría de horas` |
| 12 | `feature/admin` | `model/AdminModel.kt`, `ui/viewmodel/AdminViewModel.kt`, `ui/viewmodel/UsuarioFormViewModel.kt`, `ui/viewmodel/SucursalFormViewModel.kt`, `ui/screen/AdminHomeScreen.kt`, `ui/screen/AdminFormScreens.kt` | `feat: agrega gestión de usuarios y repositorio de sucursales` |

### Ambos (al final)
- `docs: completa README con integrantes, funcionalidades y pasos de ejecución`
- `fix: ...` por cada error que encuentren al probar (son commits valiosos, muestran trabajo real).

El proyecto compila completo cuando están todos los archivos (por ejemplo, `AppViewModelProvider.kt` referencia los ViewModels de todas las pantallas), así que no se asusten si un commit intermedio no compila por sí solo. Si trabajan en paralelo, Alonso puede partir con el tema y los componentes mientras Benjamin arma la capa de datos.

## 3. Tablero Trello

Crear un tablero **"EM Terreno - DSY1105"** con las listas: `Backlog`, `En curso`, `En revisión`, `Terminado`. Asignar cada tarjeta a su responsable y agregar etiqueta por indicador.

| Tarjeta | Responsable | Etiqueta | Checklist |
|---|---|---|---|
| Configurar proyecto y repositorio | Benjamin Provoste | IE 2.3.2 | Crear repo público · Invitar docente · Subir base |
| Modelo de datos SQLite | Benjamin Provoste | IE 2.3.1 | Entidades · DAOs · Relaciones · Seed ficticio |
| Validadores y reglas de negocio | Benjamin Provoste | IE 2.2.1 | RUT · Formularios · Jornada · Servicio · Tests |
| GPS en marcación de jornada | Benjamin Provoste | IE 2.4.1 | Permisos · Lectura actual · Radio 300 m · Mensajes de error |
| Cámara para evidencias | Benjamin Provoste | IE 2.4.1 | Permiso · FileProvider · Foto inicial/final · Foto respaldo |
| Vibración y linterna | Benjamin Provoste | IE 2.4.1 | Vibrar al marcar · Vibrar en error · Linterna en barra del técnico |
| Notificaciones de órdenes | Benjamin Provoste | IE 2.4.1 | Canal · Permiso Android 13 · Aviso urgente |
| Tema y componentes reutilizables | Alonso Pino | IE 2.1.1 | Colores · Tipografía · Botones 48dp+ · Campo validado |
| Navegación y transiciones | Alonso Pino | IE 2.2.2 | Rutas por rol · Animaciones · Volver seguro |
| Login | Alonso Pino | IE 2.1.2 | Validación RUT · Mostrar contraseña · Bloqueo |
| Pantallas del técnico | Alonso Pino | IE 2.1.1 | Jornada · Hoja de ruta · Historial · Detalle |
| Formulario de actividad | Alonso Pino | IE 2.1.2 | Campos · Errores por campo · Guardado |
| Panel supervisor | Alonso Pino | IE 2.1.1 | Órdenes · Filtros · Auditoría · Aprobación |
| Formularios de administración | Alonso Pino | IE 2.1.2 | Usuario · Sucursal · GPS opcional |
| README y APK release | Benjamin Provoste y Alonso Pino | Entrega | README completo · APK firmado · Revisar repo antes del plazo |

Tip: vinculen cada tarjeta con su Pull Request o issue de GitHub (Power-Up de GitHub en Trello) para que la trazabilidad entre planificación y código sea visible.
