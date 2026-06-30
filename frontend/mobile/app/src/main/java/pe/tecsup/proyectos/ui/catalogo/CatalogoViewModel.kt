package pe.tecsup.proyectos.ui.catalogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pe.tecsup.proyectos.data.remote.dto.ProyectoListaDTO
import pe.tecsup.proyectos.data.repository.ProyectoRepository
import javax.inject.Inject

data class CatalogoUiState(
    val cargando: Boolean = false,
    val proyectos: List<ProyectoListaDTO> = emptyList(),
    val ciclos: List<String> = emptyList(),
    val cicloSeleccionado: String? = null,
    val origenSeleccionado: String? = null,
    val busqueda: String = "",
    val pagina: Int = 0,
    val hayMasPaginas: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CatalogoViewModel @Inject constructor(
    private val repository: ProyectoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogoUiState(cargando = true))
    val uiState: StateFlow<CatalogoUiState> = _uiState

    init {
        cargarCiclos()
        cargarProyectos(reiniciar = true)
    }

    private fun cargarCiclos() {
        viewModelScope.launch {
            repository.listarCiclos().onSuccess { ciclos ->
                _uiState.value = _uiState.value.copy(ciclos = ciclos)
            }
        }
    }

    fun cargarProyectos(reiniciar: Boolean = false) {
        val estado = _uiState.value
        val pagina = if (reiniciar) 0 else estado.pagina

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)
            val result = repository.listarProyectos(
                ciclo = estado.cicloSeleccionado,
                origen = estado.origenSeleccionado,
                busqueda = estado.busqueda.takeIf { it.isNotBlank() },
                page = pagina
            )
            result.fold(
                onSuccess = { pagina ->
                    val lista = if (reiniciar) pagina.content
                    else _uiState.value.proyectos + pagina.content
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        proyectos = lista,
                        pagina = pagina.page,
                        hayMasPaginas = pagina.page + 1 < pagina.totalPages
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = it.message
                    )
                }
            )
        }
    }

    fun cargarMas() {
        if (_uiState.value.hayMasPaginas && !_uiState.value.cargando) {
            _uiState.value = _uiState.value.copy(pagina = _uiState.value.pagina + 1)
            cargarProyectos()
        }
    }

    fun filtrarPorCiclo(ciclo: String?) {
        _uiState.value = _uiState.value.copy(cicloSeleccionado = ciclo)
        cargarProyectos(reiniciar = true)
    }

    fun filtrarPorOrigen(origen: String?) {
        _uiState.value = _uiState.value.copy(origenSeleccionado = origen)
        cargarProyectos(reiniciar = true)
    }

    fun buscar(texto: String) {
        _uiState.value = _uiState.value.copy(busqueda = texto)
        cargarProyectos(reiniciar = true)
    }
}
