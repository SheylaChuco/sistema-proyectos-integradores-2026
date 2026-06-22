# Expediente Técnico — Sistema de Gestión de Proyectos Integradores
**TECSUP — Carrera de Desarrollo de Software**
*Documento base para desarrollo con agentes de Claude Code*

---

## Tabla de contenido
1. [Contexto del Proyecto](#1-contexto-del-proyecto)
2. [Épicas del Proyecto](#2-épicas-del-proyecto)
3. [Modelo de Datos](#3-modelo-de-datos)
4. [Especificaciones Técnicas Transversales](#4-especificaciones-técnicas-transversales)
5. [Sprints e Historias de Usuario](#5-sprints-e-historias-de-usuario)
6. [Alcance y Exclusiones](#6-alcance-y-exclusiones)

---

## 1. Contexto del Proyecto

El sistema reemplaza el Excel actual de proyectos integradores de TECSUP. Permite a estudiantes explorar proyectos históricos, registrar su grupo, proponer su proyecto y hacer seguimiento del proceso de aprobación. Los administradores (Encargados de Proyectos) revisan y aprueban propuestas, evalúan sustentaciones y consultan estadísticas. Un Asistente IA complementa la experiencia del estudiante con consultas en lenguaje natural.

El sistema maneja dos tipos de proyecto bajo un mismo modelo de datos, diferenciados por su origen:

- **Proyectos Históricos:** migrados desde el Excel acumulado de ciclos anteriores (2020-II a 2026-I). Campos disponibles: nombre, descripción, ciclo, código de sección. Sin estado, sin integrantes, sin URL.
- **Proyectos Nuevos:** registrados en el sistema a partir del ciclo actual. Campos completos: nombre, descripción, ciclo, grupo, integrantes, estado, URL, comentario de evaluación.

### 1.1 Arquitectura del Sistema

| Módulo | Stack | Responsabilidad |
|--------|-------|-----------------|
| USUARIO | React (web) + Kotlin (móvil) + Spring Boot (backend) | Autenticación de estudiante, catálogo, grupos, propuestas, Asistente IA |
| ADMINISTRACIÓN | React (web) + Django (backend) | Autenticación de admin, revisión de propuestas, evaluación de sustentaciones, dashboard de estadísticas |
| BASE DE DATOS | PostgreSQL (Docker) | BD compartida entre Spring Boot y Django. Django es dueño de las migraciones. Spring Boot opera en modo validate. |

> **PROPÓSITO DEL DOCUMENTO:** Este documento es el único expediente técnico del proyecto. Cada decisión de diseño, regla de negocio, campo de datos y comportamiento esperado está definido aquí. Un agente de Claude Code debe poder construir el sistema completo usando solo este documento, sin necesidad de inferir o asumir nada no especificado.

---

## 2. Épicas del Proyecto

| ID | Épica | Descripción |
|----|-------|-------------|
| EP-01 | Gestión de Autenticación y Usuarios | Registro de estudiantes, inicio de sesión de estudiante y administrador con JWT, y logout. |
| EP-02 | Exploración y Dashboard de Proyectos | Catálogo unificado de proyectos históricos y nuevos con filtros, búsqueda y detalle. Campos visibles según origen del proyecto. |
| EP-03 | Registro y Gestión de Propuestas | Flujo completo: registro de grupo, propuesta de proyecto, corrección de observaciones y seguimiento de estado. |
| EP-04 | Panel de Administración | Revisión y aprobación/observación de propuestas, evaluación de sustentación final, dashboard de estadísticas. |
| EP-05 | Asistente IA | Chat en lenguaje natural sobre proyectos históricos y nuevos, con distinción explícita de campos según origen. Backend: Spring Boot. |
| EP-06 | Preparación Técnica de Datos Históricos | Tareas técnicas de limpieza, normalización y carga del Excel histórico a la BD. No contiene historias de usuario. |

---

## 3. Modelo de Datos

Base de datos única compartida entre Spring Boot y Django. Django es dueño del schema y ejecuta todas las migraciones. Spring Boot opera con `spring.jpa.hibernate.ddl-auto=validate`.

### 3.1 Convenciones

- Todos los `id` son autoincrementales (int). No se usa UUID.
- `created_at` y `updated_at` están presentes en todas las tablas (auditoría estándar), no se listan en cada entidad por brevedad.
- Contraseñas almacenadas con hash BCrypt, nunca en texto plano.
- Motor: PostgreSQL 15+, levantado con Docker Compose.

### 3.2 Entidades

#### Usuario
*Tabla base de autenticación. Compartida entre ambos backends.*

| Campo | Tipo | Restricciones / Notas |
|-------|------|-----------------------|
| id | int | PK, autoincremental |
| nombre | varchar(255) | NOT NULL |
| correo | varchar(255) | NOT NULL, UNIQUE, dominio institucional validado |
| password | varchar(255) | NOT NULL, hash BCrypt |
| rol | enum('ESTUDIANTE','ADMIN') | NOT NULL |
| activo | boolean | NOT NULL, default true |

#### Estudiante
*Extiende Usuario para el rol ESTUDIANTE.*

| Campo | Tipo | Restricciones / Notas |
|-------|------|-----------------------|
| id | int | PK, autoincremental |
| usuario_id | int | FK → Usuario.id, UNIQUE (1 a 1) |
| codigo_estudiante | varchar(20) | NOT NULL, UNIQUE |

#### Administrador
*Extiende Usuario para el rol ADMIN. Credencial única creada por seed (createsuperuser de Django), sin autoregistro.*

| Campo | Tipo | Restricciones / Notas |
|-------|------|-----------------------|
| id | int | PK, autoincremental |
| usuario_id | int | FK → Usuario.id, UNIQUE (1 a 1) |

#### Grupo

| Campo | Tipo | Restricciones / Notas |
|-------|------|-----------------------|
| id | int | PK, autoincremental |
| codigo_grupo | varchar(20) | NOT NULL, UNIQUE |
| periodo | varchar(10) | NOT NULL, ej. "2026-I" |

#### GrupoIntegrante
*Tabla intermedia N a N entre Grupo y Estudiante.*

| Campo | Tipo | Restricciones / Notas |
|-------|------|-----------------------|
| id | int | PK, autoincremental |
| grupo_id | int | FK → Grupo.id, NOT NULL |
| estudiante_id | int | FK → Estudiante.id, NOT NULL |

> **REGLA DE NEGOCIO:** Un estudiante no puede pertenecer a dos grupos en el mismo periodo. Validar al crear GrupoIntegrante: si existe otro registro con mismo `estudiante_id` y mismo `periodo` (via Grupo.periodo), rechazar con error `ESTUDIANTE_YA_TIENE_GRUPO`.

#### Propuesta

| Campo | Tipo | Restricciones / Notas |
|-------|------|-----------------------|
| id | int | PK, autoincremental |
| grupo_id | int | FK → Grupo.id, NOT NULL, UNIQUE (1 propuesta activa por grupo) |
| nombre | varchar(255) | NOT NULL |
| descripcion | text | NOT NULL |
| estado | enum('PENDIENTE','APROBADO','OBSERVADO') | NOT NULL, default PENDIENTE |
| comentario_observacion | text | nullable, solo cuando estado=OBSERVADO |
| fecha_envio | datetime | NOT NULL |

> **REGLA DE NEGOCIO:** El campo `url` NO existe en Propuesta. La URL del repositorio se captura en Proyecto, una vez que la propuesta es aprobada y el proyecto creado.

#### PropuestaVersion
*Historial de versiones de una propuesta. Se crea un snapshot cada vez que un estudiante edita su propuesta observada.*

| Campo | Tipo | Restricciones / Notas |
|-------|------|-----------------------|
| id | int | PK, autoincremental |
| propuesta_id | int | FK → Propuesta.id, NOT NULL |
| nombre | varchar(255) | snapshot del nombre antes de editar |
| descripcion | text | snapshot de la descripción antes de editar |
| version_fecha | datetime | NOT NULL, timestamp de la edición |

#### Proyecto
*Tabla unificada para proyectos históricos y nuevos. La columna `origen` determina qué campos aplican.*

| Campo | Tipo | Restricciones / Notas |
|-------|------|-----------------------|
| id | int | PK, autoincremental |
| origen | enum('HISTORICO','NUEVO') | NOT NULL |
| nombre | varchar(255) | NOT NULL |
| descripcion | text | NOT NULL |
| ciclo | varchar(10) | NOT NULL, ej. "2025-II" |
| codigo_seccion | varchar(20) | nullable, solo si origen=HISTORICO, ej. "2025-II-1A" |
| grupo_id | int | FK → Grupo.id, nullable, solo si origen=NUEVO |
| estado | enum('EN_DESARROLLO','APROBADO','NO_APROBADO') | nullable, solo si origen=NUEVO |
| comentario_evaluacion | text | nullable, opcional en ambos resultados de sustentación |
| url | varchar(500) | nullable, solo si origen=NUEVO, se registra tras aprobación |

> **REGLA DE NEGOCIO:** Los campos `grupo_id`, `estado`, `comentario_evaluacion` y `url` son siempre NULL para proyectos de `origen=HISTORICO`. Ninguna vista ni endpoint debe mostrar ni solicitar estos campos para proyectos históricos.

### 3.3 Relaciones

| Relación | Tipo | Regla |
|----------|------|-------|
| Usuario → Estudiante | 1 a 1 | Solo si rol=ESTUDIANTE |
| Usuario → Administrador | 1 a 1 | Solo si rol=ADMIN |
| Grupo ↔ Estudiante | N a N | Vía GrupoIntegrante. Un estudiante, un grupo por periodo. |
| Grupo → Propuesta | 1 a 1 | Una propuesta activa por grupo por ciclo. |
| Propuesta → PropuestaVersion | 1 a N | Snapshot antes de cada edición por observación. |
| Propuesta → Proyecto | 1 a 1 | Al aprobarse la propuesta se crea el Proyecto automáticamente. |
| Grupo → Proyecto | 1 a 1 | Un grupo desarrolla como máximo un proyecto nuevo por ciclo. |

### 3.4 Ciclo de vida: Propuesta → Proyecto

1. Propuesta creada → estado `PENDIENTE`.
2. Admin observa → estado `OBSERVADO` + `comentario_observacion`. Estudiante corrige → snapshot en `PropuestaVersion` → estado vuelve a `PENDIENTE`.
3. Admin aprueba → estado `APROBADO` → sistema crea automáticamente `Proyecto` (`origen=NUEVO`, `estado=EN_DESARROLLO`, `url=NULL`, `grupo_id` del grupo de la propuesta).
4. Sistema notifica al estudiante y ofrece (opcionalmente) registrar la URL del repositorio. Puede completarla después desde su panel mientras el proyecto esté `EN_DESARROLLO`.
5. Sustentación final → admin evalúa → Proyecto pasa a `APROBADO` o `NO_APROBADO` + `comentario_evaluacion` opcional. Decisión única, sin reintentos.
6. Grupo con proyecto `NO_APROBADO`: el ciclo cierra para ese grupo. No pueden registrar nuevas propuestas en ese periodo.

### 3.5 Caso de ejemplo — recorrido completo

**Paso 1 — Registro de estudiantes (HU-01)**
```
Usuario(id=10, nombre="Lucía Ramos", correo="lucia.ramos@tecsup.edu.pe", rol=ESTUDIANTE)
Estudiante(id=5, usuario_id=10, codigo_estudiante="U21205678")
Usuario(id=11, nombre="Mateo Vidal", correo="mateo.vidal@tecsup.edu.pe", rol=ESTUDIANTE)
Estudiante(id=6, usuario_id=11, codigo_estudiante="U21209912")
```

**Paso 2 — Registro del grupo (HU-06)**
```
Grupo(id=3, codigo_grupo="7B", periodo="2026-I")
GrupoIntegrante(id=1, grupo_id=3, estudiante_id=5)  -- Lucía
GrupoIntegrante(id=2, grupo_id=3, estudiante_id=6)  -- Mateo
```

**Paso 3 — Registro de propuesta (HU-07)**
```
Propuesta(id=8, grupo_id=3, nombre="EcoRuta",
          descripcion="App de rutas de reciclaje...",
          estado=PENDIENTE, fecha_envio="2026-06-20")
```

**Paso 4 — Administrador observa (HU-08)**
```
Propuesta(id=8, estado=OBSERVADO,
          comentario_observacion="Definir mejor el alcance del MVP")
```

**Paso 5 — Estudiante corrige y reenvía (HU-09)**
```
PropuestaVersion(id=1, propuesta_id=8, nombre="EcoRuta",
                 descripcion="[descripción original]", version_fecha="2026-06-21")
Propuesta(id=8, descripcion="[descripción corregida]", estado=PENDIENTE)
```

**Paso 6 — Administrador aprueba (HU-08)**
```
Propuesta(id=8, estado=APROBADO)
```

**Paso 7 — Creación automática del Proyecto**
```
Proyecto(id=201, origen=NUEVO, nombre="EcoRuta", ciclo="2026-I",
         grupo_id=3, estado=EN_DESARROLLO, url=NULL, comentario_evaluacion=NULL)
```

**Paso 8 — Sustentación final**
```
Proyecto(id=201, estado=APROBADO,
         comentario_evaluacion="Buen MVP, falta documentación técnica")
```

---

## 4. Especificaciones Técnicas Transversales

> **INSTRUCCIÓN:** El agente de Claude Code debe leer esta sección completa antes de implementar cualquier módulo.

### 4.1 Base de Datos

| Parámetro | Valor |
|-----------|-------|
| Motor | PostgreSQL 15+ |
| Entorno de desarrollo | Docker Compose (contenedor) |
| Puerto | 5432 |
| Nombre de la BD | proyectos_integradores |
| Dueño de migraciones | Django (`python manage.py migrate`) |
| Configuración Spring Boot | `spring.jpa.hibernate.ddl-auto=validate` |

### 4.2 Puertos y URLs base

| Servicio | Puerto | URL base en desarrollo |
|----------|--------|------------------------|
| Spring Boot (backend estudiante) | 8080 | http://localhost:8080 |
| Django (backend administrador) | 8000 | http://localhost:8000 |
| React web (frontend) | 5173 | http://localhost:5173 |
| PostgreSQL (Docker) | 5432 | localhost:5432 |
| Kotlin Android (emulador → Spring Boot) | — | http://10.0.2.2:8080 |

> **REGLA CRÍTICA ANDROID:** En el emulador de Android, `localhost` del emulador NO es `localhost` de tu computadora. Usar siempre `10.0.2.2:8080` para conectar el emulador con Spring Boot. Para dispositivo físico, usar la IP local de la computadora en la red WiFi.

### 4.3 Autenticación — JWT unificado

Ambos backends emiten JWT. React usa exactamente el mismo mecanismo para ambos.

| Parámetro | Spring Boot (estudiante) | Django (administrador) |
|-----------|--------------------------|------------------------|
| Endpoint de login | POST /api/auth/login | POST /api/admin/auth/login |
| Librería JWT | Spring Security + jjwt | djangorestframework-simplejwt |
| Expiración del token | 8 horas (28800000 ms) | 8 horas |
| Claim rol en el token | rol=ESTUDIANTE | rol=ADMIN |
| Header de envío | Authorization: Bearer {token} | Authorization: Bearer {token} |
| Storage en React | localStorage | localStorage |
| Storage en Kotlin | SharedPreferences | No aplica |

- **Logout:** el frontend borra el token del localStorage/SharedPreferences y redirige al login. Sin blacklist en backend.
- **Recuperación de contraseña:** fuera del alcance. Reset manual por administrador desde Django admin.

### 4.4 Reglas de enrutamiento React → Backends

| Prefijo del endpoint | Backend destino | Puerto |
|---------------------|-----------------|--------|
| /api/auth/** | Spring Boot | 8080 |
| /api/** (cualquier otro) | Spring Boot | 8080 |
| /api/admin/** | Django | 8000 |

React tiene dos variables de entorno: `VITE_API_ESTUDIANTE_URL` (Spring Boot) y `VITE_API_ADMIN_URL` (Django). Cada llamada axios/fetch debe usar la variable correcta según el prefijo del endpoint.

### 4.5 CORS

| Backend | Origen permitido (desarrollo) |
|---------|-------------------------------|
| Spring Boot | http://localhost:5173 |
| Django | http://localhost:5173 |

CORS debe configurarse en ambos backends desde el Sprint 1, antes de que React intente consumirlos.

### 4.6 Seguridad de Endpoints por Rol

| Ruta | Acceso permitido | Respuesta si no autorizado |
|------|------------------|---------------------------|
| /api/auth/** (Spring Boot) | Público (sin token) | — |
| /api/** (Spring Boot) | Solo token con rol=ESTUDIANTE | 403 FORBIDDEN |
| /api/admin/auth/** (Django) | Público (sin token) | — |
| /api/admin/** (Django) | Solo token con rol=ADMIN | 403 FORBIDDEN |

> **REGLA CRÍTICA:** Ningún endpoint de `/api/admin/**` puede ser accedido con un token de estudiante, y viceversa. Implementar verificación de rol en Spring Security (Spring Boot) y en `permission_classes` (Django) desde el inicio.

### 4.7 Formato estándar de Respuestas HTTP

Ambos backends deben usar exactamente el mismo formato. React espera este contrato en todos los endpoints.

**Respuesta exitosa:**
```json
{
  "success": true,
  "data": { }
}
```

**Respuesta de error:**
```json
{
  "success": false,
  "error": {
    "code": "CODIGO_ERROR",
    "message": "Mensaje legible para el usuario."
  }
}
```

**Tabla de códigos de error definidos:**

| Código | HTTP | Cuándo ocurre |
|--------|------|---------------|
| CORREO_DUPLICADO | 409 | HU-01: el correo ya está registrado |
| CORREO_INVALIDO | 400 | HU-01: el correo no tiene dominio institucional |
| CREDENCIALES_INCORRECTAS | 401 | HU-02a/b: login con credenciales incorrectas |
| NO_AUTORIZADO | 401 | Endpoint protegido sin token válido |
| FORBIDDEN | 403 | Token válido pero rol incorrecto para ese endpoint |
| SIN_GRUPO | 403 | HU-07: estudiante sin grupo intenta proponer |
| ESTUDIANTE_YA_TIENE_GRUPO | 409 | HU-06: integrante ya pertenece a otro grupo en ese periodo |
| PROPUESTA_ACTIVA | 409 | HU-07: el grupo ya tiene una propuesta pendiente o aprobada |
| ESTADO_INVALIDO | 422 | HU-09: intentar editar propuesta que no está en estado OBSERVADO |
| CICLO_CERRADO | 403 | Grupo con NO_APROBADO intenta registrar nueva propuesta en el mismo periodo |
| NO_ENCONTRADO | 404 | Cualquier /{id} que no existe en la BD |

### 4.8 Variables de Entorno

> **REGLA ABSOLUTA:** ninguna variable de entorno va en el código fuente ni en el repositorio. Siempre en archivos `.env` incluidos en `.gitignore`. Usar `.env.example` como plantilla con valores ficticios.

**PostgreSQL (Docker Compose):**
```
POSTGRES_DB=proyectos_integradores
POSTGRES_USER=pg_admin
POSTGRES_PASSWORD=CAMBIAR_POR_CONTRASENA_SEGURA
POSTGRES_PORT=5432
```

**Spring Boot (.env):**
```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=proyectos_integradores
DB_USER=pg_admin
DB_PASSWORD=CAMBIAR_POR_CONTRASENA_SEGURA
JWT_SECRET=CAMBIAR_POR_CLAVE_ALEATORIA_MINIMO_64_CARACTERES
JWT_EXPIRATION_MS=28800000
ALLOWED_ORIGINS=http://localhost:5173
LLM_API_KEY=CAMBIAR_POR_API_KEY_DEL_LLM
LLM_API_URL=https://api.anthropic.com/v1/messages
LLM_MODEL=claude-sonnet-4-6
```

**Django (.env):**
```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=proyectos_integradores
DB_USER=pg_admin
DB_PASSWORD=CAMBIAR_POR_CONTRASENA_SEGURA
DJANGO_SECRET_KEY=CAMBIAR_POR_CLAVE_ALEATORIA_LARGA
JWT_SECRET_DJANGO=CAMBIAR_POR_CLAVE_ALEATORIA_MINIMO_64_CARACTERES
JWT_EXPIRATION_HOURS=8
ALLOWED_ORIGINS=http://localhost:5173
ADMIN_EMAIL=encargado@tecsup.edu.pe
ADMIN_PASSWORD=CAMBIAR_POR_CONTRASENA_SEGURA
```

**React (.env):**
```
VITE_API_ESTUDIANTE_URL=http://localhost:8080
VITE_API_ADMIN_URL=http://localhost:8000
```

**Kotlin Android (build.gradle.kts — NO es .env):**
```kotlin
buildConfigField("String", "API_ESTUDIANTE_URL", "\"http://10.0.2.2:8080\"")
buildConfigField("String", "LLM_TIMEOUT_SECONDS", "\"15\"")
```

### 4.9 Reglas de UX Transversales

- **Estudiante sin grupo que intenta proponer:** mostrar mensaje amigable con botón directo a registrar grupo. NUNCA un error genérico.
- **Grupo con proyecto NO_APROBADO:** no puede registrar nuevas propuestas en ese periodo. Mostrar mensaje que indique que el ciclo está cerrado para su grupo.
- **Historial del chat IA:** solo de sesión. Al cerrar o recargar la pantalla, el historial se pierde. No persiste en BD.
- **Ventana de contexto del chat IA:** máximo 10 mensajes (últimos 5 del usuario + 5 del asistente) enviados al LLM en cada llamada.

---

## 5. Sprints e Historias de Usuario

### Sprint 1 — Autenticación y Preparación de Datos Históricos

**Meta:** tener el sistema funcionando con autenticación completa y la data histórica limpia e importada a la base de datos.

| ID | Item | Tipo | Épica |
|----|------|------|-------|
| TT-01 | Limpieza y normalización del CSV histórico | Tarea Técnica | EP-06 |
| TT-02 | Carga de data histórica limpia a la BD | Tarea Técnica | EP-06 |
| TT-03 | Creación de credencial de administrador (seed) | Tarea Técnica | EP-01 |
| HU-01 | Registro de estudiante | Historia de Usuario | EP-01 |
| HU-02a | Inicio de sesión de estudiante | Historia de Usuario | EP-01 |
| HU-02b | Inicio de sesión de administrador | Historia de Usuario | EP-01 |

---

#### TT-01 — Limpieza y normalización del CSV histórico

**Tipo:** Tarea Técnica | **Épica:** EP-06

**Objetivo:** Dejar el archivo `docs/data/proyectos_historicos.csv` en condiciones de ser cargado a la BD sin errores, duplicados ni inconsistencias.

**Reglas aplicadas:**
- Excluir todos los registros del ciclo 2020-I (no tienen campo nombre).
- Excluir las filas 149–174 del bloque "Final" (códigos 1A–6A): confirmado como error de captura — son registros duplicados de proyectos ya existentes en la parte superior del archivo.
- No migrar el campo `Estado` de los registros históricos — ese campo no existe para proyectos de `origen=HISTORICO`.
- No migrar el campo `integrantes` — no existe en el CSV histórico.
- Generar el código de cada proyecto como combinación ciclo + sección (ej. "2025-II-1A").
- Conservar `nombre` y `descripción` tal como están en el CSV, solo normalizando encoding si aplica.

**Tareas Técnicas:**
- Leer el archivo `docs/data/proyectos_historicos.csv` aplicando las reglas anteriores (script Python con pandas).
- Generar un archivo de salida normalizado con columnas: `ciclo`, `codigo_seccion`, `nombre`, `descripcion`.
- Documentar el conteo: registros originales, excluidos (2020-I), descartados (duplicados), válidos para carga.

**Definition of Done:**
- El archivo de salida no contiene registros del ciclo 2020-I.
- El archivo de salida no contiene las filas duplicadas identificadas.
- Ningún registro de salida tiene el campo `nombre` vacío.
- El código ciclo+sección es único para cada fila del archivo de salida.

---

#### TT-02 — Carga de data histórica limpia a la BD

**Tipo:** Tarea Técnica | **Épica:** EP-06

**Objetivo:** Insertar el archivo limpio (resultado de TT-01) en la tabla `Proyecto` de la BD con `origen=HISTORICO`.

**Tareas Técnicas:**
- `[Backend – Django]` Instalar pandas para leer el archivo limpio.
- `[Backend – Django]` Crear el modelo `Proyecto` según la sección 3.2 del Modelo de Datos. Django es dueño de esta migración.
- `[Backend – Django]` Crear un management command que lea el archivo limpio e inserte registros con `origen=HISTORICO`.
- `[Backend – Django]` Idempotencia: si el comando se ejecuta dos veces, no generar duplicados (verificar por `ciclo+codigo_seccion` antes de insertar).
- `[Backend – Django]` Loggear resumen: registros importados, omitidos por ya existir.

**Definition of Done:**
- Los proyectos históricos aparecen en la tabla `Proyecto` con `origen=HISTORICO`.
- Los campos `grupo_id`, `estado`, `url` y `comentario_evaluacion` son NULL en todos los registros históricos.
- Ejecutar el comando dos veces no genera duplicados.

---

#### TT-03 — Creación de credencial de administrador (seed)

**Tipo:** Tarea Técnica | **Épica:** EP-01

**Objetivo:** Crear la credencial única de administrador antes de iniciar el desarrollo de HU-02b.

**Reglas aplicadas:**
- El administrador no se autoregistra desde la aplicación. Su credencial se crea una vez por el equipo de desarrollo.

**Tareas Técnicas:**
- `[Backend – Django]` Crear superusuario administrador vía comando `createsuperuser` de Django.
- `[Backend – Django]` Documentar las credenciales en variables de entorno (.env): `ADMIN_EMAIL` y `ADMIN_PASSWORD`. Nunca en código fuente.

**Definition of Done:**
- Existe al menos una credencial de administrador funcional antes de iniciar HU-02b.
- Las credenciales no están expuestas en el repositorio de código.

---

#### HU-01 — Registro de estudiante

**Épica:** EP-01 | **Rol:** estudiante nuevo

**Historia:** Como estudiante nuevo, quiero registrarme en el sistema con mis datos académicos, para poder acceder a todas las funcionalidades de la plataforma.

**Criterios de Aceptación:**
- CA1: El formulario solicita: nombre completo, código de estudiante, correo institucional y contraseña.
- CA2: El sistema valida que el correo tenga el dominio institucional correcto.
- CA3: El sistema retorna error `CORREO_DUPLICADO` si el correo ya está registrado.
- CA4: El sistema retorna error `CORREO_INVALIDO` si el dominio no es institucional.
- CA5: Tras el registro exitoso, el estudiante es redirigido a la pantalla de inicio de sesión.
- CA6: La contraseña se almacena con hash BCrypt, nunca en texto plano.

**Tareas Técnicas:**
- `[Backend – Spring Boot]` Crear las entidades `Usuario` y `Estudiante` según sección 3.2. Spring Boot opera en modo validate — Django debe haber creado las tablas primero (TT-02).
- `[Backend – Spring Boot]` Crear endpoint `POST /api/auth/registro`. Retorna `201 Created` con `{success:true, data:{id, nombre, correo, rol}}`.
- `[Backend – Spring Boot]` Validar dominio institucional y unicidad de correo. Retornar errores en formato estándar (sección 4.7).
- `[Backend – Spring Boot]` Usar BCrypt para encriptar la contraseña.
- `[Frontend Web – React]` Crear componente `FormularioRegistro` con los campos requeridos.
- `[Frontend Web – React]` Conectar al endpoint usando `VITE_API_ESTUDIANTE_URL`.
- `[Frontend Web – React]` Mostrar mensajes de error del backend debajo de cada campo.
- `[Frontend Móvil – Kotlin]` Crear pantalla de registro con Jetpack Compose.
- `[Frontend Móvil – Kotlin]` Conectar al endpoint usando Retrofit con base URL `API_ESTUDIANTE_URL` (10.0.2.2:8080).

**Definition of Done:**
- `POST /api/auth/registro` retorna 201 con datos del usuario (sin password).
- Errores `CORREO_DUPLICADO` y `CORREO_INVALIDO` retornan en formato estándar (sección 4.7).
- Se puede registrar un usuario nuevo desde web y desde la app móvil.
- Código en rama `feature/HU-01-registro` con Pull Request aprobado.

---

#### HU-02a — Inicio de sesión de estudiante

**Épica:** EP-01 | **Rol:** estudiante registrado

**Historia:** Como estudiante registrado, quiero iniciar sesión con mi correo y contraseña, para poder acceder a mi panel personalizado.

**Criterios de Aceptación:**
- CA1: El formulario solicita correo y contraseña.
- CA2: Si las credenciales son correctas, Spring Boot genera un JWT con `rol=ESTUDIANTE` y expiración de 8 horas.
- CA3: Si las credenciales son incorrectas, retorna error `CREDENCIALES_INCORRECTAS`.
- CA4: Tras login exitoso, React redirige a `/dashboard-estudiante`.

**Tareas Técnicas:**
- `[Backend – Spring Boot]` Configurar Spring Security con JWT exclusivo para rol ESTUDIANTE.
- `[Backend – Spring Boot]` Crear endpoint `POST /api/auth/login`. Retorna `{success:true, data:{token, rol}}`.
- `[Backend – Spring Boot]` Proteger todos los endpoints `/api/**` para que solo acepten tokens con `rol=ESTUDIANTE`.
- `[Frontend Web – React]` Crear componente `FormularioLogin` (estudiante).
- `[Frontend Web – React]` Guardar token en localStorage. Redirigir a `/dashboard-estudiante`.
- `[Frontend Móvil – Kotlin]` Crear pantalla de login con Retrofit. Guardar token en SharedPreferences.

**Definition of Done:**
- `POST /api/auth/login` retorna 200 con JWT al hacer login correcto.
- `POST /api/auth/login` retorna 401 con error `CREDENCIALES_INCORRECTAS` al fallar.
- Endpoints `/api/**` retornan 403 si se accede con token de administrador.
- Código en rama `feature/HU-02a-login-estudiante` con PR aprobado.

---

#### HU-02b — Inicio de sesión de administrador

**Épica:** EP-01 | **Rol:** administrador

**Historia:** Como administrador, quiero iniciar sesión con mi correo y contraseña, para poder acceder al panel de administración.

**Criterios de Aceptación:**
- CA1: El formulario solicita correo y contraseña.
- CA2: Si las credenciales son correctas, Django genera un JWT con `rol=ADMIN` y expiración de 8 horas.
- CA3: Si las credenciales son incorrectas, retorna error `CREDENCIALES_INCORRECTAS`.
- CA4: Tras login exitoso, React redirige a `/dashboard-admin`.
- CA5: No existe flujo de autoregistro para administradores — la credencial ya fue creada en TT-03.

**Tareas Técnicas:**
- `[Backend – Django]` Instalar `djangorestframework-simplejwt`.
- `[Backend – Django]` Crear endpoint `POST /api/admin/auth/login`. Retorna `{success:true, data:{token, rol}}`.
- `[Backend – Django]` Proteger todos los endpoints `/api/admin/**` para que solo acepten tokens con `rol=ADMIN`.
- `[Frontend Web – React]` Crear componente `FormularioLogin` (administrador), separado del login de estudiante.
- `[Frontend Web – React]` Guardar token en localStorage usando `VITE_API_ADMIN_URL`. Redirigir a `/dashboard-admin`.

**Definition of Done:**
- `POST /api/admin/auth/login` retorna 200 con JWT al hacer login correcto.
- `POST /api/admin/auth/login` retorna 401 con error `CREDENCIALES_INCORRECTAS` al fallar.
- Endpoints `/api/admin/**` retornan 403 si se accede con token de estudiante.
- Código en rama `feature/HU-02b-login-admin` con PR aprobado.

---

### Sprint 2 — Catálogo, Detalle de Proyecto y Registro de Grupo

**Meta:** que el estudiante pueda explorar el catálogo completo y registrar su grupo de trabajo.

| ID | Historia de Usuario | Épica | Rol |
|----|---------------------|-------|-----|
| HU-04 | Explorar catálogo de proyectos | EP-02 | estudiante |
| HU-05 | Ver detalle de un proyecto | EP-02 | estudiante |
| HU-06 | Registrar grupo de trabajo | EP-03 | estudiante |

---

#### HU-04 — Explorar catálogo de proyectos

**Épica:** EP-02 | **Rol:** estudiante

**Historia:** Como estudiante, quiero ver un listado de todos los proyectos del sistema (históricos y nuevos), para poder conocer los temas ya desarrollados e inspirarme para el mío.

**Criterios de Aceptación:**
- CA1: La pantalla muestra una grilla de proyectos con: nombre, ciclo, código y descripción corta.
- CA2: Para proyectos de `origen=NUEVO`, la tarjeta muestra un badge de estado (amarillo=En desarrollo, verde=Aprobado, rojo=No aprobado).
- CA3: Para proyectos de `origen=HISTORICO`, la tarjeta **no muestra** badge de estado.
- CA4: Se puede filtrar por ciclo académico — aplica a todos los proyectos.
- CA5: Se puede filtrar por estado — este filtro solo afecta proyectos de `origen=NUEVO`. Los históricos nunca aparecen al aplicar este filtro.
- CA6: La UI muestra un texto de ayuda junto al filtro de estado: *"Este filtro aplica solo a proyectos del ciclo actual"*.
- CA7: Se puede buscar por nombre de proyecto.
- CA8: La lista está paginada (máximo 20 proyectos por página).

**Tareas Técnicas:**
- `[Backend – Spring Boot]` Crear endpoint `GET /api/proyectos` con paginación. Parámetros: `ciclo`, `estado` (solo filtra `origen=NUEVO`), `busqueda`.
- `[Backend – Spring Boot]` El filtro por estado debe excluir automáticamente registros con `origen=HISTORICO`.
- `[Frontend Web – React]` Crear componente `CatalogoProyectos`. Badge de estado renderizado condicionalmente solo si `origen=NUEVO`.
- `[Frontend Web – React]` Implementar filtros de ciclo y estado como dropdowns, con texto de ayuda en el filtro de estado.
- `[Frontend Web – React]` Implementar búsqueda por nombre y paginación con Anterior/Siguiente.
- `[Frontend Móvil – Kotlin]` Crear pantalla de catálogo con lista scrolleable y mismas reglas condicionales de UI.

**Definition of Done:**
- El filtro por estado nunca retorna proyectos históricos.
- Las tarjetas de proyectos históricos no muestran badge de estado en ningún caso.
- Código en rama `feature/HU-04-catalogo` con PR aprobado.

---

#### HU-05 — Ver detalle de un proyecto

**Épica:** EP-02 | **Rol:** estudiante

**Historia:** Como estudiante, quiero ver toda la información disponible de un proyecto específico, para poder entender a fondo de qué trata.

**Criterios de Aceptación:**
- CA1: Para `origen=HISTORICO`: muestra nombre, descripción completa, ciclo, código de sección. No muestra estado, integrantes ni URL.
- CA2: Para `origen=NUEVO`: muestra nombre, descripción, ciclo, código de grupo, integrantes del grupo, estado (badge de color), URL si existe, y comentario de evaluación si existe.
- CA3: Badge de estado: amarillo=En desarrollo, verde=Aprobado, rojo=No aprobado (solo `origen=NUEVO`).
- CA4: Botón para volver al catálogo sin perder los filtros aplicados.

**Tareas Técnicas:**
- `[Backend – Spring Boot]` Crear endpoint `GET /api/proyectos/{id}`. Si `origen=NUEVO`, incluir integrantes del grupo vía join `Grupo → GrupoIntegrante → Estudiante`.
- `[Frontend Web – React]` Crear componente `DetalleProyecto` con renderizado condicional según `origen`.
- `[Frontend Móvil – Kotlin]` Crear pantalla de detalle con la misma lógica condicional.

**Definition of Done:**
- Proyectos históricos nunca muestran estado, integrantes ni URL en el detalle.
- Proyectos nuevos muestran todos sus campos disponibles correctamente.
- El botón volver regresa al catálogo con los filtros previos intactos.
- Código en rama `feature/HU-05-detalle-proyecto` con PR aprobado.

---

#### HU-06 — Registrar grupo de trabajo

**Épica:** EP-03 | **Rol:** estudiante

**Historia:** Como estudiante, quiero registrar mi grupo indicando mis compañeros y un código de grupo, para poder asociar mi futura propuesta de proyecto a mi equipo.

**Criterios de Aceptación:**
- CA1: El estudiante busca compañeros por nombre o apellido. El sistema retorna una lista de coincidencias con nombre y código de estudiante para confirmar la selección.
- CA2: El sistema valida que cada compañero esté registrado (existe en tabla `Estudiante`).
- CA3: El sistema valida que ningún compañero ya pertenezca a otro grupo en el mismo periodo. Error: `ESTUDIANTE_YA_TIENE_GRUPO`.
- CA4: El grupo tiene un `codigo_grupo` único, ingresado por el estudiante o asignado por el sistema.
- CA5: Una vez creado el grupo, no se pueden agregar más integrantes (solo el admin puede modificarlo).

**Tareas Técnicas:**
- `[Backend – Spring Boot]` Crear endpoint `GET /api/estudiantes/buscar?nombre=XXX`. Busca coincidencias parciales en `Usuario.nombre` (rol=ESTUDIANTE). Retorna lista con `nombre` y `codigo_estudiante`.
- `[Backend – Spring Boot]` Crear endpoint `POST /api/grupos`. Valida unicidad de `codigo_grupo` y que ningún integrante ya tenga grupo en ese periodo.
- `[Backend – Spring Boot]` Crear registros en `Grupo` y `GrupoIntegrante`.
- `[Frontend Web – React]` Campo de búsqueda de compañeros por nombre/apellido. Lista desplegable de coincidencias con nombre + código de estudiante.
- `[Frontend Web – React]` Compañeros seleccionados se muestran como chips/tags removibles.
- `[Frontend Móvil – Kotlin]` Pantalla equivalente con misma lógica de búsqueda.

**Definition of Done:**
- Un estudiante puede crear un grupo y agregar compañeros mediante búsqueda por nombre.
- El sistema rechaza agregar un estudiante que ya tiene grupo en ese periodo.
- Código en rama `feature/HU-06-registro-grupo` con PR aprobado.

---

### Sprint 3 — Propuesta, Revisión Administrativa y Corrección

**Meta:** que el flujo completo de propuesta, revisión y corrección funcione entre estudiante y administrador.

| ID | Historia de Usuario | Épica | Rol |
|----|---------------------|-------|-----|
| HU-07 | Registrar propuesta de proyecto | EP-03 | estudiante con grupo registrado |
| HU-08 | Revisar y aprobar/observar propuestas | EP-04 | administrador |
| HU-09 | Corregir propuesta observada | EP-03 | estudiante con propuesta observada |

---

#### HU-07 — Registrar propuesta de proyecto

**Épica:** EP-03 | **Rol:** estudiante con grupo registrado

**Historia:** Como estudiante con grupo registrado, quiero registrar la propuesta de mi proyecto integrador, para que el encargado la revise y la apruebe.

**Criterios de Aceptación:**
- CA1: Solo estudiantes con grupo registrado pueden proponer. Si no tiene grupo, el sistema muestra mensaje amigable con botón directo a registrar grupo.
- CA2: El formulario solicita: nombre del proyecto y descripción detallada. No incluye URL.
- CA3: Al enviar, la propuesta queda en estado `PENDIENTE`.
- CA4: El estudiante ve el estado actual de su propuesta en su panel con badge de color.
- CA5: No se puede enviar segunda propuesta si ya hay una en estado `PENDIENTE` o `APROBADO`. Error: `PROPUESTA_ACTIVA`.
- CA6: Cuando la propuesta cambia a estado `APROBADO`, el sistema notifica al estudiante y le ofrece (opcionalmente) registrar la URL del repositorio. Puede completarla después desde su panel mientras el proyecto esté `EN_DESARROLLO`.

**Tareas Técnicas:**
- `[Backend – Spring Boot]` Crear endpoint `POST /api/propuestas`. Verificar que el estudiante tiene grupo. Verificar que no tiene propuesta activa.
- `[Backend – Spring Boot]` Al cambiar `Propuesta.estado` a `APROBADO`, crear automáticamente `Proyecto` (`origen=NUEVO`, `estado=EN_DESARROLLO`, `url=NULL`, `grupo_id` del grupo de la propuesta).
- `[Backend – Spring Boot]` Crear endpoint `PUT /api/proyectos/{id}/url` para que el grupo registre o actualice la URL mientras el proyecto esté `EN_DESARROLLO`.
- `[Frontend Web – React]` Si el estudiante no tiene grupo, mostrar mensaje amigable con botón a HU-06.
- `[Frontend Web – React]` Formulario de propuesta con campos nombre y descripción (sin URL).
- `[Frontend Web – React]` Al detectar estado `APROBADO`, mostrar modal opcional para ingresar URL del repositorio.
- `[Frontend Móvil – Kotlin]` Misma lógica de pantalla de propuesta y modal de URL.

**Definition of Done:**
- Estudiante sin grupo ve mensaje amigable, no un error genérico.
- Propuesta creada queda en estado `PENDIENTE`.
- Al aprobarse la propuesta, se crea automáticamente el `Proyecto` con `estado=EN_DESARROLLO`.
- Código en rama `feature/HU-07-propuesta` con PR aprobado.

---

#### HU-08 — Revisar y aprobar/observar propuestas (Administrador)

**Épica:** EP-04 | **Rol:** administrador

**Historia:** Como administrador, quiero ver todas las propuestas y poder aprobarlas o enviar una observación, para gestionar el proceso de aprobación de proyectos del ciclo actual.

**Criterios de Aceptación:**
- CA1: El administrador ve una lista de todas las propuestas con su estado actual.
- CA2: Puede filtrar por estado: Pendientes, Aprobadas, Observadas.
- CA3: Puede ver el detalle completo de una propuesta: nombre, descripción, grupo, integrantes.
- CA4: Puede aprobar una propuesta (cambia estado a `APROBADO`, dispara creación automática del Proyecto).
- CA5: Puede observar una propuesta escribiendo un comentario (cambia estado a `OBSERVADO`).
- CA6: El cambio de estado se refleja en el panel del estudiante la próxima vez que consulte vía REST.

**Tareas Técnicas:**
- `[Backend – Django]` Crear endpoint `GET /api/admin/propuestas` con filtro por estado.
- `[Backend – Django]` Crear endpoint `PUT /api/admin/propuestas/{id}/aprobar`. Cambia estado a `APROBADO`. La creación del Proyecto la ejecuta Spring Boot automáticamente.
- `[Backend – Django]` Crear endpoint `PUT /api/admin/propuestas/{id}/observar`. Recibe comentario, cambia estado a `OBSERVADO`, guarda en `comentario_observacion`.
- `[Frontend Web – React]` Panel con lista de propuestas, filtros por estado y modal de detalle.
- `[Frontend Web – React]` Botón Aprobar y botón Observar. El botón Observar abre un textarea obligatorio antes de confirmar.

**Definition of Done:**
- Admin puede aprobar una propuesta; el Proyecto correspondiente se crea en la BD.
- Admin puede observar con comentario; el estudiante ve el nuevo estado y comentario al consultar.
- Código en rama `feature/HU-08-admin-revision` con PR aprobado.

---

#### HU-09 — Corregir propuesta observada

**Épica:** EP-03 | **Rol:** estudiante con propuesta observada

**Historia:** Como estudiante con propuesta observada, quiero editar mi propuesta para corregir lo indicado en la observación y reenviarla, para que el administrador la vuelva a revisar con los cambios hechos.

**Criterios de Aceptación:**
- CA1: Solo se puede editar una propuesta en estado `OBSERVADO`. Error `ESTADO_INVALIDO` si el estado es diferente.
- CA2: El estudiante ve el comentario de observación del administrador antes del formulario de edición.
- CA3: Puede editar nombre y descripción. No incluye URL.
- CA4: Al guardar, el estado vuelve a `PENDIENTE` automáticamente.
- CA5: El administrador puede ver el historial de versiones vía `PropuestaVersion`.

**Tareas Técnicas:**
- `[Backend – Spring Boot]` Crear endpoint `PUT /api/propuestas/{id}`. Solo permite editar si `estado=OBSERVADO`.
- `[Backend – Spring Boot]` Antes de sobreescribir, guardar snapshot en `PropuestaVersion` (nombre, descripcion, version_fecha).
- `[Backend – Spring Boot]` Al guardar, cambiar estado a `PENDIENTE` automáticamente.
- `[Frontend Web – React]` Mostrar `comentario_observacion` antes del formulario de edición.
- `[Frontend Web – React]` Formulario de edición habilitado solo si `estado=OBSERVADO`.
- `[Frontend Móvil – Kotlin]` Pantalla equivalente de edición.

**Definition of Done:**
- El estudiante puede editar nombre y descripción de propuesta observada.
- El estado vuelve a `PENDIENTE` tras guardar.
- `PropuestaVersion` guarda el snapshot correctamente.
- Código en rama `feature/HU-09-correccion` con PR aprobado.

---

### Sprint 4 — Asistente IA y Dashboard de Estadísticas

**Meta:** que el Asistente IA esté operativo y el dashboard administrativo sea funcional.

| ID | Historia de Usuario | Épica | Rol |
|----|---------------------|-------|-----|
| HU-10 | Asistente IA para consultas sobre proyectos | EP-05 | estudiante |
| HU-11 | Dashboard de estadísticas de proyectos | EP-04 | administrador |

---

#### HU-10 — Asistente IA para consultas sobre proyectos

**Épica:** EP-05 | **Rol:** estudiante

**Historia:** Como estudiante, quiero hacer preguntas en lenguaje natural sobre los proyectos históricos y nuevos, para poder encontrar información rápidamente sin buscar manualmente.

**Criterios de Aceptación:**
- CA1: El estudiante puede escribir preguntas como "¿Qué proyectos de inteligencia artificial hay?".
- CA2: El asistente responde con información basada en la tabla `Proyecto` (históricos y nuevos).
- CA3: El asistente puede recomendar proyectos similares y sugerir temas aún no desarrollados.
- CA4: El contexto enviado al LLM distingue explícitamente `origen=HISTORICO` (solo nombre, descripción, ciclo, código) y `origen=NUEVO` (todos los campos). **El asistente nunca inventa estado, integrantes ni URL para proyectos históricos.**
- CA5: Ventana de contexto: máximo 10 mensajes anteriores (últimos 5 del usuario + 5 del asistente).
- CA6: El historial del chat es solo de sesión — se pierde al cerrar o recargar la pantalla.
- CA7: Las respuestas se muestran de forma clara y amigable.

**Tareas Técnicas:**
- `[Backend – Spring Boot]` Integrar API del LLM (ver variables `LLM_API_KEY`, `LLM_API_URL`, `LLM_MODEL` en sección 4.8).
- `[Backend – Spring Boot]` Crear endpoint `POST /api/asistente/chat`. Recibe `{pregunta, historial[]}` donde historial son los últimos 10 mensajes de la sesión.
- `[Backend – Spring Boot]` Antes de llamar al LLM, consultar tabla `Proyecto` y construir contexto estructurado por origen. Solo incluir los campos que aplican según origen.
- `[Backend – Spring Boot]` System prompt del LLM debe incluir explícitamente: *"Si un proyecto es de origen HISTORICO, solo tienes disponibles nombre, descripción, ciclo y código de sección. No menciones estado, integrantes ni URL para estos proyectos — esos datos no existen y no debes inventarlos."*
- `[Frontend Web – React]` Componente de chat con burbuja de usuario y burbuja de asistente.
- `[Frontend Web – React]` Indicador de "escribiendo..." mientras espera la respuesta.
- `[Frontend Web – React]` El frontend mantiene el historial de la sesión en estado local (`useState`), lo envía al endpoint en cada mensaje.
- `[Frontend Móvil – Kotlin]` Pantalla de chat equivalente.

**Definition of Done:**
- El asistente responde con información real de la BD sin inventar campos inexistentes para históricos.
- El endpoint responde en menos de 10 segundos.
- Código en rama `feature/HU-10-asistente-ia` con PR aprobado.

---

#### HU-11 — Dashboard de estadísticas de proyectos

**Épica:** EP-04 | **Rol:** administrador

**Historia:** Como administrador, quiero ver estadísticas visuales sobre los proyectos del sistema, para tener una visión rápida del estado general por periodo y tomar decisiones de gestión.

**Criterios de Aceptación:**
- CA1: El dashboard muestra: total de proyectos, proyectos por estado (`origen=NUEVO`, gráfico de torta/dona), proyectos por ciclo (todos los orígenes, gráfico de barras).
- CA2: Los datos se actualizan al recargar el dashboard.
- CA3: Solo el administrador tiene acceso — no aparece en el panel del estudiante ni en la app móvil.
- CA4: Es responsivo.

**Tareas Técnicas:**
- `[Backend – Django]` Crear endpoint `GET /api/admin/estadisticas` protegido por `rol=ADMIN`. Retorna: `total_proyectos`, `por_estado` (solo `origen=NUEVO`), `por_ciclo` (todos los orígenes).
- `[Frontend Web – React]` Instalar Recharts. Crear componente Dashboard con gráfico de torta y barras.
- `[Frontend Web – React]` Conectar al endpoint usando `VITE_API_ADMIN_URL`.

**Definition of Done:**
- Los gráficos muestran datos reales de la BD.
- El dashboard carga en menos de 3 segundos.
- Solo accesible desde el panel admin.
- Código en rama `feature/HU-11-dashboard-admin` con PR aprobado.

---

## 6. Alcance y Exclusiones

Las siguientes funcionalidades están explícitamente fuera del alcance de este proyecto:

| Funcionalidad | Razón de exclusión |
|---------------|--------------------|
| Recuperación de contraseña | Requiere servicio de email externo. Reset manual por admin desde Django admin. |
| Blacklist de tokens JWT (logout completo) | El logout simple (borrar token en frontend) es suficiente para el alcance académico. |
| Activar/desactivar cuentas de estudiantes desde UI | Complejidad innecesaria. Gestión de cuentas via Django admin si se requiere. |
| Historial persistente del chat IA entre sesiones | El historial de sesión es suficiente. Persistir requiere tabla adicional en BD. |
| App móvil para administrador | El panel de administración es exclusivamente web (React + Django). |
| Notificaciones push | Fuera del alcance tecnológico del proyecto. |
