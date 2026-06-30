package pe.tecsup.proyectos.data.remote.dto

data class ProyectoListaDTO(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val ciclo: String,
    val codigoSeccion: String,    // JSON: codigo_seccion
    val origen: String            // HISTORICO | NUEVO
)

data class ProyectoDetalleDTO(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val ciclo: String,
    val codigoSeccion: String,            // JSON: codigo_seccion
    val origen: String,                   // HISTORICO | NUEVO
    val estado: String?,                  // null para HISTORICO
    val codigoGrupo: String?,             // JSON: codigo_grupo; null para HISTORICO
    val integrantes: List<IntegranteDTO>? = null,
    val comentarioEvaluacion: String?     // JSON: comentario_evaluacion
)

data class IntegranteDTO(
    val nombre: String,
    val codigoEstudiante: String          // JSON: codigo_estudiante
)

data class PaginaDTO<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,              // JSON: total_elements
    val totalPages: Int                   // JSON: total_pages
)
