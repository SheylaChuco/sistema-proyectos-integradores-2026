package pe.tecsup.proyectos.data.repository

import pe.tecsup.proyectos.data.remote.api.ApiService
import pe.tecsup.proyectos.data.remote.dto.ChatRequest
import pe.tecsup.proyectos.data.remote.dto.MensajeChatDTO
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AsistenteRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun enviarMensaje(
        mensaje: String,
        historial: List<MensajeChatDTO>
    ): Result<String> {
        return try {
            val response = api.chat(ChatRequest(mensaje, historial))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data ?: "")
            } else {
                Result.failure(Exception("No pude procesar tu pregunta. Intenta nuevamente."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sin conexión al servidor"))
        }
    }
}
