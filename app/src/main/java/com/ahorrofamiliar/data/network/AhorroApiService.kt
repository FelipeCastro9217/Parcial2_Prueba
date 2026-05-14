package com.ahorro.familiar.data.network

import com.ahorro.familiar.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * AhorroApiService — interfaz de la API REST consumida con Retrofit.
 *
 * JUSTIFICACIÓN TÉCNICA:
 * - Retrofit genera automáticamente la implementación de esta interfaz en tiempo
 *   de compilación. El desarrollador solo declara los endpoints con anotaciones;
 *   Retrofit construye las peticiones HTTP, serializa el body y deserializa la respuesta.
 *
 * - Todas las funciones son suspend (corrutinas): Retrofit las ejecuta en un
 *   hilo de IO sin bloquear el hilo principal de la UI.
 *
 * - Se usa Response<T> en lugar de T directamente para acceder al código HTTP
 *   (isSuccessful, code()) y poder distinguir éxito (2xx) de error (4xx, 5xx).
 *
 * ENDPOINTS IMPLEMENTADOS:
 * - GET  /metas           → lista todas las metas con porcentaje calculado
 * - GET  /metas/{id}      → detalle de una meta con aportes por miembro
 * - POST /metas           → crea una nueva meta de ahorro
 * - POST /metas/{id}/miembros → agrega un miembro a una meta existente
 * - GET  /metas/{id}/pagos → consulta todos los pagos de una meta
 * - POST /pagos           → registra un nuevo pago de un miembro
 */
interface AhorroApiService {

    /** Obtiene la lista de todas las metas con resumen financiero (totalRecaudado, porcentajes). */
    @GET("metas")
    suspend fun getMetas(): Response<List<Meta>>

    /** Obtiene el detalle completo de una meta: foto, miembros, aportes individuales y porcentaje. */
    @GET("metas/{id}")
    suspend fun getDetalleMeta(@Path("id") id: Int): Response<Meta>

    /** Crea una nueva meta de ahorro con sus miembros iniciales. Body: CrearMetaRequest (JSON). */
    @POST("metas")
    suspend fun crearMeta(@Body request: CrearMetaRequest): Response<Meta>

    /** Agrega un nuevo miembro a una meta existente. Body: AgregarMiembroRequest (JSON). */
    @POST("metas/{id}/miembros")
    suspend fun agregarMiembro(
        @Path("id") metaId: Int,
        @Body request: AgregarMiembroRequest
    ): Response<Miembro>

    /** Obtiene todos los pagos registrados para una meta, incluyendo el nombre del miembro. */
    @GET("metas/{id}/pagos")
    suspend fun getPagosDeMeta(@Path("id") metaId: Int): Response<List<Pago>>

    /** Registra un nuevo pago asociado a un miembro de una meta. Body: CrearPagoRequest (JSON). */
    @POST("pagos")
    suspend fun registrarPago(@Body request: CrearPagoRequest): Response<Pago>
}