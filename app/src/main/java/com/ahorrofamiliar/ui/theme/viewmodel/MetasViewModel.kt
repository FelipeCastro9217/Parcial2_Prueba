package com.ahorro.familiar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahorro.familiar.data.model.*
import com.ahorro.familiar.data.repository.AhorroRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * MetasViewModel — capa de presentación del patrón MVVM.
 *
 * JUSTIFICACIÓN TÉCNICA — POR QUÉ VIEWMODEL:
 * - Sobrevive a los cambios de configuración (rotación de pantalla, cambio de idioma).
 *   Sin ViewModel, cada rotación destruiría y recrearía la Activity, perdiendo el estado.
 * - Nunca referencia Activity, Fragment ni Context → no causa memory leaks.
 * - viewModelScope es un CoroutineScope ligado al ciclo de vida del ViewModel:
 *   cuando el ViewModel se destruye, todas sus corrutinas se cancelan automáticamente.
 *
 * POR QUÉ STATEFLOW EN LUGAR DE LIVEDATA:
 * - StateFlow es la alternativa moderna recomendada para Jetpack Compose.
 * - collectAsState() en Compose observa StateFlow de forma reactiva: la UI se
 *   recompone automáticamente cuando el estado cambia, sin callbacks manuales.
 * - StateFlow tiene un valor inicial siempre disponible (a diferencia de LiveData),
 *   lo que evita estados nulos en la UI.
 * - MutableStateFlow es privado (_estado): solo el ViewModel lo modifica.
 *   StateFlow público (estado) es inmutable para las pantallas → encapsulamiento.
 *
 * RESPONSABILIDADES:
 * - Llama al Repository para obtener/enviar datos (nunca llama a Retrofit directamente).
 * - Expone el estado a las pantallas mediante StateFlow.
 * - Contiene la lógica de negocio testeable (calcularPorcentaje).
 * - Las pantallas (Composables) SOLO observan los StateFlow, nunca modifican datos.
 */
class MetasViewModel : ViewModel() {

    // Repository: única fuente de datos del ViewModel.
    // El ViewModel no sabe si los datos vienen de la red, caché o BD local.
    private val repository = AhorroRepository()

    // ---- ESTADOS REACTIVOS (StateFlow) ----
    // Patrón: _estado (MutableStateFlow privado) + estado (StateFlow público e inmutable)

    private val _metas = MutableStateFlow<List<Meta>>(emptyList())
    val metas: StateFlow<List<Meta>> = _metas

    private val _detalleMeta = MutableStateFlow<Meta?>(null)
    val detalleMeta: StateFlow<Meta?> = _detalleMeta

    private val _pagos = MutableStateFlow<List<Pago>>(emptyList())
    val pagos: StateFlow<List<Pago>> = _pagos

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    // ---- OPERACIONES CON LA API (a través del Repository) ----

    /**
     * Carga la lista de metas desde el backend.
     * viewModelScope.launch ejecuta la corrutina en el hilo de IO, liberando el hilo principal.
     */
    fun cargarMetas() {
        viewModelScope.launch {
            _cargando.value = true
            repository.getMetas()
                .onSuccess { _metas.value = it }
                .onFailure { _error.value = "No se pudo cargar las metas: ${it.message}" }
            _cargando.value = false
        }
    }

    /**
     * Carga el detalle completo de una meta: miembros, aportes individuales y porcentaje.
     */
    fun cargarDetalleMeta(id: Int) {
        viewModelScope.launch {
            _cargando.value = true
            repository.getDetalleMeta(id)
                .onSuccess { _detalleMeta.value = it }
                .onFailure { _error.value = "No se pudo cargar el detalle: ${it.message}" }
            _cargando.value = false
        }
    }

