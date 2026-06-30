package pe.tecsup.proyectos.ui.catalogo

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
import pe.tecsup.proyectos.data.remote.dto.ProyectoDetalleDTO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleProyectoScreen(
    proyectoId: Long,
    onVolver: () -> Unit,
    viewModel: DetalleProyectoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(proyectoId) {
        viewModel.cargar(proyectoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del proyecto") },
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
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                }
            }
            uiState.proyecto != null -> {
                DetalleContenido(
                    proyecto = uiState.proyecto!!,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun DetalleContenido(proyecto: ProyectoDetalleDTO, modifier: Modifier = Modifier) {
    val esHistorico = proyecto.origen == "HISTORICO"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = proyecto.nombre,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            val (badgeColor, badgeText) = if (esHistorico) {
                MaterialTheme.colorScheme.secondaryContainer to "Histórico"
            } else {
                MaterialTheme.colorScheme.primaryContainer to "Nuevo"
            }
            Surface(color = badgeColor, shape = MaterialTheme.shapes.small) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Información básica
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilaInfo("Ciclo", proyecto.ciclo)
                FilaInfo("Sección", proyecto.codigoSeccion)

                // Solo para proyectos NUEVO
                if (!esHistorico) {
                    proyecto.estado?.let { FilaInfo("Estado", it) }
                    proyecto.codigoGrupo?.let { FilaInfo("Código de grupo", it) }
                }
            }
        }

        // Descripción
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Descripción", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(proyecto.descripcion, style = MaterialTheme.typography.bodyMedium)
            }
        }

        // Integrantes — solo para proyectos NUEVO con integrantes
        if (!esHistorico && !proyecto.integrantes.isNullOrEmpty()) {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Integrantes", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    proyecto.integrantes.forEach { integrante ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(integrante.nombre, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                integrante.codigoEstudiante,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        // Comentario de evaluación — solo si existe
        if (!esHistorico && proyecto.comentarioEvaluacion != null) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Comentario del evaluador", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(proyecto.comentarioEvaluacion, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun FilaInfo(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = valor, style = MaterialTheme.typography.bodyMedium)
    }
}
