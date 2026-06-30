package pe.tecsup.proyectos.ui.grupos

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.tecsup.proyectos.data.remote.dto.BuscarEstudianteDTO
import pe.tecsup.proyectos.data.remote.dto.GrupoDTO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiGrupoScreen(viewModel: MiGrupoViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.errorCreacion) {
        uiState.errorCreacion?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.limpiarErrorCreacion()
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Mi Grupo") }) }) { padding ->
        when {
            uiState.cargando -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.grupo != null -> {
                GrupoDetalle(grupo = uiState.grupo!!, modifier = Modifier.padding(padding))
            }
            uiState.sinGrupo -> {
                RegistrarGrupo(
                    uiState = uiState,
                    onBuscar = viewModel::buscarEstudiantes,
                    onToggleSeleccion = viewModel::toggleSeleccion,
                    onCrear = viewModel::crearGrupo,
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.error ?: "Error al cargar", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = viewModel::cargarMiGrupo) { Text("Reintentar") }
                    }
                }
            }
        }
    }
}

@Composable
private fun GrupoDetalle(grupo: GrupoDTO, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Código", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(grupo.codigoGrupo, style = MaterialTheme.typography.titleMedium)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Período", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(grupo.periodo)
                }
            }
        }

        Text("Integrantes", style = MaterialTheme.typography.titleMedium)

        grupo.integrantes.forEach { integrante ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(integrante.nombre, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            integrante.codigoEstudiante,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (integrante.esLider) {
                        AssistChip(onClick = {}, label = { Text("Líder", style = MaterialTheme.typography.labelSmall) })
                    }
                }
            }
        }
    }
}

@Composable
private fun RegistrarGrupo(
    uiState: MiGrupoUiState,
    onBuscar: (String) -> Unit,
    onToggleSeleccion: (BuscarEstudianteDTO) -> Unit,
    onCrear: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Registrar mi grupo", style = MaterialTheme.typography.titleLarge)
            Text(
                "Busca a tus compañeros por nombre o apellido para añadirlos al grupo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            OutlinedTextField(
                value = uiState.textoBusqueda,
                onValueChange = onBuscar,
                label = { Text("Buscar compañero por nombre") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (uiState.buscando) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }

        items(uiState.resultadosBusqueda) { est ->
            val seleccionado = uiState.integrantesSeleccionados.any { it.id == est.id }
            ListItem(
                headlineContent = { Text(est.nombre) },
                supportingContent = { Text(est.codigoEstudiante) },
                trailingContent = {
                    if (!est.tieneGrupo) {
                        IconButton(onClick = { onToggleSeleccion(est) }) {
                            Icon(
                                imageVector = if (seleccionado) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = null,
                                tint = if (seleccionado)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Text("Ya tiene grupo", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
            HorizontalDivider()
        }

        if (uiState.integrantesSeleccionados.isNotEmpty()) {
            item {
                Text(
                    "Seleccionados: ${uiState.integrantesSeleccionados.joinToString { it.nombre }}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            item {
                Button(
                    onClick = onCrear,
                    enabled = !uiState.creando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.creando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Crear grupo")
                    }
                }
            }
        }
    }
}
