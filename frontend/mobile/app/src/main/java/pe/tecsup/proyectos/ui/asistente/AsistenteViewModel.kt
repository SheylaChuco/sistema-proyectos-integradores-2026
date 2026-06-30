package pe.tecsup.proyectos.ui.asistente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pe.tecsup.proyectos.data.remote.dto.MensajeChatDTO
import pe.tecsup.proyectos.data.repository.AsistenteRepository
import javax.inject.Inject

data class Mensaje(
    val rol: String,        // "user" | "assistant"
    val contenido: String
)

data class AsistenteUiState(
    val mensajes: List<Mensaje> = emptyList(),
    val enviando: Boolean = false,
    val error: String? = null
)

private const val MAX_HISTORIAL = 10

@HiltViewModel
class AsistenteViewModel @Inject constructor(
    private val repository: AsistenteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AsistenteUiState())
    val uiState: StateFlow<AsistenteUiState> = _uiState

    fun enviarMensaje(texto: String) {
        if (texto.isBlank() || _uiState.value.enviando) return

        val mensajeUsuario = Mensaje("user", texto)
        val nuevosMensajes = _uiState.value.mensajes + mensajeUsuario
        _uiState.value = _uiState.value.copy(mensajes = nuevosMensajes, enviando = true, error = null)

        // Construye historial con máximo MAX_HISTORIAL mensajes
        val historial = nuevosMensajes
            .takeLast(MAX_HISTORIAL)
            .dropLast(1)  // El último es el mensaje actual, va en "mensaje"
            .map { MensajeChatDTO(role = it.rol, content = it.contenido) }

        viewModelScope.launch {
            repository.enviarMensaje(texto, historial).fold(
                onSuccess = { respuesta ->
                    _uiState.value = _uiState.value.copy(
                        mensajes = _uiState.value.mensajes + Mensaje("assistant", respuesta),
                        enviando = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        enviando = false,
                        error = e.message
                    )
                }
            )
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
