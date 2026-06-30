package pe.tecsup.proyectos.data.repository

import com.google.gson.JsonParser
import pe.tecsup.proyectos.data.local.TokenManager
import pe.tecsup.proyectos.data.remote.api.ApiService
import pe.tecsup.proyectos.data.remote.dto.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun login(correo: String, password: String): Result<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(correo, password))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                tokenManager.guardarToken(data.accessToken)
                tokenManager.guardarNombreUsuario(data.usuario.nombre)
                Result.success(data)
            } else {
                Result.failure(Exception(parsearMensaje(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sin conexión al servidor"))
        }
    }

    suspend fun registro(
        nombre: String,
        correo: String,
        password: String,
        codigoEstudiante: String
    ): Result<RegistroResponse> {
        return try {
            val request = RegistroRequest(nombre, correo, password, codigoEstudiante)
            val response = api.registro(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(parsearMensaje(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sin conexión al servidor"))
        }
    }

    fun cerrarSesion() = tokenManager.cerrarSesion()
}

internal fun parsearMensaje(json: String?): String {
    return try {
        JsonParser.parseString(json).asJsonObject
            .getAsJsonObject("error")
            ?.get("message")?.asString
            ?: "Error inesperado"
    } catch (e: Exception) {
        "Error inesperado"
    }
}

internal fun parsarCodigo(json: String?): String {
    return try {
        JsonParser.parseString(json).asJsonObject
            .getAsJsonObject("error")
            ?.get("code")?.asString
            ?: "ERROR"
    } catch (e: Exception) {
        "ERROR"
    }
}
