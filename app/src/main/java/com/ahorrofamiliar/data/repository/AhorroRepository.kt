package com.ahorro.familiar.data.repository

import com.ahorro.familiar.data.model.*
import com.ahorro.familiar.data.network.RetrofitClient

/**
 * AhorroRepository — capa de acceso a datos de la aplicación.
 *
 * JUSTIFICACIÓN TÉCNICA — POR QUÉ USAR UN REPOSITORY:
 * - En el patrón MVVM, el ViewModel NO debe conocer de dónde vienen los datos
 *   (API REST, base de datos local, caché, etc.). El Repository actúa como
 *   intermediario que abstrae esa decisión.
 * - Si en el futuro se agrega Room (base de datos local) o caché, solo cambia
 *   el Repository; el ViewModel y las pantallas no se modifican.
 * - Facilita las pruebas unitarias: se puede crear un FakeRepository para
 *   probar el ViewModel sin necesidad de red real.
 *
 * MANEJO DE ERRORES CON Result<T>:
 * - Cada función retorna Result<T> (Success o Failure) en lugar de lanzar excepciones.
 * - El ViewModel recibe el Result y decide si actualizar la UI con datos
 *   o mostrar un mensaje de error al usuario.
 * - try/catch envuelve cada llamada Retrofit para capturar errores de red
 *   (sin conexión, timeout, servidor caído) sin crashear la app.
 *
 * CORRUTINAS (suspend fun):
 * - Todas las funciones son suspend para ejecutarse en corrutinas del ViewModel
 *   (viewModelScope.launch), liberando el hilo principal de la UI durante
 *   operaciones de red potencialmente lentas.
 */
class AhorroRepository {

    private val api = RetrofitClient.instance

    // ---- METAS ----

    /** Obtiene la lista completa de metas con sus porcentajes calculados desde el backend. */
    suspend fun getMetas(): Result<List<Meta>> = try {
        val r = api.getMetas()
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Error ${r.code()}: no se pudo cargar la lista de metas"))
    } catch (e: Exception) { Result.failure(e) }

    /** Obtiene el detalle completo de una meta: miembros, aportes individuales y porcentaje. */
    suspend fun getDetalleMeta(id: Int): Result<Meta> = try {
        val r = api.getDetalleMeta(id)
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Meta no encontrada (código ${r.code()})"))
    } catch (e: Exception) { Result.failure(e) }

    /** Envía una nueva meta al backend mediante HTTP POST. */
    suspend fun crearMeta(request: CrearMetaRequest): Result<Meta> = try {
        val r = api.crearMeta(request)
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Error al crear meta (código ${r.code()})"))
    } catch (e: Exception) { Result.failure(e) }

    /** Agrega un nuevo miembro a una meta existente mediante HTTP POST. */
    suspend fun agregarMiembro(metaId: Int, nombre: String): Result<Miembro> = try {
        val r = api.agregarMiembro(metaId, AgregarMiembroRequest(nombre))
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Error al agregar miembro (código ${r.code()})"))
    } catch (e: Exception) { Result.failure(e) }

    // ---- PAGOS ----

    /** Consulta todos los pagos registrados para una meta específica. */
    suspend fun getPagosDeMeta(metaId: Int): Result<List<Pago>> = try {
        val r = api.getPagosDeMeta(metaId)
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Error al obtener pagos (código ${r.code()})"))
    } catch (e: Exception) { Result.failure(e) }

    /** Registra un nuevo pago asociado a un miembro de una meta mediante HTTP POST. */
    suspend fun registrarPago(request: CrearPagoRequest): Result<Pago> = try {
        val r = api.registrarPago(request)
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Error al registrar pago (código ${r.code()})"))
    } catch (e: Exception) { Result.failure(e) }
}
