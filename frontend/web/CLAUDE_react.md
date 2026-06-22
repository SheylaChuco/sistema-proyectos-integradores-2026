# CLAUDE.md — frontend/web/ (React)

## INSTRUCCIÓN CRÍTICA
Lee primero el CLAUDE.md raíz del repositorio. Este archivo agrega las reglas específicas de React que aplican SOLO a este módulo. Ante cualquier contradicción, el CLAUDE.md raíz tiene prioridad.

---

## 1. Responsabilidad de este módulo

Este módulo es la interfaz web para DOS tipos de usuario:
- Panel del estudiante → consume Spring Boot (puerto 8080)
- Panel del administrador → consume Django (puerto 8000)

Ambos paneles viven en el mismo proyecto React, separados por rutas protegidas.

---

## 2. Stack y versiones

- React 18+
- TypeScript 5+ (OBLIGATORIO — nunca uses JavaScript plano)
- Vite 5+ (bundler)
- React Router v6 (routing)
- Axios (llamadas HTTP — NUNCA fetch directo)
- Tailwind CSS 3+ (estilos)
- React Hook Form (formularios)
- Recharts (gráficos — HU-11)
- Context API (estado global — NO Redux)

---

## 3. Arquitectura feature-based — estructura obligatoria

```
frontend/web/
├── src/
│   ├── features/                     ← una carpeta por épica
│   │   ├── auth/                     ← EP-01
│   │   │   ├── components/
│   │   │   │   ├── FormularioLogin.tsx
│   │   │   │   └── FormularioRegistro.tsx
│   │   │   ├── hooks/
│   │   │   │   └── useAuth.ts
│   │   │   ├── services/
│   │   │   │   └── authService.ts
│   │   │   └── types.ts
│   │   ├── catalogo/                 ← EP-02: HU-04, HU-05
│   │   │   ├── components/
│   │   │   │   ├── CatalogoProyectos.tsx
│   │   │   │   ├── TarjetaProyecto.tsx
│   │   │   │   ├── DetalleProyecto.tsx
│   │   │   │   └── BadgeEstado.tsx
│   │   │   ├── hooks/
│   │   │   │   └── useCatalogo.ts
│   │   │   └── services/
│   │   │       └── catalogoService.ts
│   │   ├── grupos/                   ← EP-03: HU-06
│   │   ├── propuestas/               ← EP-03: HU-07, HU-09
│   │   ├── asistente/                ← EP-05: HU-10
│   │   └── admin/                    ← EP-04
│   │       ├── propuestas/           ← HU-08
│   │       └── dashboard/            ← HU-11
│   ├── shared/
│   │   ├── components/               ← componentes reutilizables
│   │   │   ├── Button.tsx
│   │   │   ├── Modal.tsx
│   │   │   ├── Spinner.tsx
│   │   │   ├── Pagination.tsx
│   │   │   └── ProtectedRoute.tsx
│   │   ├── api/                      ← clientes Axios configurados
│   │   │   ├── clienteEstudiante.ts  ← SOLO para Spring Boot
│   │   │   └── clienteAdmin.ts       ← SOLO para Django
│   │   ├── hooks/
│   │   │   └── usePagination.ts
│   │   └── types/                    ← tipos TypeScript globales
│   │       ├── proyecto.types.ts
│   │       ├── propuesta.types.ts
│   │       └── api.types.ts
│   ├── router/
│   │   ├── RoutesEstudiante.tsx      ← rutas protegidas rol ESTUDIANTE
│   │   ├── RoutesAdmin.tsx           ← rutas protegidas rol ADMIN
│   │   └── AppRouter.tsx             ← router raíz
│   ├── context/
│   │   └── AuthContext.tsx           ← token + rol del usuario activo
│   └── main.tsx
├── .env.example
├── tailwind.config.js
├── tsconfig.json
└── CLAUDE.md
```

---

## 4. Reglas de arquitectura React — obligatorias

**Regla 1 — Dos clientes Axios, nunca mezclados:**
```typescript
// shared/api/clienteEstudiante.ts
const clienteEstudiante = axios.create({
  baseURL: import.meta.env.VITE_API_ESTUDIANTE_URL,
});
clienteEstudiante.interceptors.request.use(config => {
  const token = localStorage.getItem('token_estudiante');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// shared/api/clienteAdmin.ts
const clienteAdmin = axios.create({
  baseURL: import.meta.env.VITE_API_ADMIN_URL,
});
clienteAdmin.interceptors.request.use(config => {
  const token = localStorage.getItem('token_admin');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
```
Nunca uses `fetch` directamente. Nunca uses `clienteAdmin` en features del estudiante ni viceversa.

**Regla 2 — Lógica fuera de componentes:**
- Los componentes SOLO renderizan UI y manejan eventos locales
- Las llamadas a la API van en `services/`
- La lógica de negocio y estado van en `hooks/`
- Los componentes consumen hooks, los hooks consumen services

```typescript
// CORRECTO
const CatalogoProyectos = () => {
  const { proyectos, loading, filtros, setFiltros } = useCatalogo();
  return <div>...</div>;
};

// INCORRECTO — lógica de API dentro del componente
const CatalogoProyectos = () => {
  const [proyectos, setProyectos] = useState([]);
  useEffect(() => {
    axios.get('/api/proyectos').then(r => setProyectos(r.data));
  }, []);
};
```

