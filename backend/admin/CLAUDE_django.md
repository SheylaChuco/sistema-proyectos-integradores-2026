# CLAUDE.md — backend/admin/ (Django)

## INSTRUCCIÓN CRÍTICA
Lee primero el CLAUDE.md raíz del repositorio. Este archivo agrega las reglas específicas de Django que aplican SOLO a este módulo. Ante cualquier contradicción, el CLAUDE.md raíz tiene prioridad.

---

## 1. Responsabilidad de este módulo

Este módulo es el backend del panel de administración. Solo atiende al rol ADMIN.

Endpoints que viven aquí:
- POST /api/admin/auth/login
- GET /api/admin/propuestas/
- PUT /api/admin/propuestas/{id}/aprobar/
- PUT /api/admin/propuestas/{id}/observar/
- GET /api/admin/estadisticas/
- Management command: importar data histórica (TT-02)

NO implementes aquí ningún endpoint del estudiante. Si dudas, consulta el expediente técnico sección 1.1.

---

## 2. Stack y versiones

- Python 3.11+
- Django 4.2 LTS
- Django REST Framework 3.14+
- djangorestframework-simplejwt 5.3+
- psycopg2-binary (conector PostgreSQL)
- openpyxl (lectura de Excel para TT-01/TT-02)
- pytest + pytest-django (pruebas)

---

## 3. Arquitectura MTV + DRF — estructura obligatoria

```
backend/admin/
├── manage.py
├── config/                    ← configuración del proyecto Django
│   ├── settings/
│   │   ├── base.py            ← settings comunes
│   │   ├── development.py     ← settings de desarrollo (DEBUG=True)
│   │   └── production.py      ← settings de producción
│   ├── urls.py                ← urls raíz
│   └── wsgi.py
├── apps/
│   ├── authentication/        ← login admin, JWT
│   │   ├── models.py
│   │   ├── serializers.py
│   │   ├── views.py
│   │   ├── urls.py
│   │   └── tests/
│   ├── propuestas/            ← gestión de propuestas (HU-08)
│   │   ├── models.py
│   │   ├── serializers.py
│   │   ├── views.py
│   │   ├── urls.py
│   │   └── tests/
│   ├── proyectos/             ← modelos y migraciones (dueño del schema)
│   │   ├── models.py          ← TODAS las entidades del sistema
│   │   ├── migrations/        ← TODAS las migraciones
│   │   └── admin.py
│   ├── estadisticas/          ← dashboard admin (HU-11)
│   │   ├── views.py
│   │   ├── urls.py
│   │   └── tests/
│   └── importacion/           ← TT-01 y TT-02 (management commands)
│       └── management/
│           └── commands/
│               ├── limpiar_excel.py
│               └── importar_historicos.py
├── requirements.txt
├── .env.example
└── CLAUDE.md                  ← este archivo
```

---

## 4. Reglas de arquitectura Django — obligatorias

**Regla 1 — Modelos en una sola app:**
TODOS los modelos del sistema (Usuario, Estudiante, Administrador, Grupo, GrupoIntegrante, Propuesta, PropuestaVersion, Proyecto) viven en `apps/proyectos/models.py`. Django es dueño de TODAS las migraciones. Nunca crees modelos en otras apps — solo importa desde `apps.proyectos.models`.

**Regla 2 — Serializers obligatorios:**
Nunca devuelvas un objeto del modelo directamente en una vista. Siempre usa un serializer de DRF. Los serializers validan los datos de entrada Y formatean la salida.

**Regla 3 — Vistas basadas en clases (APIView o ViewSet):**
Usa `APIView` para endpoints simples o `ModelViewSet` cuando sea CRUD completo. Nunca uses funciones de vista (`def mi_vista(request)`) — siempre clases.

**Regla 4 — Formato de respuesta estándar:**
Todos los endpoints deben devolver el formato definido en el CLAUDE.md raíz sección 5. Crea un helper reutilizable:
```python
def success_response(data, status=200):
    return Response({"success": True, "data": data}, status=status)

def error_response(code, message, status=400):
    return Response({"success": False, "error": {"code": code, "message": message}}, status=status)
```

**Regla 5 — Permisos en cada vista:**
Cada vista de `/api/admin/**` debe tener:
```python
permission_classes = [IsAuthenticated, IsAdminUser]
```
Nunca dejes una vista sin permission_classes. Sin esta configuración, cualquier usuario puede acceder.

**Regla 6 — Settings separados por entorno:**
Nunca uses un solo `settings.py`. Usa `config/settings/base.py` + `development.py` + `production.py`. El `DEBUG=True` solo en development, nunca en base ni production.

---

## 5. Migraciones — dueño del schema completo

Django es el único que crea y modifica tablas. Sigue este orden siempre:

```bash
# 1. Crear la migración después de cambiar un modelo
python manage.py makemigrations

# 2. Aplicar migraciones (siempre antes de levantar Spring Boot)
python manage.py migrate

# 3. Verificar que las migraciones estén aplicadas
python manage.py showmigrations
```

Nunca edites archivos de migración manualmente. Si una migración falla, reviértela y corrige el modelo.

---

## 6. Pruebas con pytest — obligatorias

Estructura de pruebas para cada app:
```
apps/propuestas/tests/
├── __init__.py
├── test_views.py       ← pruebas de endpoints con APIClient
├── test_serializers.py ← pruebas de validación
└── conftest.py         ← fixtures reutilizables
```

Cada endpoint nuevo debe tener al menos:
- Prueba del caso exitoso (200/201)
- Prueba de acceso sin token (401)
- Prueba de acceso con token de estudiante (403)
- Prueba de datos inválidos (400/422)

Ejemplo de prueba de endpoint:
```python
def test_aprobar_propuesta_exitoso(api_client, admin_token, propuesta_pendiente):
    api_client.credentials(HTTP_AUTHORIZATION=f'Bearer {admin_token}')
    response = api_client.put(f'/api/admin/propuestas/{propuesta_pendiente.id}/aprobar/')
    assert response.status_code == 200
    assert response.data['success'] is True
```

Corre las pruebas con:
```bash
pytest --cov=apps --cov-report=term-missing
```
Cobertura mínima: 70%.

---

## 7. Management commands para importación (TT-01 y TT-02)

El comando de importación debe ser idempotente (ejecutarlo dos veces no duplica datos).

```python
# apps/importacion/management/commands/importar_historicos.py
class Command(BaseCommand):
    help = 'Importa proyectos históricos desde Excel limpio'

    def handle(self, *args, **options):
        # 1. Leer archivo limpio (resultado de TT-01)
        # 2. Por cada fila: verificar si ya existe (ciclo + codigo_seccion)
        # 3. Si no existe: crear Proyecto(origen='HISTORICO', ...)
        # 4. Loggear resumen: importados, omitidos
        pass
```

Excluye siempre: ciclo 2020-I (sin nombre) y filas duplicadas confirmadas (149-174 del Excel original).

---

## 8. CORS en Django

```python
# En base.py
INSTALLED_APPS = [..., 'corsheaders']
MIDDLEWARE = ['corsheaders.middleware.CorsMiddleware', ...]
CORS_ALLOWED_ORIGINS = [os.getenv('ALLOWED_ORIGINS', 'http://localhost:5173')]
```

Instala: `pip install django-cors-headers`