    /**
     * Crea una nueva meta de ahorro enviando un POST al backend.
     * Valida datos antes de la llamada para evitar peticiones inválidas.
     */
    fun crearMeta(
        nombre: String,
        descripcion: String,
        valorTotal: Double,
        foto: String,
        nombresMiembros: List<String>
    ) {
        if (nombre.isBlank()) { _error.value = "El nombre es obligatorio"; return }
        if (valorTotal <= 0) { _error.value = "El valor debe ser mayor a 0"; return }

        viewModelScope.launch {
            _cargando.value = true
            val request = CrearMetaRequest(
                nombre = nombre,
                descripcion = descripcion,
                valorTotal = valorTotal,
                // URL de imagen por defecto si el usuario no proporciona una
                foto = foto.ifBlank { "https://picsum.photos/seed/${nombre.hashCode()}/300/200" },
                miembros = nombresMiembros.filter { it.isNotBlank() }.map { NombreMiembro(it) }
            )
            repository.crearMeta(request)
                .onSuccess { _mensaje.value = "Meta '${it.nombre}' creada exitosamente"; cargarMetas() }
                .onFailure { _error.value = "Error al crear meta: ${it.message}" }
            _cargando.value = false
        }
    }

    /**
     * Agrega un nuevo miembro a una meta existente.
     * Recarga el detalle después de agregar para reflejar el cambio en la UI.
     */
    fun agregarMiembro(metaId: Int, nombre: String) {
        if (nombre.isBlank()) { _error.value = "El nombre del miembro es obligatorio"; return }
        viewModelScope.launch {
            repository.agregarMiembro(metaId, nombre)
                .onSuccess { _mensaje.value = "${it.nombre} agregado correctamente"; cargarDetalleMeta(metaId) }
                .onFailure { _error.value = "Error al agregar miembro: ${it.message}" }
        }
    }

    /**
     * Carga todos los pagos registrados para una meta específica.
     */
    fun cargarPagosDeMeta(metaId: Int) {
        viewModelScope.launch {
            _cargando.value = true
            repository.getPagosDeMeta(metaId)
                .onSuccess { _pagos.value = it }
                .onFailure { _error.value = "Error al obtener pagos: ${it.message}" }
            _cargando.value = false
        }
    }

    /**
     * Registra un pago de un miembro hacia una meta mediante POST al backend.
     * Valida el monto antes de enviar la petición.
     * Recarga los pagos después de registrar para actualizar el historial en la UI.
     */
    fun registrarPago(metaId: Int, miembroId: Int, monto: Double, descripcion: String) {
        if (monto <= 0) { _error.value = "El monto debe ser mayor a 0"; return }
        viewModelScope.launch {
            _cargando.value = true
            val request = CrearPagoRequest(
                metaId = metaId,
                miembroId = miembroId,
                monto = monto,
                descripcion = descripcion.ifBlank { "Sin descripción" }
            )
            repository.registrarPago(request)
                .onSuccess {
                    _mensaje.value = "Pago de $${"%.0f".format(monto)} registrado exitosamente"
                    cargarPagosDeMeta(metaId)
                    cargarDetalleMeta(metaId) // Actualiza también el porcentaje de la meta
                }
                .onFailure { _error.value = "Error al registrar pago: ${it.message}" }
            _cargando.value = false
        }
    }

    // ---- LÓGICA DE NEGOCIO TESTEABLE ----

    /**
     * Calcula el porcentaje de cumplimiento de una meta de ahorro.
     *
     * Esta función está separada del ViewModel intencionalmente para ser
     * fácilmente testeable con pruebas unitarias (sin emulador ni red).
     *
     * @param totalRecaudado Monto total aportado por todos los miembros.
     * @param valorTotal     Valor objetivo de la meta de ahorro.
     * @return Porcentaje entre 0 y 100 (nunca negativo ni mayor a 100).
     */
    fun calcularPorcentaje(totalRecaudado: Double, valorTotal: Double): Int {
        if (valorTotal <= 0) return 0
        return (totalRecaudado / valorTotal * 100).toInt().coerceIn(0, 100)
    }

    // ---- LIMPIEZA DE MENSAJES ----
    // Las pantallas llaman estas funciones después de mostrar el Snackbar
    // para que el mensaje no se muestre de nuevo al recomponerse la UI.

    fun limpiarMensaje() { _mensaje.value = null }
    fun limpiarError() { _error.value = null }
}