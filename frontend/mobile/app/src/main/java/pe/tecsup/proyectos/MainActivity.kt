package pe.tecsup.proyectos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import pe.tecsup.proyectos.data.local.TokenManager
import pe.tecsup.proyectos.ui.navigation.AppNavigation
import pe.tecsup.proyectos.ui.theme.ProyectosIntegradoresTheme
import pe.tecsup.proyectos.worker.NotificacionWorker
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicia el worker de notificaciones si hay sesión activa
        if (tokenManager.hayToken()) {
            NotificacionWorker.programar(WorkManager.getInstance(this))
        }

        setContent {
            ProyectosIntegradoresTheme {
                AppNavigation(haySesion = tokenManager.hayToken())
            }
        }
    }
}
