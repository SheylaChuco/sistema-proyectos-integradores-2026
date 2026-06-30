package pe.tecsup.proyectos.ui.propuestas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.tecsup.proyectos.data.local.TokenManager
import pe.tecsup.proyectos.data.remote.dto.ProyectoDetalleDTO
import pe.tecsup.proyectos.data.repository.ProyectoRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MiProyectoUiState(
    val cargando: Boolean = true,
    val proyecto: ProyectoDetalleDTO? = null,
    val error: String? = null
)

@HiltViewModel
class MiProyectoViewModel @Inject constructor(
    private val repository: ProyectoRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MiProyectoUiState())
    val uiState: StateFlow<MiProyectoUiState> = _uiState

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = MiProyectoUiState(cargando = true)
            repository.getMiProyecto().fold(
                onSuccess = { proyecto ->
                    proyecto.estado?.let { tokenManager.guardarEstadoProyecto(it) }
                    _uiState.value = MiProyectoUiState(proyecto = proyecto)
                },
                onFailure = {
                    _uiState.value = MiProyectoUiState(
                        error = if (it.message == "SIN_PROYECTO")
                            "Tu grupo aún no tiene un proyecto en curso."
                        else it.message
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiProyectoScreen(
    onVolver: () -> Unit,
    viewModel: MiProyectoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Proyecto") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.cargando -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.proyecto != null -> {
                ProyectoContenido(proyecto = uiState.proyecto!!, modifier = Modifier.padding(padding))
            }
            else -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text(
                            uiState.error ?: "Error inesperado",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (uiState.error?.startsWith("Tu grupo") == false) {
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = viewModel::cargar) { Text("Reintentar") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProyectoContenido(proyecto: ProyectoDetalleDTO, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Estado del proyecto con badge
        val (badgeColor, badgeText) = when (proyecto.estado) {
            "EN_DESARROLLO" -> MaterialTheme.colorScheme.tertiaryContainer to "En desarrollo"
            "APROBADO" -> MaterialTheme.colorScheme.primaryContainer to "Aprobado"
            "NO_APROBADO" -> MaterialTheme.colorScheme.errorContainer to "No aprobado"
            else -> MaterialTheme.colorScheme.surfaceVariant to (proyecto.estado ?: "-")
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Text(proyecto.nombre, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Surface(color = badgeColor, shape = MaterialTheme.shapes.small) {
                Text(badgeText, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium)
            }
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Ciclo", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(proyecto.ciclo)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Sección", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(proyecto.codigoSeccion)
                }
                proyecto.codigoGrupo?.let {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Grupo", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(it)
                    }
                }
            }
        }

        Card {
            Column(Modifier.padding(16.dp)) {
                Text("Descripción", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                Text(proyecto.descripcion, style = MaterialTheme.typography.bodyMedium)
            }
        }

        // Integrantes
        if (!proyecto.integrantes.isNullOrEmpty()) {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Integrantes", style = MaterialTheme.typography.titleSmall)
                    proyecto.integrantes.forEach { i ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(i.nombre)
                            Text(i.codigoEstudiante, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Comentario del evaluador (solo si existe)
        if (proyecto.comentarioEvaluacion != null) {
            val bgColor = if (proyecto.estado == "APROBADO")
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer

            Card(colors = CardDefaults.cardColors(containerColor = bgColor)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Comentario del evaluador", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(proyecto.comentarioEvaluacion, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
