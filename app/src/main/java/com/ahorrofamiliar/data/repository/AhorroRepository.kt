package com.ahorro.familiar.data.repository

import com.ahorro.familiar.data.model.*
import com.ahorro.familiar.data.network.RetrofitClient

// Repository: intermediario entre la API (Retrofit) y el ViewModel.
// El ViewModel NUNCA llama directamente a Retrofit.
class AhorroRepository {

    private val api = RetrofitClient.instance

    suspend fun getMetas(): Result<List<Meta>> = try {
        val r = api.getMetas()
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Error ${r.code()}"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getDetalleMeta(id: Int): Result<Meta> = try {
        val r = api.getDetalleMeta(id)
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Meta no encontrada"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun crearMeta(request: CrearMetaRequest): Result<Meta> = try {
        val r = api.crearMeta(request)
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Error al crear meta"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun agregarMiembro(metaId: Int, nombre: String): Result<Miembro> = try {
        val r = api.agregarMiembro(metaId, AgregarMiembroRequest(nombre))
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Error al agregar miembro"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getPagosDeMeta(metaId: Int): Result<List<Pago>> = try {
        val r = api.getPagosDeMeta(metaId)
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Error al obtener pagos"))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun registrarPago(request: CrearPagoRequest): Result<Pago> = try {
        val r = api.registrarPago(request)
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Error al registrar pago"))
    } catch (e: Exception) { Result.failure(e) }
}
