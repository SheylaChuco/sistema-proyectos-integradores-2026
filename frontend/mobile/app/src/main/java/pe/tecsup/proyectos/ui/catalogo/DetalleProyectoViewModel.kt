package pe.tecsup.proyectos.ui.catalogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pe.tecsup.proyectos.data.remote.dto.ProyectoDetalleDTO
import pe.tecsup.proyectos.data.repository.ProyectoRepository
import javax.inject.Inject

data class DetalleProyectoUiState(
    val cargando: Boolean = true,
    val proyecto: ProyectoDetalleDTO? = null,
    val error: String? = null
)

@HiltViewModel
class DetalleProyectoViewModel @Inject constructor(
    private val repository: ProyectoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleProyectoUiState())
    val uiState: StateFlow<DetalleProyectoUiState> = _uiState

    fun cargar(id: Long) {
        viewModelScope.launch {
            _uiState.value = DetalleProyectoUiState(cargando = true)
            repository.obtenerProyecto(id).fold(
                onSuccess = { _uiState.value = DetalleProyectoUiState(proyecto = it) },
                onFailure = { _uiState.value = DetalleProyectoUiState(error = it.message) }
            )
        }
    }
}
