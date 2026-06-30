package pe.tecsup.proyectos.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pe.tecsup.proyectos.ui.asistente.AsistenteScreen
import pe.tecsup.proyectos.ui.auth.LoginScreen
import pe.tecsup.proyectos.ui.auth.RegistroScreen
import pe.tecsup.proyectos.ui.catalogo.CatalogoScreen
import pe.tecsup.proyectos.ui.catalogo.DetalleProyectoScreen
import pe.tecsup.proyectos.ui.grupos.MiGrupoScreen
import pe.tecsup.proyectos.ui.propuestas.MiPropuestaScreen
import pe.tecsup.proyectos.ui.propuestas.MiProyectoScreen

sealed class Ruta(val ruta: String) {
    object Login : Ruta("login")
    object Registro : Ruta("registro")
    object Catalogo : Ruta("catalogo")
    object DetalleProyecto : Ruta("catalogo/{proyectoId}") {
        fun crearRuta(id: Long) = "catalogo/$id"
    }
    object MiGrupo : Ruta("mi-grupo")
    object MiPropuesta : Ruta("mi-propuesta")
    object MiProyecto : Ruta("mi-proyecto")
    object Asistente : Ruta("asistente")
}

private val rutasConBottomBar = setOf(
    Ruta.Catalogo.ruta,
    Ruta.MiGrupo.ruta,
    Ruta.MiPropuesta.ruta,
    Ruta.Asistente.ruta
)

data class ItemNavegacion(
    val ruta: String,
    val etiqueta: String,
    val icono: androidx.compose.ui.graphics.vector.ImageVector
)

private val itemsNav = listOf(
    ItemNavegacion(Ruta.Catalogo.ruta, "Catálogo", Icons.Default.Home),
    ItemNavegacion(Ruta.MiGrupo.ruta, "Mi Grupo", Icons.Default.Group),
    ItemNavegacion(Ruta.MiPropuesta.ruta, "Propuesta", Icons.Default.Description),
    ItemNavegacion(Ruta.Asistente.ruta, "Asistente", Icons.AutoMirrored.Filled.Chat)
)

@Composable
fun AppNavigation(haySesion: Boolean) {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStack?.destination?.route

    val mostrarBottomBar = rutaActual in rutasConBottomBar

    Scaffold(
        bottomBar = {
            if (mostrarBottomBar) {
                NavigationBar {
                    itemsNav.forEach { item ->
                        NavigationBarItem(
                            selected = navBackStack?.destination?.hierarchy
                                ?.any { it.route == item.ruta } == true,
                            onClick = {
                                navController.navigate(item.ruta) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icono, contentDescription = item.etiqueta) },
                            label = { Text(item.etiqueta) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (haySesion) Ruta.Catalogo.ruta else Ruta.Login.ruta,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Ruta.Login.ruta) {
                LoginScreen(
                    onLoginExitoso = {
                        navController.navigate(Ruta.Catalogo.ruta) {
                            popUpTo(Ruta.Login.ruta) { inclusive = true }
                        }
                    },
                    onIrARegistro = { navController.navigate(Ruta.Registro.ruta) }
                )
            }

            composable(Ruta.Registro.ruta) {
                RegistroScreen(
                    onRegistroExitoso = {
                        navController.navigate(Ruta.Login.ruta) {
                            popUpTo(Ruta.Registro.ruta) { inclusive = true }
                        }
                    },
                    onVolver = { navController.popBackStack() }
                )
            }

            composable(Ruta.Catalogo.ruta) {
                CatalogoScreen(
                    onVerDetalle = { id ->
                        navController.navigate(Ruta.DetalleProyecto.crearRuta(id))
                    }
                )
            }

            composable(
                route = Ruta.DetalleProyecto.ruta,
                arguments = listOf(navArgument("proyectoId") { type = NavType.LongType })
            ) { backStack ->
                val id = backStack.arguments?.getLong("proyectoId") ?: return@composable
                DetalleProyectoScreen(
                    proyectoId = id,
                    onVolver = { navController.popBackStack() }
                )
            }

            composable(Ruta.MiGrupo.ruta) {
                MiGrupoScreen()
            }

            composable(Ruta.MiPropuesta.ruta) {
                MiPropuestaScreen(
                    onVerMiProyecto = { navController.navigate(Ruta.MiProyecto.ruta) }
                )
            }

            composable(Ruta.MiProyecto.ruta) {
                MiProyectoScreen(
                    onVolver = { navController.popBackStack() }
                )
            }

            composable(Ruta.Asistente.ruta) {
                AsistenteScreen()
            }
        }
    }
}
