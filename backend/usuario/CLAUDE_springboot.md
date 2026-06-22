# CLAUDE.md — backend/usuario/ (Spring Boot)

## INSTRUCCIÓN CRÍTICA
Lee primero el CLAUDE.md raíz del repositorio. Este archivo agrega las reglas específicas de Spring Boot que aplican SOLO a este módulo. Ante cualquier contradicción, el CLAUDE.md raíz tiene prioridad.

---

## 1. Responsabilidad de este módulo

Este módulo es el backend del panel del estudiante. Solo atiende al rol ESTUDIANTE, con una excepción: también expone los endpoints de catálogo (HU-04, HU-05) que leen proyectos históricos y nuevos de la BD compartida.

Endpoints que viven aquí:
- POST /api/auth/registro
- POST /api/auth/login
- GET  /api/proyectos
- GET  /api/proyectos/{id}
- GET  /api/estudiantes/buscar
- POST /api/grupos
- POST /api/propuestas
- PUT  /api/propuestas/{id}
- PUT  /api/proyectos/{id}/url
- POST /api/asistente/chat

NO implementes aquí ningún endpoint del administrador. Si dudas, consulta el expediente técnico sección 1.1.

---

## 2. Stack y versiones

- Java 17 LTS
- Spring Boot 3.2+
- Spring Security 6+
- Spring Data JPA + Hibernate
- jjwt 0.12+ (JWT)
- PostgreSQL driver
- Lombok (reducir boilerplate)
- Maven (gestor de dependencias)
- JUnit 5 + Mockito + MockMvc (pruebas)

---

## 3. Arquitectura en capas — estructura obligatoria

```
backend/usuario/
├── src/main/java/pe/tecsup/proyectos/
│   ├── ProyectosApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java       ← Spring Security + JWT filter
│   │   ├── JwtConfig.java            ← configuración JWT
│   │   └── CorsConfig.java           ← configuración CORS
│   ├── auth/                         ← EP-01: registro y login
│   │   ├── controller/
│   │   │   └── AuthController.java
│   │   ├── service/
│   │   │   └── AuthService.java
│   │   ├── dto/
│   │   │   ├── RegistroRequest.java
│   │   │   ├── LoginRequest.java
│   │   │   └── AuthResponse.java
│   │   └── repository/
│   │       └── UsuarioRepository.java
│   ├── catalogo/                     ← EP-02: HU-04, HU-05
│   │   ├── controller/
│   │   ├── service/
│   │   ├── dto/
│   │   └── repository/
│   ├── grupos/                       ← EP-03: HU-06
│   │   ├── controller/
│   │   ├── service/
│   │   ├── dto/
│   │   └── repository/
│   ├── propuestas/                   ← EP-03: HU-07, HU-09
│   │   ├── controller/
│   │   ├── service/
│   │   ├── dto/
│   │   └── repository/
│   ├── asistente/                    ← EP-05: HU-10
│   │   ├── controller/
│   │   ├── service/
│   │   └── dto/
│   └── shared/
│       ├── entity/                   ← entidades JPA (SOLO lectura del schema)
│       │   ├── Usuario.java
│       │   ├── Estudiante.java
│       │   ├── Grupo.java
│       │   ├── GrupoIntegrante.java
│       │   ├── Propuesta.java
│       │   ├── PropuestaVersion.java
│       │   └── Proyecto.java
│       ├── response/
│       │   └── ApiResponse.java      ← formato estándar de respuesta
│       └── exception/
│           └── GlobalExceptionHandler.java
├── src/main/resources/
│   ├── application.properties        ← config base (sin secretos)
│   └── application-dev.properties   ← config desarrollo
└── src/test/java/pe/tecsup/proyectos/
    ├── auth/
    ├── catalogo/
    ├── grupos/
    └── propuestas/
```

---

## 4. Reglas de arquitectura Spring Boot — obligatorias

**Regla 1 — Capas estrictas, sin saltarse:**
```
Controller → Service → Repository → Entity
```
- El Controller SOLO recibe la request, llama al Service y devuelve la respuesta. Sin lógica de negocio.
- El Service contiene TODA la lógica de negocio. Sin acceso directo a la BD.
- El Repository SOLO hace consultas a la BD. Sin lógica.
- Nunca inyectes un Repository directamente en un Controller.

**Regla 2 — DTOs obligatorios, nunca entidades en respuestas:**
```java
// CORRECTO
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<ProyectoDTO>> getProyecto(@PathVariable Long id) {
    ProyectoDTO dto = proyectoService.findById(id);
    return ResponseEntity.ok(ApiResponse.success(dto));
}

// INCORRECTO — nunca devuelvas la entidad directamente
public ResponseEntity<Proyecto> getProyecto(@PathVariable Long id) { ... }
```

