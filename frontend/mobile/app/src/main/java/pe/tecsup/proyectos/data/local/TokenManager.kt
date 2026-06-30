package pe.tecsup.proyectos.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("proyectos_prefs", Context.MODE_PRIVATE)

    fun guardarToken(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()
    fun obtenerToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun limpiarToken() = prefs.edit().remove(KEY_TOKEN).apply()

    fun guardarNombreUsuario(nombre: String) = prefs.edit().putString(KEY_NOMBRE, nombre).apply()
    fun obtenerNombreUsuario(): String? = prefs.getString(KEY_NOMBRE, null)

    // Estado propuesta — para comparar cambios desde el worker
    fun guardarEstadoPropuesta(estado: String) = prefs.edit().putString(KEY_ESTADO_PROPUESTA, estado).apply()
    fun obtenerEstadoPropuesta(): String? = prefs.getString(KEY_ESTADO_PROPUESTA, null)

    // Estado proyecto (sustentación) — para comparar cambios desde el worker
    fun guardarEstadoProyecto(estado: String) = prefs.edit().putString(KEY_ESTADO_PROYECTO, estado).apply()
    fun obtenerEstadoProyecto(): String? = prefs.getString(KEY_ESTADO_PROYECTO, null)

    fun hayToken(): Boolean = obtenerToken() != null

    fun cerrarSesion() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_NOMBRE)
            .remove(KEY_ESTADO_PROPUESTA)
            .remove(KEY_ESTADO_PROYECTO)
            .apply()
    }

    companion object {
        private const val KEY_TOKEN = "access_token"
        private const val KEY_NOMBRE = "nombre_usuario"
        private const val KEY_ESTADO_PROPUESTA = "estado_propuesta"
        private const val KEY_ESTADO_PROYECTO = "estado_proyecto"
    }
}
