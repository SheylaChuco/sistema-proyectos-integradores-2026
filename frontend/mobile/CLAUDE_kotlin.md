# CLAUDE.md — frontend/mobile/ (Kotlin + Android)

## INSTRUCCIÓN CRÍTICA
Lee primero el CLAUDE.md raíz del repositorio. Este archivo agrega las reglas específicas de Kotlin que aplican SOLO a este módulo. Ante cualquier contradicción, el CLAUDE.md raíz tiene prioridad.

---

## 1. Responsabilidad de este módulo

La app móvil es SOLO para el rol ESTUDIANTE. No existe panel de administración en la app móvil.

Funcionalidades que cubre:
- HU-01: Registro de estudiante
- HU-02a: Login de estudiante
- HU-04: Catálogo de proyectos (con reglas de origen)
- HU-05: Detalle de proyecto
- HU-06: Registro de grupo
- HU-07: Registro y seguimiento de propuesta
- HU-09: Corrección de propuesta observada
- HU-10: Chat con Asistente IA

Toda la comunicación es con Spring Boot (puerto 8080). NUNCA llames a Django desde la app móvil.

---

## 2. Stack y versiones

- Kotlin 1.9+
- Android API mínimo: 26 (Android 8.0)
- Android API target: 34 (Android 14)
- Jetpack Compose (UI — NUNCA XML layouts)
- ViewModel + StateFlow (estado)
- Room (base de datos local — SOLO para caché ligero si aplica)
- Retrofit 2+ (llamadas HTTP)
- OkHttp (cliente HTTP + interceptores)
- Hilt (inyección de dependencias)
- Kotlin Coroutines + Flow (asincronía)
- Gson o Moshi (serialización JSON)
- Navigation Compose (routing entre pantallas)

---

## 3. Arquitectura MVVM — estructura obligatoria

```
frontend/mobile/
├── app/src/main/java/pe/tecsup/proyectos/
│   ├── MainActivity.kt
│   ├── di/                           ← módulos de Hilt
│   │   ├── NetworkModule.kt
│   │   └── RepositoryModule.kt
│   ├── data/
│   │   ├── remote/
│   │   │   ├── api/
│   │   │   │   └── ApiService.kt     ← interfaz Retrofit con todos los endpoints
│   │   │   ├── dto/                  ← clases de datos para JSON
│   │   │   │   ├── ProyectoDTO.kt
│   │   │   │   ├── PropuestaDTO.kt
│   │   │   │   └── ApiResponse.kt
│   │   │   └── interceptor/
│   │   │       └── AuthInterceptor.kt ← agrega JWT a cada request
│   │   └── repository/               ← implementación de repositorios
│   │       ├── AuthRepository.kt
│   │       ├── CatalogoRepository.kt
│   │       ├── GrupoRepository.kt
│   │       ├── PropuestaRepository.kt
│   │       └── AsistenteRepository.kt
│   ├── domain/
│   │   ├── model/                    ← modelos del dominio (distintos a DTOs)
│   │   │   ├── Proyecto.kt
│   │   │   └── Propuesta.kt
│   │   └── repository/               ← interfaces de repositorio
│   │       ├── IAuthRepository.kt
│   │       └── ICatalogoRepository.kt
│   └── ui/
│       ├── auth/                     ← HU-01, HU-02a
│       │   ├── LoginScreen.kt
│       │   ├── RegistroScreen.kt
│       │   └── AuthViewModel.kt
│       ├── catalogo/                 ← HU-04, HU-05
│       │   ├── CatalogoScreen.kt
│       │   ├── DetalleProyectoScreen.kt
│       │   └── CatalogoViewModel.kt
│       ├── grupos/                   ← HU-06
│       │   ├── RegistroGrupoScreen.kt
│       │   └── GrupoViewModel.kt
│       ├── propuestas/               ← HU-07, HU-09
│       │   ├── PropuestaScreen.kt
│       │   └── PropuestaViewModel.kt
│       ├── asistente/                ← HU-10
│       │   ├── AsistenteScreen.kt
│       │   └── AsistenteViewModel.kt
│       ├── navigation/
│       │   └── AppNavigation.kt      ← Navigation Compose
│       └── shared/
│           ├── components/           ← composables reutilizables
│           │   ├── BadgeEstado.kt
│           │   ├── LoadingIndicator.kt
│           │   └── ErrorMessage.kt
│           └── theme/
│               └── Theme.kt          ← colores y tipografía de la app
├── app/src/main/res/
│   └── values/
│       └── strings.xml               ← textos en español
├── app/build.gradle.kts
├── local.properties                  ← NO subir al repo (en .gitignore)
└── CLAUDE.md
```

