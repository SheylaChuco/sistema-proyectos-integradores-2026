package pe.tecsup.proyectos.ui.grupos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pe.tecsup.proyectos.data.remote.dto.BuscarEstudianteDTO
import pe.tecsup.proyectos.data.remote.dto.GrupoDTO
import pe.tecsup.proyectos.data.repository.GrupoRepository
import javax.inject.Inject

data class MiGrupoUiState(
    val cargando: Boolean = true,
    val grupo: GrupoDTO? = null,
    val sinGrupo: Boolean = false,
    val error: String? = null,
    // Búsqueda
    val textoBusqueda: String = "",
    val resultadosBusqueda: List<BuscarEstudianteDTO> = emptyList(),
    val buscando: Boolean = false,
    val integrantesSeleccionados: List<BuscarEstudianteDTO> = emptyList(),
    val creando: Boolean = false,
    val errorCreacion: String? = null,
    val exitoCreacion: Boolean = false
)

@HiltViewModel
class MiGrupoViewModel @Inject constructor(
    private val repository: GrupoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MiGrupoUiState())
    val uiState: StateFlow<MiGrupoUiState> = _uiState

    init {
        cargarMiGrupo()
    }

    fun cargarMiGrupo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)
            repository.getMiGrupo().fold(
                onSuccess = { grupo ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        grupo = grupo,
                        sinGrupo = false
                    )
                },
                onFailure = { e ->
                    if (e.message == "SIN_GRUPO") {
                        _uiState.value = _uiState.value.copy(cargando = false, sinGrupo = true)
                    } else {
                        _uiState.value = _uiState.value.copy(cargando = false, error = e.message)
                    }
                }
            )
        }
    }

    fun buscarEstudiantes(nombre: String) {
        _uiState.value = _uiState.value.copy(textoBusqueda = nombre)
        if (nombre.length < 2) {
            _uiState.value = _uiState.value.copy(resultadosBusqueda = emptyList())
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(buscando = true)
            repository.buscarEstudiantes(nombre).onSuccess { resultados ->
                _uiState.value = _uiState.value.copy(
                    buscando = false,
                    resultadosBusqueda = resultados
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(buscando = false)
            }
        }
    }

    fun toggleSeleccion(estudiante: BuscarEstudianteDTO) {
        val seleccionados = _uiState.value.integrantesSeleccionados.toMutableList()
        if (seleccionados.any { it.id == estudiante.id }) {
            seleccionados.removeAll { it.id == estudiante.id }
        } else {
            seleccionados.add(estudiante)
        }
        _uiState.value = _uiState.value.copy(integrantesSeleccionados = seleccionados)
    }

    fun crearGrupo() {
        val ids = _uiState.value.integrantesSeleccionados.map { it.id }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(creando = true, errorCreacion = null)
            repository.crearGrupo(ids).fold(
                onSuccess = { grupo ->
                    _uiState.value = _uiState.value.copy(
                        creando = false,
                        grupo = grupo,
                        sinGrupo = false,
                        exitoCreacion = true
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        creando = false,
                        errorCreacion = it.message
                    )
                }
            )
        }
    }

    fun limpiarErrorCreacion() {
        _uiState.value = _uiState.value.copy(errorCreacion = null)
    }
}
