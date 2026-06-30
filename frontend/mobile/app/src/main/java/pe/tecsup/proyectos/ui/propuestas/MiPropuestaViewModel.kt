package pe.tecsup.proyectos.ui.propuestas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pe.tecsup.proyectos.data.local.TokenManager
import pe.tecsup.proyectos.data.remote.dto.PropuestaDTO
import pe.tecsup.proyectos.data.repository.PropuestaRepository
import javax.inject.Inject

data class MiPropuestaUiState(
    val cargando: Boolean = true,
    val propuesta: PropuestaDTO? = null,
    val sinGrupo: Boolean = false,
    val sinPropuesta: Boolean = false,
    val error: String? = null,
    // Formulario
    val nombre: String = "",
    val descripcion: String = "",
    val guardando: Boolean = false,
    val errorGuardado: String? = null,
    val modoEdicion: Boolean = false
)

@HiltViewModel
class MiPropuestaViewModel @Inject constructor(
    private val repository: PropuestaRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MiPropuestaUiState())
    val uiState: StateFlow<MiPropuestaUiState> = _uiState

    init {
        cargarMiPropuesta()
    }

    fun cargarMiPropuesta() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)
            repository.getMiPropuesta().fold(
                onSuccess = { propuesta ->
                    tokenManager.guardarEstadoPropuesta(propuesta.estado)
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        propuesta = propuesta,
                        sinGrupo = false,
                        sinPropuesta = false
                    )
                },
                onFailure = { e ->
                    when (e.message) {
                        "SIN_GRUPO" -> _uiState.value = _uiState.value.copy(
                            cargando = false, sinGrupo = true
                        )
                        "SIN_PROPUESTA" -> _uiState.value = _uiState.value.copy(
                            cargando = false, sinPropuesta = true
                        )
                        else -> _uiState.value = _uiState.value.copy(
                            cargando = false, error = e.message
                        )
                    }
                }
            )
        }
    }

    fun actualizarNombre(v: String) = run { _uiState.value = _uiState.value.copy(nombre = v) }
    fun actualizarDescripcion(v: String) = run { _uiState.value = _uiState.value.copy(descripcion = v) }

    fun activarEdicion() {
        val propuesta = _uiState.value.propuesta ?: return
        _uiState.value = _uiState.value.copy(
            modoEdicion = true,
            nombre = propuesta.nombre,
            descripcion = propuesta.descripcion
        )
    }

    fun cancelarEdicion() {
        _uiState.value = _uiState.value.copy(modoEdicion = false, errorGuardado = null)
    }

    fun crearPropuesta() {
        val estado = _uiState.value
        if (estado.nombre.isBlank() || estado.descripcion.isBlank()) {
            _uiState.value = _uiState.value.copy(errorGuardado = "Completa todos los campos")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true, errorGuardado = null)
            repository.crearPropuesta(estado.nombre, estado.descripcion).fold(
                onSuccess = { propuesta ->
                    tokenManager.guardarEstadoPropuesta(propuesta.estado)
                    _uiState.value = _uiState.value.copy(
                        guardando = false,
                        propuesta = propuesta,
                        sinPropuesta = false,
                        nombre = "",
                        descripcion = ""
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        guardando = false,
                        errorGuardado = it.message
                    )
                }
            )
        }
    }

    fun editarPropuesta() {
        val estado = _uiState.value
        val id = estado.propuesta?.id ?: return
        if (estado.nombre.isBlank() || estado.descripcion.isBlank()) {
            _uiState.value = _uiState.value.copy(errorGuardado = "Completa todos los campos")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true, errorGuardado = null)
            repository.editarPropuesta(id, estado.nombre, estado.descripcion).fold(
                onSuccess = { propuesta ->
                    tokenManager.guardarEstadoPropuesta(propuesta.estado)
                    _uiState.value = _uiState.value.copy(
                        guardando = false,
                        propuesta = propuesta,
                        modoEdicion = false
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        guardando = false,
                        errorGuardado = it.message
                    )
                }
            )
        }
    }

    fun limpiarErrorGuardado() {
        _uiState.value = _uiState.value.copy(errorGuardado = null)
    }
}
