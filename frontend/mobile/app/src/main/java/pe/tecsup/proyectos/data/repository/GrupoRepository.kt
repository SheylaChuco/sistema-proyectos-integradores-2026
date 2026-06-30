package pe.tecsup.proyectos.data.repository

import pe.tecsup.proyectos.data.remote.api.ApiService
import pe.tecsup.proyectos.data.remote.dto.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GrupoRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun getMiGrupo(): Result<GrupoDTO> {
        return try {
            val response = api.getMiGrupo()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                val code = response.code()
                if (code == 403 || code == 404) {
                    Result.failure(Exception("SIN_GRUPO"))
                } else {
                    Result.failure(Exception("Error al cargar tu grupo"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sin conexión al servidor"))
        }
    }

    suspend fun crearGrupo(integranteIds: List<Long>): Result<GrupoDTO> {
        return try {
            val response = api.crearGrupo(CrearGrupoRequest(integranteIds))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(parsearMensaje(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sin conexión al servidor"))
        }
    }

    suspend fun buscarEstudiantes(nombre: String): Result<List<BuscarEstudianteDTO>> {
        return try {
            val response = api.buscarEstudiantes(nombre)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Error al buscar estudiantes"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sin conexión al servidor"))
        }
    }
}
