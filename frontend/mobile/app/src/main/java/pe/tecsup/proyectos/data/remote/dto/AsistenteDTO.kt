package pe.tecsup.proyectos.data.remote.dto

data class MensajeChatDTO(
    val role: String,      // "user" | "assistant"
    val content: String
)

data class ChatRequest(
    val mensaje: String,
    val historial: List<MensajeChatDTO>
)