**Regla 3 — Rutas protegidas obligatorias:**
```typescript
// shared/components/ProtectedRoute.tsx
const ProtectedRoute = ({ rol }: { rol: 'ESTUDIANTE' | 'ADMIN' }) => {
  const { token, rolUsuario } = useContext(AuthContext);
  if (!token) return <Navigate to="/login" />;
  if (rolUsuario !== rol) return <Navigate to="/login" />;
  return <Outlet />;
};
```
Toda ruta que requiera autenticación DEBE pasar por `ProtectedRoute`.

**Regla 4 — Renderizado condicional por origen — CRÍTICO:**
```typescript
// SIEMPRE verifica origen antes de mostrar campos
const TarjetaProyecto = ({ proyecto }: { proyecto: Proyecto }) => (
  <div>
    <h3>{proyecto.nombre}</h3>
    <p>{proyecto.ciclo}</p>
    {proyecto.origen === 'NUEVO' && (
      <BadgeEstado estado={proyecto.estado} />
    )}
    {/* Para HISTORICO: nunca mostrar estado, integrantes ni url */}
  </div>
);
```

**Regla 5 — Manejo de errores estándar:**
```typescript
// Interceptor de respuesta en ambos clientes
cliente.interceptors.response.use(
  response => response,
  error => {
    const code = error.response?.data?.error?.code;
    const message = error.response?.data?.error?.message;
    // Mostrar el message al usuario, usar code para lógica
    return Promise.reject({ code, message });
  }
);
```

**Regla 6 — TypeScript estricto:**
```json
// tsconfig.json
{
  "compilerOptions": {
    "strict": true,
    "noImplicitAny": true,
    "strictNullChecks": true
  }
}
```
Nunca uses `any`. Si no sabes el tipo, usa `unknown` y estrecha con type guards.

**Regla 7 — Tipos globales para entidades del expediente:**
```typescript
// shared/types/proyecto.types.ts
export type OrigenProyecto = 'HISTORICO' | 'NUEVO';
export type EstadoProyecto = 'EN_DESARROLLO' | 'APROBADO' | 'NO_APROBADO';

export interface Proyecto {
  id: number;
  origen: OrigenProyecto;
  nombre: string;
  descripcion: string;
  ciclo: string;
  codigoSeccion?: string;      // solo HISTORICO
  grupoId?: number;            // solo NUEVO
  estado?: EstadoProyecto;     // solo NUEVO
  url?: string;                // solo NUEVO
  comentarioEvaluacion?: string; // solo NUEVO
}
```

---

## 5. Chat IA — manejo del historial (HU-10)

El historial del chat vive en estado local del componente — NO en localStorage ni en BD.

```typescript
const AsistenteIA = () => {
  const [historial, setHistorial] = useState<Mensaje[]>([]);
  // Al cerrar o recargar la página, el historial se pierde — es el comportamiento esperado

  const enviarPregunta = async (pregunta: string) => {
    const nuevoHistorial = [...historial, { rol: 'user', contenido: pregunta }];
    // Enviar solo los últimos 10 mensajes al backend
    const contexto = nuevoHistorial.slice(-10);
    const respuesta = await asistentService.chat({ pregunta, historial: contexto });
    setHistorial([...nuevoHistorial, { rol: 'assistant', contenido: respuesta }]);
  };
};
```

---

## 6. Estructura de rutas

```typescript
// router/AppRouter.tsx
<Routes>
  {/* Públicas */}
  <Route path="/login" element={<LoginEstudiante />} />
  <Route path="/registro" element={<Registro />} />
  <Route path="/admin/login" element={<LoginAdmin />} />

  {/* Estudiante — protegidas */}
  <Route element={<ProtectedRoute rol="ESTUDIANTE" />}>
    <Route path="/dashboard-estudiante" element={<DashboardEstudiante />} />
    <Route path="/catalogo" element={<CatalogoProyectos />} />
    <Route path="/catalogo/:id" element={<DetalleProyecto />} />
    <Route path="/mi-grupo" element={<RegistroGrupo />} />
    <Route path="/mi-propuesta" element={<GestionPropuesta />} />
    <Route path="/asistente" element={<AsistenteIA />} />
  </Route>

  {/* Admin — protegidas */}
  <Route element={<ProtectedRoute rol="ADMIN" />}>
    <Route path="/dashboard-admin" element={<DashboardAdmin />} />
    <Route path="/admin/propuestas" element={<GestionPropuestasAdmin />} />
    <Route path="/admin/estadisticas" element={<EstadisticasAdmin />} />
  </Route>
</Routes>
```

---

## 7. Convenciones de archivos

- Un componente por archivo
- Nombre del archivo = nombre del componente (PascalCase): `FormularioLogin.tsx`
- Hooks en archivos separados con prefijo `use`: `useAuth.ts`
- Services en archivos con sufijo `Service`: `authService.ts`
- Tipos en archivos con sufijo `.types`: `proyecto.types.ts`

