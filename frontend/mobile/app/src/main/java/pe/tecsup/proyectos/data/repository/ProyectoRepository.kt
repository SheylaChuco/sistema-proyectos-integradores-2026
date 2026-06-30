package pe.tecsup.proyectos.data.repository

import pe.tecsup.proyectos.data.remote.api.ApiService
import pe.tecsup.proyectos.data.remote.dto.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProyectoRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun listarProyectos(
        ciclo: String? = null,
        origen: String? = null,
        busqueda: String? = null,
        page: Int = 0,
        size: Int = 20
    ): Result<PaginaDTO<ProyectoListaDTO>> {
        return try {
            val response = api.listarProyectos(ciclo, origen, busqueda, page, size)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Error al cargar proyectos"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sin conexión al servidor"))
        }
    }

    suspend fun listarCiclos(): Result<List<String>> {
        return try {
            val response = api.listarCiclos()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Error al cargar ciclos"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sin conexión al servidor"))
        }
    }

    suspend fun obtenerProyecto(id: Long): Result<ProyectoDetalleDTO> {
        return try {
            val response = api.obtenerProyecto(id)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Proyecto no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sin conexión al servidor"))
        }
    }

    suspend fun getMiProyecto(): Result<ProyectoDetalleDTO> {
        return try {
            val response = api.getMiProyecto()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                val code = response.code()
                if (code == 403 || code == 404) {
                    Result.failure(Exception("SIN_PROYECTO"))
                } else {
                    Result.failure(Exception("Error al cargar tu proyecto"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sin conexión al servidor"))
        }
    }
}
