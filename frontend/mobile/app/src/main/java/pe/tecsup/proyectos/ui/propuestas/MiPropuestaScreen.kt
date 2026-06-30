package pe.tecsup.proyectos.ui.propuestas

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.tecsup.proyectos.data.remote.dto.PropuestaDTO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiPropuestaScreen(
    onVerMiProyecto: () -> Unit,
    viewModel: MiPropuestaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.errorGuardado) {
        uiState.errorGuardado?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.limpiarErrorGuardado()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mi Propuesta") }) }
    ) { padding ->
        when {
            uiState.cargando -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.sinGrupo -> {
                Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Necesitas registrar un grupo antes de enviar una propuesta.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
            uiState.sinPropuesta || uiState.modoEdicion -> {
                FormularioPropuesta(
                    nombre = uiState.nombre,
                    descripcion = uiState.descripcion,
                    guardando = uiState.guardando,
                    esEdicion = uiState.modoEdicion,
                    onNombreChange = viewModel::actualizarNombre,
                    onDescripcionChange = viewModel::actualizarDescripcion,
                    onGuardar = if (uiState.modoEdicion) viewModel::editarPropuesta else viewModel::crearPropuesta,
                    onCancelar = if (uiState.modoEdicion) viewModel::cancelarEdicion else null,
                    modifier = Modifier.padding(padding)
                )
            }
            uiState.propuesta != null -> {
                PropuestaDetalle(
                    propuesta = uiState.propuesta!!,
                    onEditar = viewModel::activarEdicion,
                    onVerProyecto = onVerMiProyecto,
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.error ?: "Error inesperado", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = viewModel::cargarMiPropuesta) { Text("Reintentar") }
                    }
                }
            }
        }
    }
}

@Composable
private fun PropuestaDetalle(
    propuesta: PropuestaDTO,
    onEditar: () -> Unit,
    onVerProyecto: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Badge de estado
        val (badgeColor, badgeText) = when (propuesta.estado) {
            "PENDIENTE" -> MaterialTheme.colorScheme.tertiaryContainer to "Pendiente"
            "OBSERVADO" -> MaterialTheme.colorScheme.errorContainer to "Observado"
            "APROBADO" -> MaterialTheme.colorScheme.primaryContainer to "Aprobado"
            else -> MaterialTheme.colorScheme.surfaceVariant to propuesta.estado
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(propuesta.nombre, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Surface(color = badgeColor, shape = MaterialTheme.shapes.small) {
                Text(badgeText, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium)
            }
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Grupo: ${propuesta.codigoGrupo}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Enviada: ${propuesta.fechaEnvio.take(10)}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Card {
            Column(Modifier.padding(16.dp)) {
                Text("Descripción", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                Text(propuesta.descripcion, style = MaterialTheme.typography.bodyMedium)
            }
        }

        // Observación del admin
        if (propuesta.estado == "OBSERVADO" && propuesta.comentarioObservacion != null) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Observación del administrador", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(propuesta.comentarioObservacion, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Button(
                onClick = onEditar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Corregir propuesta")
            }
        }

        // Ver proyecto aprobado
        if (propuesta.estado == "APROBADO") {
            Button(
                onClick = onVerProyecto,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver mi proyecto")
            }
        }
    }
}

@Composable
private fun FormularioPropuesta(
    nombre: String,
    descripcion: String,
    guardando: Boolean,
    esEdicion: Boolean,
    onNombreChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            if (esEdicion) "Corregir propuesta" else "Enviar nueva propuesta",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = onNombreChange,
            label = { Text("Nombre del proyecto") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = onDescripcionChange,
            label = { Text("Descripción") },
            minLines = 4,
            maxLines = 8,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onGuardar,
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (guardando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(if (esEdicion) "Guardar corrección" else "Enviar propuesta")
            }
        }

        if (onCancelar != null) {
            OutlinedButton(onClick = onCancelar, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar")
            }
        }
    }
}
