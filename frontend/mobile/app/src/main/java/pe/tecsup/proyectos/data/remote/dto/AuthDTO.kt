package pe.tecsup.proyectos.data.remote.dto

// ---- Registro ----

data class RegistroRequest(
    val nombre: String,
    val correo: String,
    val password: String,
    val codigoEstudiante: String   // JSON: codigo_estudiante
)

data class RegistroResponse(
    val id: Long,
    val nombre: String,
    val correo: String,
    val codigoEstudiante: String   // JSON: codigo_estudiante
)

// ---- Login ----

data class LoginRequest(
    val correo: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String,       // JSON: access_token
    val usuario: UsuarioInfo
)

data class UsuarioInfo(
    val id: Long,
    val nombre: String,
    val correo: String,
    val rol: String
)
