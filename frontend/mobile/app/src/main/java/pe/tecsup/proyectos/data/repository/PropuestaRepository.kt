package pe.tecsup.proyectos.data.repository

import pe.tecsup.proyectos.data.remote.api.ApiService
import pe.tecsup.proyectos.data.remote.dto.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PropuestaRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun getMiPropuesta(): Result<PropuestaDTO> {
        return try {
            val response = api.getMiPropuesta()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                when (response.code()) {
                    403 -> Result.failure(Exception(parsarCodigo(response.errorBody()?.string())))
                    404 -> Result.failure(Exception("SIN_PROPUESTA"))
                    else -> Result.failure(Exception("Error al cargar tu propuesta"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sin conexión al servidor"))
        }
    }

    suspend fun crearPropuesta(nombre: String, descripcion: String): Result<PropuestaDTO> {
        return try {
            val response = api.crearPropuesta(CrearPropuestaRequest(nombre, descripcion))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(parsearMensaje(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sin conexión al servidor"))
        }
    }

    suspend fun editarPropuesta(
        id: Long,
        nombre: String,
        descripcion: String
    ): Result<PropuestaDTO> {
        return try {
            val response = api.editarPropuesta(id, EditarPropuestaRequest(nombre, descripcion))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(parsearMensaje(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sin conexión al servidor"))
        }
    }
}
