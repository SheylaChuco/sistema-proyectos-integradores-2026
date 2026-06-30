package pe.tecsup.proyectos.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import pe.tecsup.proyectos.data.local.TokenManager
import pe.tecsup.proyectos.data.repository.PropuestaRepository
import pe.tecsup.proyectos.data.repository.ProyectoRepository
import java.util.concurrent.TimeUnit

@HiltWorker
class NotificacionWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val tokenManager: TokenManager,
    private val propuestaRepository: PropuestaRepository,
    private val proyectoRepository: ProyectoRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // Solo ejecuta si hay sesión activa
        if (!tokenManager.hayToken()) return Result.success()

        verificarCambioPropuesta()
        verificarCambioProyecto()

        return Result.success()
    }

    private suspend fun verificarCambioPropuesta() {
        val estadoAnterior = tokenManager.obtenerEstadoPropuesta()
        val result = propuestaRepository.getMiPropuesta()

        result.onSuccess { propuesta ->
            val estadoActual = propuesta.estado
            tokenManager.guardarEstadoPropuesta(estadoActual)

            if (estadoAnterior != null && estadoAnterior != estadoActual) {
                val (titulo, cuerpo) = when (estadoActual) {
                    "APROBADO" -> "¡Tu propuesta fue aprobada!" to
                            "Tu propuesta \"${propuesta.nombre}\" fue aprobada. ¡Ya puedes comenzar tu proyecto!"
                    "OBSERVADO" -> "Tu propuesta tiene observaciones" to
                            "El administrador dejó observaciones en tu propuesta. Revísalas y corrígelas."
                    else -> return
                }
                mostrarNotificacion(CHANNEL_PROPUESTA, ID_PROPUESTA, titulo, cuerpo)
            }
        }
    }

    private suspend fun verificarCambioProyecto() {
        val estadoAnterior = tokenManager.obtenerEstadoProyecto()
        val result = proyectoRepository.getMiProyecto()

        result.onSuccess { proyecto ->
            val estadoActual = proyecto.estado ?: return
            tokenManager.guardarEstadoProyecto(estadoActual)

            if (estadoAnterior != null && estadoAnterior != estadoActual) {
                val (titulo, cuerpo) = when (estadoActual) {
                    "APROBADO" -> "¡Felicidades! Sustentación aprobada" to
                            "Tu proyecto \"${proyecto.nombre}\" fue aprobado en la sustentación."
                    "NO_APROBADO" -> "Resultado de tu sustentación" to
                            "Tu proyecto \"${proyecto.nombre}\" no fue aprobado. Consulta los comentarios del evaluador."
                    else -> return
                }
                mostrarNotificacion(CHANNEL_PROYECTO, ID_PROYECTO, titulo, cuerpo)
            }
        }
    }

    private fun mostrarNotificacion(canal: String, id: Int, titulo: String, cuerpo: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal si no existe (requerido en Android 8+)
        if (manager.getNotificationChannel(canal) == null) {
            val channel = NotificationChannel(
                canal,
                "Proyectos Integradores",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val notificacion = NotificationCompat.Builder(context, canal)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setStyle(NotificationCompat.BigTextStyle().bigText(cuerpo))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        manager.notify(id, notificacion)
    }

    companion object {
        const val WORKER_NAME = "notificacion_worker"
        private const val CHANNEL_PROPUESTA = "canal_propuesta"
        private const val CHANNEL_PROYECTO = "canal_proyecto"
        private const val ID_PROPUESTA = 1001
        private const val ID_PROYECTO = 1002

        fun programar(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<NotificacionWorker>(
                15, TimeUnit.MINUTES
            ).build()

            workManager.enqueueUniquePeriodicWork(
                WORKER_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancelar(workManager: WorkManager) {
            workManager.cancelUniqueWork(WORKER_NAME)
        }
    }
}