**Regla 3 — Spring Boot en modo validate SIEMPRE:**
```properties
# application.properties
spring.jpa.hibernate.ddl-auto=validate
```
Nunca uses `create`, `update` o `create-drop`. Django es dueño del schema.

**Regla 4 — ApiResponse para todas las respuestas:**
```java
// shared/response/ApiResponse.java
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ErrorDetail error;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorDetail(code, message));
    }
}
```

**Regla 5 — GlobalExceptionHandler obligatorio:**
Captura TODAS las excepciones y las convierte al formato estándar. Nunca dejes que Spring devuelva su formato de error por defecto.

**Regla 6 — BCrypt para contraseñas:**
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```
Nunca almacenes contraseñas en texto plano. Nunca uses MD5 ni SHA.

**Regla 7 — Lógica de origen en catálogo:**
Al construir el DTO de Proyecto, verifica `origen` antes de incluir campos:
```java
ProyectoDTO dto = new ProyectoDTO();
dto.setNombre(proyecto.getNombre());
dto.setOrigen(proyecto.getOrigen());
if (proyecto.getOrigen() == Origen.NUEVO) {
    dto.setEstado(proyecto.getEstado());
    dto.setUrl(proyecto.getUrl());
    dto.setIntegrantes(obtenerIntegrantes(proyecto.getGrupo()));
}
// Para HISTORICO: estado, url e integrantes quedan null/ausentes del DTO
```

---

## 5. Seguridad JWT — configuración obligatoria

```java
// SecurityConfig.java
http
  .csrf(csrf -> csrf.disable())
  .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS))
  .authorizeHttpRequests(auth -> auth
      .requestMatchers("/api/auth/**").permitAll()
      .requestMatchers("/api/**").hasRole("ESTUDIANTE")
      .anyRequest().authenticated()
  )
  .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
```

El token JWT debe incluir:
- `sub`: id del usuario
- `rol`: ESTUDIANTE
- `exp`: timestamp de expiración (8 horas)

---

## 6. Lógica automática al aprobar propuesta

Cuando Django cambia el estado de una propuesta a APROBADO, Spring Boot debe:
1. Detectarlo al procesar cualquier request que involucre esa propuesta
2. Verificar si ya existe un Proyecto asociado a ese grupo
3. Si no existe: crear `Proyecto(origen=NUEVO, estado=EN_DESARROLLO, grupoId=propuesta.grupoId, nombre=propuesta.nombre, descripcion=propuesta.descripcion, ciclo=periodoActual)`

Alternativamente, exponer un endpoint interno que Django llame al aprobar:
```
POST /api/interno/proyectos/crear-desde-propuesta/{propuestaId}
```
Este endpoint es interno — protégelo con un secret compartido, no con JWT de usuario.

---

## 7. Asistente IA (HU-10) — reglas del LLM

```java
// El system prompt DEBE incluir exactamente este texto:
String systemPrompt = """
    Eres un asistente que ayuda a estudiantes de TECSUP a encontrar
    información sobre proyectos integradores.

    REGLA CRÍTICA: Los proyectos de origen HISTORICO solo tienen
    nombre, descripción, ciclo y código de sección. NO tienen estado,
    integrantes ni URL. NUNCA inventes ni asumas esos datos para
    proyectos históricos. Si te preguntan por ellos, indica que
    esa información no está disponible para proyectos históricos.

    Proyectos disponibles en la base de datos:
    {contexto_proyectos}
    """;
```

Ventana de contexto: incluye en cada llamada los últimos 10 mensajes del historial enviado por el frontend. No almacenes historial en BD — es responsabilidad del frontend mantenerlo en estado local.

---

## 8. Pruebas — obligatorias

### Pruebas unitarias (Service layer) con Mockito:
```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private AuthService authService;

    @Test
    void registro_correoYaExiste_lanzaException() {
        when(usuarioRepository.existsByCorreo(anyString())).thenReturn(true);
        assertThrows(CorreoDuplicadoException.class,
            () -> authService.registrar(registroRequest));
    }
}
```

### Pruebas de integración (Controller layer) con MockMvc:
```java
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
    @Autowired MockMvc mockMvc;

    @Test
    void login_credencialesCorrectas_retornaToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
            .contentType(APPLICATION_JSON)
            .content("{\"correo\":\"test@tecsup.edu.pe\",\"password\":\"pass\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.token").exists());
    }
}
```

Cobertura mínima obligatoria: 70% en capa Service.

---

## 9. CORS en Spring Boot

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(System.getenv("ALLOWED_ORIGINS")));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

