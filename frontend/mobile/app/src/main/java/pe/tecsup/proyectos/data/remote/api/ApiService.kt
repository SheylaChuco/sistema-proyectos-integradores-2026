package pe.tecsup.proyectos.data.remote.api

import pe.tecsup.proyectos.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ---- Auth ----

    @POST("api/auth/registro")
    suspend fun registro(@Body request: RegistroRequest): Response<ApiResponse<RegistroResponse>>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<LoginResponse>>

    // ---- Proyectos ----

    @GET("api/proyectos")
    suspend fun listarProyectos(
        @Query("ciclo") ciclo: String? = null,
        @Query("origen") origen: String? = null,
        @Query("busqueda") busqueda: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PaginaDTO<ProyectoListaDTO>>>

    @GET("api/proyectos/ciclos")
    suspend fun listarCiclos(): Response<ApiResponse<List<String>>>

    // Debe ir ANTES de {id} para que Retrofit no lo confunda con un path variable
    @GET("api/proyectos/mi-proyecto")
    suspend fun getMiProyecto(): Response<ApiResponse<ProyectoDetalleDTO>>

    @GET("api/proyectos/{id}")
    suspend fun obtenerProyecto(@Path("id") id: Long): Response<ApiResponse<ProyectoDetalleDTO>>

    // ---- Grupos ----

    @GET("api/grupos/mi-grupo")
    suspend fun getMiGrupo(): Response<ApiResponse<GrupoDTO>>

    @POST("api/grupos")
    suspend fun crearGrupo(@Body request: CrearGrupoRequest): Response<ApiResponse<GrupoDTO>>

    @GET("api/estudiantes/buscar")
    suspend fun buscarEstudiantes(
        @Query("nombre") nombre: String
    ): Response<ApiResponse<List<BuscarEstudianteDTO>>>

    // ---- Propuestas ----

    @POST("api/propuestas")
    suspend fun crearPropuesta(
        @Body request: CrearPropuestaRequest
    ): Response<ApiResponse<PropuestaDTO>>

    @GET("api/propuestas/mi-propuesta")
    suspend fun getMiPropuesta(): Response<ApiResponse<PropuestaDTO>>

    @PUT("api/propuestas/{id}")
    suspend fun editarPropuesta(
        @Path("id") id: Long,
        @Body request: EditarPropuestaRequest
    ): Response<ApiResponse<PropuestaDTO>>

    // ---- Asistente IA ----

    @POST("api/asistente/chat")
    suspend fun chat(@Body request: ChatRequest): Response<ApiResponse<String>>
}
