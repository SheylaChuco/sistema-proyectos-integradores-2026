package pe.tecsup.proyectos.ui.catalogo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.tecsup.proyectos.data.remote.dto.ProyectoListaDTO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    onVerDetalle: (Long) -> Unit,
    viewModel: CatalogoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var textoBusqueda by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Catálogo de proyectos") })

        // Buscador
        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = {
                textoBusqueda = it
                viewModel.buscar(it)
            },
            label = { Text("Buscar por nombre...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Filtros de origen
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val opcionesOrigen = listOf(null to "Todos", "HISTORICO" to "Históricos", "NUEVO" to "Nuevos")
            items(opcionesOrigen) { (valor, etiqueta) ->
                FilterChip(
                    selected = uiState.origenSeleccionado == valor,
                    onClick = { viewModel.filtrarPorOrigen(valor) },
                    label = { Text(etiqueta) }
                )
            }
        }

        // Filtros de ciclo
        if (uiState.ciclos.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.cicloSeleccionado == null,
                        onClick = { viewModel.filtrarPorCiclo(null) },
                        label = { Text("Todos los ciclos") }
                    )
                }
                items(uiState.ciclos) { ciclo ->
                    FilterChip(
                        selected = uiState.cicloSeleccionado == ciclo,
                        onClick = { viewModel.filtrarPorCiclo(ciclo) },
                        label = { Text(ciclo) }
                    )
                }
            }
        }

        // Lista de proyectos
        val listState = rememberLazyListState()

        // Cargar más al llegar al final
        val cargarMas by remember {
            derivedStateOf {
                val layoutInfo = listState.layoutInfo
                val totalItems = layoutInfo.totalItemsCount
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                lastVisible >= totalItems - 3
            }
        }

        LaunchedEffect(cargarMas) {
            if (cargarMas) viewModel.cargarMas()
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                uiState.cargando && uiState.proyectos.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null && uiState.proyectos.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.cargarProyectos(reiniciar = true) }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.proyectos) { proyecto ->
                            ProyectoCard(proyecto = proyecto, onClick = { onVerDetalle(proyecto.id) })
                        }
                        if (uiState.cargando) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProyectoCard(proyecto: ProyectoListaDTO, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = proyecto.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                // Badge de origen
                val (badgeColor, badgeText) = if (proyecto.origen == "HISTORICO") {
                    MaterialTheme.colorScheme.secondaryContainer to "Histórico"
                } else {
                    MaterialTheme.colorScheme.primaryContainer to "Nuevo"
                }
                Surface(
                    color = badgeColor,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = proyecto.descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text(proyecto.ciclo, style = MaterialTheme.typography.labelSmall) }
                )
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            proyecto.codigoSeccion,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }
        }
    }
}
