package pe.tecsup.proyectos.data.remote.dto

data class PropuestaDTO(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val estado: String,                       // PENDIENTE | OBSERVADO | APROBADO | NO_APROBADO
    val fechaEnvio: String,                   // JSON: fecha_envio
    val comentarioObservacion: String?,       // JSON: comentario_observacion
    val codigoGrupo: String                   // JSON: codigo_grupo
)

data class CrearPropuestaRequest(
    val nombre: String,
    val descripcion: String
)

data class EditarPropuestaRequest(
    val nombre: String,
    val descripcion: String
)