---

## 4. Reglas de arquitectura Kotlin/MVVM — obligatorias

**Regla 1 — Flujo de datos unidireccional:**
```
UI (Screen) → ViewModel → Repository → ApiService
     ↑               ↓
     └── StateFlow (estado) ──────────┘
```
La UI NUNCA llama directamente al Repository ni al ApiService. Siempre a través del ViewModel.

**Regla 2 — StateFlow para estado de UI:**
```kotlin
data class CatalogoUiState(
    val proyectos: List<Proyecto> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val paginaActual: Int = 1
)

class CatalogoViewModel @Inject constructor(
    private val catalogoRepository: ICatalogoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CatalogoUiState())
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    fun cargarProyectos(filtros: FiltrosCatalogo) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            catalogoRepository.getProyectos(filtros)
                .onSuccess { proyectos -> _uiState.update { it.copy(proyectos = proyectos, loading = false) } }
                .onFailure { error -> _uiState.update { it.copy(error = error.message, loading = false) } }
        }
    }
}
```

**Regla 3 — Screens solo observan estado:**
```kotlin
@Composable
fun CatalogoScreen(viewModel: CatalogoViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.loading -> LoadingIndicator()
        uiState.error != null -> ErrorMessage(uiState.error!!)
        else -> ListaProyectos(uiState.proyectos)
    }
}
```

**Regla 4 — URL del emulador — CRÍTICO:**
```kotlin
// build.gradle.kts — NUNCA uses "localhost" en Android
buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080\"")
```
`10.0.2.2` es la dirección que el emulador de Android usa para referirse a `localhost` de tu computadora. Si usas un dispositivo físico, usa la IP de tu computadora en la red WiFi local.

**Regla 5 — JWT en SharedPreferences:**
```kotlin
// data/local/TokenManager.kt
class TokenManager @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun guardarToken(token: String) = prefs.edit().putString("jwt_token", token).apply()
    fun obtenerToken(): String? = prefs.getString("jwt_token", null)
    fun limpiarToken() = prefs.edit().remove("jwt_token").apply()
}
```

**Regla 6 — Interceptor JWT en OkHttp:**
```kotlin
class AuthInterceptor @Inject constructor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        tokenManager.obtenerToken()?.let { token ->
            request.addHeader("Authorization", "Bearer $token")
        }
        return chain.proceed(request.build())
    }
}
```

**Regla 7 — Renderizado condicional por origen — CRÍTICO:**
```kotlin
@Composable
fun TarjetaProyecto(proyecto: Proyecto) {
    Column {
        Text(proyecto.nombre)
        Text(proyecto.ciclo)
        // SOLO mostrar estado si origen == NUEVO
        if (proyecto.origen == OrigenProyecto.NUEVO) {
            BadgeEstado(proyecto.estado)
        }
        // Para HISTORICO: nunca mostrar estado, integrantes ni url
    }
}
```

**Regla 8 — Logout:**
```kotlin
fun logout() {
    tokenManager.limpiarToken()
    // Navegar a LoginScreen — sin llamar a ningún endpoint
}
```

---

## 5. Configuración Retrofit

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}
```

---

## 6. Chat IA — historial en memoria (HU-10)

```kotlin
data class MensajeChat(val rol: String, val contenido: String)

class AsistenteViewModel @Inject constructor(
    private val asistenteRepository: AsistenteRepository
) : ViewModel() {
    // El historial vive en memoria — se pierde al cerrar la app (comportamiento esperado)
    private val historialLocal = mutableListOf<MensajeChat>()

    fun enviarPregunta(pregunta: String) {
        viewModelScope.launch {
            historialLocal.add(MensajeChat("user", pregunta))
            // Enviar solo los últimos 10 mensajes
            val contexto = historialLocal.takeLast(10)
            val respuesta = asistenteRepository.chat(pregunta, contexto)
            historialLocal.add(MensajeChat("assistant", respuesta))
        }
    }
}
```

---

## 7. Convenciones de nombrado Kotlin

- Screens: `NombreScreen.kt` (ej. `CatalogoScreen.kt`)
- ViewModels: `NombreViewModel.kt`
- Repositories: `NombreRepository.kt` (implementación), `INombreRepository.kt` (interfaz)
- DTOs: `NombreDTO.kt`
- Composables reutilizables: `NombreComponent.kt`
- Strings de UI: siempre en `strings.xml`, nunca hardcodeadas en el código

