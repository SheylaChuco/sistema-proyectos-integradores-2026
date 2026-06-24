# Sistema de Gestión de Proyectos Integradores — TECSUP

Sistema web de proyectos integradores de la carrera de Desarrollo de Software de TECSUP. Permite a estudiantes explorar proyectos históricos, registrar grupos, proponer proyectos y consultar al asistente IA. Los administradores gestionan propuestas, sustentaciones y visualizan estadísticas.

---

## Tecnologías

| Módulo | Tecnología | Puerto |
|--------|-----------|--------|
| Base de datos | PostgreSQL 15 (Docker) | 5433 |
| Backend estudiante | Spring Boot 3.2 + Java 17 | 8080 |
| Backend administrador | Django 4.2 + DRF | 8000 |
| Frontend web | React 18 + Vite + Tailwind CSS | 5173 |
| Asistente IA | Google Gemini 2.0 Flash | — |

---

## Requisitos previos

Asegúrate de tener instalado:

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Java 17](https://adoptium.net/) (JDK)
- [Maven 3.8+](https://maven.apache.org/) (o usar IntelliJ IDEA con soporte Maven)
- [Python 3.11+](https://www.python.org/)
- [Node.js 20+](https://nodejs.org/) y npm
- API Key de [Google AI Studio](https://aistudio.google.com/) (gratuita)

---

## Estructura del repositorio

```
sistema-proyectos-integradores/
├── docker-compose.yml          # PostgreSQL
├── .env.example                # Plantilla de variables de entorno
├── docs/
│   ├── expediente_tecnico.md
│   └── data/
│       └── proyectos_historicos.csv
├── backend/
│   ├── admin/                  # Django — panel administrador
│   └── usuario/                # Spring Boot — panel estudiante
└── frontend/
    └── web/                    # React + Vite
```

---

## Configuración inicial

### 1. Clonar el repositorio

```bash
git clone https://github.com/SheylaChuco/sistema-proyectos-integradores.git
cd sistema-proyectos-integradores
```

### 2. Crear el archivo `.env`

Copia la plantilla y completa los valores:

```bash
cp .env.example .env
```

Edita `.env` y reemplaza los valores marcados con `CAMBIAR_POR_...`:

```env
# PostgreSQL
POSTGRES_PASSWORD=una_contrasena_segura

# Spring Boot
DB_PASSWORD=la_misma_contrasena_de_arriba
JWT_SECRET=clave_aleatoria_de_minimo_64_caracteres_para_firmar_tokens_jwt_aqui
LLM_API_KEY=tu_api_key_de_google_ai_studio   # https://aistudio.google.com/

# Django
DJANGO_SECRET_KEY=otra_clave_aleatoria_larga_para_django
JWT_SECRET_DJANGO=clave_aleatoria_de_minimo_64_caracteres_para_django_jwt
ADMIN_PASSWORD=contrasena_del_administrador
```

> **Nota:** `LLM_API_URL` y `LLM_MODEL` ya tienen valores correctos para Gemini en el `.env.example`. No es necesario cambiarlos.

---

## Levantar la base de datos

```bash
docker-compose up -d
```

Verifica que el contenedor esté corriendo:

```bash
docker ps
# Debe aparecer: proyectos_db (postgres:15-alpine)
```

---

## Backend Django (administrador) — puerto 8000

```bash
cd backend/admin

# Crear entorno virtual
python -m venv venv

# Activar (Windows)
venv\Scripts\activate
# Activar (Linux/Mac)
source venv/bin/activate

# Instalar dependencias
pip install -r requirements.txt

# Ejecutar migraciones (OBLIGATORIO antes de Spring Boot)
python manage.py migrate

# Crear el usuario administrador
python manage.py crear_admin

# Importar los 146 proyectos históricos
python manage.py importar_historicos

# Iniciar el servidor
python manage.py runserver 8000
```

> El comando `crear_admin` crea el administrador con las credenciales definidas en `ADMIN_EMAIL` y `ADMIN_PASSWORD` del `.env`.

---

## Backend Spring Boot (estudiante) — puerto 8080

> **Importante:** Django debe haber ejecutado las migraciones antes de iniciar Spring Boot.

### Con IntelliJ IDEA

1. Abrir la carpeta `backend/usuario` como proyecto Maven
2. Recargar Maven (icono de recarga en el panel Maven, o `Maven → Reload All Maven Projects`)
3. Ejecutar `ProyectosApplication.java`

### Con terminal

```bash
cd backend/usuario
mvn spring-boot:run
```

Spring Boot cargará automáticamente el `.env` de la raíz del proyecto.

---

## Frontend React — puerto 5173

```bash
cd frontend/web

# Crear archivo de variables de entorno para React
cp .env.example .env
# (o crear manualmente frontend/web/.env con el contenido de abajo)
```

Contenido de `frontend/web/.env`:

```env
VITE_API_ESTUDIANTE_URL=http://localhost:8080
VITE_API_ADMIN_URL=http://localhost:8000
```

```bash
# Instalar dependencias
npm install

# Iniciar servidor de desarrollo
npm run dev
```

Abre [http://localhost:5173](http://localhost:5173) en el navegador.

---

## Orden de inicio

Siempre respetar este orden:

```
1. docker-compose up -d          → PostgreSQL listo
2. python manage.py migrate      → tablas creadas
3. ProyectosApplication (Spring) → lee tablas existentes
4. npm run dev                   → frontend conecta a ambos backends
```

---

## Credenciales de prueba

### Administrador
| Campo | Valor |
|-------|-------|
| URL | http://localhost:5173/admin/login |
| Correo | El definido en `ADMIN_EMAIL` del `.env` |
| Contraseña | El definido en `ADMIN_PASSWORD` del `.env` |

### Estudiante
Registrarse desde [http://localhost:5173/registro](http://localhost:5173/registro) con un correo `@tecsup.edu.pe`.

---

## Funcionalidades implementadas

### Panel estudiante
- Registro e inicio de sesión con correo institucional `@tecsup.edu.pe`
- Catálogo de proyectos con filtros por ciclo, origen y búsqueda por nombre
- Detalle de proyecto con campos condicionales según su origen (histórico / nuevo)
- Registro de grupo con búsqueda de compañeros por nombre
- Registro, seguimiento y corrección de propuestas de proyecto
- Ingreso de URL de repositorio cuando el proyecto está en desarrollo
- Visualización del resultado de sustentación (aprobado / no aprobado)
- Asistente IA (Google Gemini) para consultas sobre proyectos

### Panel administrador
- Inicio de sesión con cuenta de administrador
- Gestión de propuestas: listado por estado, aprobación y observación con comentario
- Sustentaciones: evaluación de proyectos en desarrollo (aprobar / no aprobar)
- Estadísticas: contadores globales y distribución por ciclo con gráficos
- Estudiantes: lista completa con indicador de pertenencia a grupo

---

## Ejecutar pruebas

### Spring Boot (JUnit 5 + Mockito)

```bash
cd backend/usuario
mvn test
```

### Django (pytest)

```bash
cd backend/admin
# (con el entorno virtual activado)
pytest
```

---

## Variables de entorno — referencia completa

Ver [`.env.example`](.env.example) para la lista completa con descripción de cada variable.
