package pe.tecsup.proyectos.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pe.tecsup.proyectos.data.repository.AuthRepository
import javax.inject.Inject

data class RegistroUiState(
    val cargando: Boolean = false,
    val error: String? = null,
    val exitoso: Boolean = false
)

@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState

    fun registrar(nombre: String, correo: String, password: String, codigoEstudiante: String) {
        if (nombre.isBlank() || correo.isBlank() || password.isBlank() || codigoEstudiante.isBlank()) {
            _uiState.value = RegistroUiState(error = "Completa todos los campos")
            return
        }
        viewModelScope.launch {
            _uiState.value = RegistroUiState(cargando = true)
            val result = repository.registro(nombre, correo, password, codigoEstudiante)
            result.fold(
                onSuccess = { _uiState.value = RegistroUiState(exitoso = true) },
                onFailure = { _uiState.value = RegistroUiState(error = it.message) }
            )
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
