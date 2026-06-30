package pe.tecsup.proyectos.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pe.tecsup.proyectos.data.repository.AuthRepository
import javax.inject.Inject

data class LoginUiState(
    val cargando: Boolean = false,
    val error: String? = null,
    val exitoso: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(correo: String, password: String) {
        if (correo.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState(error = "Completa todos los campos")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState(cargando = true)
            val result = repository.login(correo, password)
            result.fold(
                onSuccess = { _uiState.value = LoginUiState(exitoso = true) },
                onFailure = { _uiState.value = LoginUiState(error = it.message) }
            )
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
