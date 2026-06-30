package pe.tecsup.proyectos.data.remote.dto

data class GrupoDTO(
    val id: Long,
    val codigoGrupo: String,                  // JSON: codigo_grupo
    val periodo: String,
    val integrantes: List<IntegranteGrupoDTO>
)

data class IntegranteGrupoDTO(
    val id: Long,
    val nombre: String,
    val codigoEstudiante: String,             // JSON: codigo_estudiante
    val esLider: Boolean                      // JSON: es_lider
)

data class BuscarEstudianteDTO(
    val id: Long,
    val nombre: String,
    val codigoEstudiante: String,             // JSON: codigo_estudiante
    val tieneGrupo: Boolean                   // JSON: tiene_grupo
)

data class CrearGrupoRequest(
    val integranteIds: List<Long>             // JSON: integrante_ids
)
